package tianyou;

import arc.Events;
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

    public static void init() {
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

    // 为天帝自动初始化护盾
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
            if (a != null) shields.get(i).currentPath = a;
        }
        for (int i = 3; i < 5; i++) {
            if (b != null) shields.get(i).currentPath = b;
        }
        shieldMap.put(u, shields);
    }

    public static void update(float delta) {
        updateTimer += delta;

        for (Unit u : Groups.unit) {
            if (u.type != UnitTypes.collaris) continue;

            // 首次见到天帝，自动创建护盾
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

        for (Unit u : Groups.unit) {
            if (u.type != UnitTypes.collaris) continue;
            Seq<Shield> shields = shieldMap.get(u);
            if (shields == null) continue;
            for (Shield s : shields) s.update(delta);
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

        // 规则1：路径A补至3，多出转移至路径B
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

        // 规则2：路径B转移至C/D
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

        // 超过容量时返还至路径A
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

        // 规则3：均匀分布
        distributeOnPath(allShields, ShieldPath.Type.A);
        distributeOnPath(allShields, ShieldPath.Type.B);
        distributeOnPath(allShields, ShieldPath.Type.C);
        distributeOnPath(allShields, ShieldPath.Type.D);
    }

    private static void distributeOnPath(Seq<Shield> allShields, ShieldPath.Type type) {
        Seq<Shield> onPath = new Seq<>();
        for (Shield s : allShields) {
            if (s.currentPath != null && s.currentPath.type == type && !s.intercepting) {
                onPath.add(s);
            }
        }
        if (onPath.isEmpty()) return;

        int n = onPath.size;
        for (int i = 0; i < n; i++) {
            Shield s = onPath.get(i);
            float t = (i / (float) n + Time.time * 0.001f) % 1f;
            Vec2 target = s.currentPath.pointAt(t);
            Vec2 tan = s.currentPath.tangentAt(t);
            s.pathParam = t;
            s.tangent.set(tan);
            s.startTransition(target);
        }
    }

    private static void checkClusterStructure() {
        Seq<Unit> allTianyou = new Seq<>();
        for (Unit u : Groups.unit) {
            if (u.type == UnitTypes.collaris && u.isValid() && !u.dead) {
                allTianyou.add(u);
            }
        }

        // 逐个检查离开
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

        // 逐个尝试加入
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
