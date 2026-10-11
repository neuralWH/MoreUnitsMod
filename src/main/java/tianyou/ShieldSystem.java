package tianyou;

import arc.Events;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.UnitTypes;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class ShieldSystem {

    public static final Seq<ShieldCluster> clusters = new Seq<>();
    public static final ObjectMap<Unit, ShieldCluster> unitToCluster = new ObjectMap<>();
    public static final ObjectMap<Unit, Seq<Shield>> shieldMap = new ObjectMap<>();
    public static final ObjectMap<Unit, CirclePath> pathAMap = new ObjectMap<>();
    public static final ObjectMap<Unit, CirclePath> pathBMap = new ObjectMap<>();

    public static float updateTimer = 0f;
    public static final float UPDATE_INTERVAL = 0.5f;

    // 护盾沿路径的线速度（世界单位/秒）
    public static final float LINEAR_SPEED = 30f;

    private static boolean registered = false;

    public static void init() {
        if (registered) return;
        registered = true;

        Events.run(Trigger.update, () -> {
            float delta = Time.delta;
            update(delta);
        });
    }

    public static void initUnitPaths(Unit u) {
        CirclePath a = new CirclePath(ShieldPath.Type.A, 6f * 8f, false);
        CirclePath b = new CirclePath(ShieldPath.Type.B, 9f * 8f, true);
        a.center.set(u.x, u.y);
        b.center.set(u.x, u.y);
        pathAMap.put(u, a);
        pathBMap.put(u, b);
    }

    public static CirclePath getPathA(Unit u) {
        return pathAMap.get(u);
    }

    public static CirclePath getPathB(Unit u) {
        return pathBMap.get(u);
    }

    private static void ensureShields(Unit u) {
        if (shieldMap.containsKey(u)) return;
        initUnitPaths(u);
        Seq<Shield> shields = new Seq<>();
        for (int i = 0; i < 5; i++) {
            shields.add(new Shield(i, u));
        }
        CirclePath a = pathAMap.get(u);
        CirclePath b = pathBMap.get(u);

        for (int i = 0; i < 3; i++) {
            if (a != null) {
                Shield s = shields.get(i);
                s.currentPath = a;
                s.pathParam = i / 3f;
                s.targetPathParam = s.pathParam;
            }
        }
        for (int i = 3; i < 5; i++) {
            if (b != null) {
                Shield s = shields.get(i);
                s.currentPath = b;
                s.pathParam = (i - 3) / 2f;
                s.targetPathParam = s.pathParam;
            }
        }
        shieldMap.put(u, shields);
    }

    public static void update(float delta) {
        updateTimer += delta;

        for (Unit u : Groups.unit) {
            if (u.type != UnitTypes.collaris) continue;
            ensureShields(u);
            CirclePath a = pathAMap.get(u);
            CirclePath b = pathBMap.get(u);
            if (a != null) a.center.set(u.x, u.y);
            if (b != null) b.center.set(u.x, u.y);
        }

        checkClusterStructure();

        if (updateTimer >= UPDATE_INTERVAL) {
            updateTimer = 0f;
            for (ShieldCluster c : clusters) {
                distributeShields(c);
            }
        }

        InterceptSystem.update(delta);

        updateTargetPathParams();

        for (Unit u : Groups.unit) {
            if (u.type != UnitTypes.collaris) continue;
            Seq<Shield> shields = shieldMap.get(u);
            if (shields == null) continue;
            for (Shield s : shields) s.update(delta);
        }
    }

    // 用 Time.time 计算相位。Time.time 单位是 tick，除以 60 转成秒
    private static void updateTargetPathParams() {
        float seconds = Time.time / 60f;

        for (Unit u : Groups.unit) {
            if (u.type != UnitTypes.collaris) continue;
            Seq<Shield> shields = shieldMap.get(u);
            if (shields == null) continue;

            CirclePath a = pathAMap.get(u);
            CirclePath b = pathBMap.get(u);

            float phaseA = 0f;
            float phaseB = 0f;
            if (a != null && a.length() > 0.01f) {
                phaseA = (seconds * LINEAR_SPEED / a.length()) % 1f;
            }
            if (b != null && b.length() > 0.01f) {
                phaseB = (seconds * LINEAR_SPEED / b.length()) % 1f;
            }

            ShieldCluster cluster = unitToCluster.get(u);
            float phaseC = cluster != null ? cluster.phaseC : 0f;
            float phaseD = cluster != null ? cluster.phaseD : 0f;

            if (cluster != null) {
                if (cluster.pathC != null && cluster.pathC.length() > 0.01f) {
                    phaseC = (seconds * LINEAR_SPEED / cluster.pathC.length()) % 1f;
                    cluster.phaseC = phaseC;
                }
                if (cluster.pathD != null && cluster.pathD.length() > 0.01f) {
                    phaseD = (seconds * LINEAR_SPEED / cluster.pathD.length()) % 1f;
                    cluster.phaseD = phaseD;
                }
            }

            int cA = 0, cB = 0, cC = 0, cD = 0;
            for (Shield s : shields) {
                if (s.currentPath == null || s.intercepting) continue;
                switch (s.currentPath.type) {
                    case A: cA++; break;
                    case B: cB++; break;
                    case C: cC++; break;
                    case D: cD++; break;
                }
            }

            int iA = 0, iB = 0, iC = 0, iD = 0;
            for (Shield s : shields) {
                if (s.currentPath == null || s.intercepting) continue;
                switch (s.currentPath.type) {
                    case A:
                        s.targetPathParam = (phaseA + iA / (float) Math.max(1, cA)) % 1f;
                        iA++;
                        break;
                    case B:
                        s.targetPathParam = (phaseB + iB / (float) Math.max(1, cB)) % 1f;
                        iB++;
                        break;
                    case C:
                        s.targetPathParam = (phaseC + iC / (float) Math.max(1, cC)) % 1f;
                        iC++;
                        break;
                    case D:
                        s.targetPathParam = (phaseD + iD / (float) Math.max(1, cD)) % 1f;
                        iD++;
                        break;
                }
            }
        }
    }

    private static void distributeShields(ShieldCluster c) {
        c.updateCentroid();
        if (c.units.size < 2) return;

        if (c.pathC == null || c.pathD == null) {
            c.rebuildPaths();
        }

        Seq<Shield> allShields = new Seq<>();

        for (Unit u : c.units) {
            if (u.type != UnitTypes.collaris) continue;
            Seq<Shield> shields = shieldMap.get(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (s.state == ShieldState.ACTIVE && !s.intercepting) {
                    allShields.add(s);
                }
            }
        }

        for (Unit u : c.units) {
            if (u.type != UnitTypes.collaris) continue;
            CirclePath pathA = getPathA(u);
            CirclePath pathB = getPathB(u);
            if (pathA == null || pathB == null) continue;

            Seq<Shield> owned = new Seq<>();
            for (Shield s : allShields) {
                if (s.owner == u) owned.add(s);
            }

            int onA = 0;
            for (Shield s : owned) {
                if (s.currentPath == pathA) onA++;
            }

            if (onA < 3) {
                int need = 3 - onA;
                for (int i = 0; i < need; i++) {
                    Shield best = null;
                    float bestDist = Float.MAX_VALUE;
                    for (Shield s : owned) {
                        if (s.currentPath == pathA) continue;
                        float d = s.position.dst(u.x, u.y);
                        if (d < bestDist) { bestDist = d; best = s; }
                    }
                    if (best != null) best.currentPath = pathA;
                }
            } else if (onA > 3) {
                int excess = onA - 3;
                int moved = 0;
                for (Shield s : owned) {
                    if (moved >= excess) break;
                    if (s.currentPath == pathA) {
                        s.currentPath = pathB;
                        moved++;
                    }
                }
            }
        }

        int capC = c.pathC.capacity();
        int capD = c.pathD.capacity();

        int onC = 0, onD = 0;
        for (Shield s : allShields) {
            if (s.currentPath == null) continue;
            if (s.currentPath.type == ShieldPath.Type.C) onC++;
            if (s.currentPath.type == ShieldPath.Type.D) onD++;
        }

        for (Shield s : allShields) {
            if (s.currentPath == null) continue;
            if (s.currentPath.type != ShieldPath.Type.B) continue;
            if (s.intercepting) continue;

            if (onC < capC) {
                s.currentPath = c.pathC;
                onC++;
            } else if (onD < capD) {
                s.currentPath = c.pathD;
                onD++;
            }
        }

        for (Shield s : allShields) {
            if (s.intercepting) continue;
            if (s.currentPath == null) continue;
            if (s.currentPath.type == ShieldPath.Type.C && onC > capC) {
                s.currentPath = getPathA(s.owner);
                onC--;
            } else if (s.currentPath.type == ShieldPath.Type.D && onD > capD) {
                s.currentPath = getPathA(s.owner);
                onD--;
            }
        }
    }

    private static void checkClusterStructure() {
        Seq<Unit> allTianyou = new Seq<>();
        for (Unit u : Groups.unit) {
            if (u.type == UnitTypes.collaris && u.isValid() && !u.dead) {
                allTianyou.add(u);
            }
        }

        for (Unit u : allTianyou) {
            ShieldCluster current = unitToCluster.get(u);
            if (current == null) continue;
            if (u.dst(current.centroid) > SharedShieldAbility.CLUSTER_RADIUS) {
                current.units.remove(u);
                unitToCluster.remove(u);
                if (current.units.size < 2) {
                    clusters.remove(current);
                }
            }
        }

        for (Unit u : allTianyou) {
            if (unitToCluster.containsKey(u)) continue;

            boolean joined = false;
            for (ShieldCluster c : clusters) {
                if (c.canAdd(u)) {
                    c.units.add(u);
                    unitToCluster.put(u, c);
                    c.updateCentroid();
                    c.rebuildPaths();
                    joined = true;
                    break;
                }
            }
            if (!joined) {
                ShieldCluster nc = new ShieldCluster();
                nc.units.add(u);
                nc.updateCentroid();
                clusters.add(nc);
                unitToCluster.put(u, nc);
            }
        }
    }

    public static void registerShields(Unit u, Seq<Shield> shields) {
        shieldMap.put(u, shields);
    }

    public static Seq<Shield> getShields(Unit u) {
        return shieldMap.get(u);
    }

    public static void drawSingleUnitShields(Unit unit) {
        Seq<Shield> shields = getShields(unit);
        if (shields == null) return;
        for (Shield s : shields) {
            if (s.state == ShieldState.ACTIVE) s.draw();
        }
    }
}
