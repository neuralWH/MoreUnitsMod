package tianyou;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class InterceptSystem {

    public static final float VISION_RADIUS = 48f;

    public static void init() {
    }

    public static void update(float delta) {
        Seq<Bullet> bullets = new Seq<>();
        for (Bullet b : Groups.bullet) bullets.add(b);

        for (ShieldCluster c : ShieldSystem.clusters) {
            handleClusterIntercept(c, bullets);
        }

        for (Unit u : Groups.unit) {
            if (u.type != TianyouUnit.tianyou) continue;
            if (ShieldSystem.unitToCluster.containsKey(u)) continue;
            handleSingleIntercept(u, bullets);
        }

        updateInterceptingShields();
    }

    private static void handleClusterIntercept(ShieldCluster c, Seq<Bullet> bullets) {
        bullets.sort((a, b) -> {
            float da = distToNearestUnit(a, c);
            float db = distToNearestUnit(b, c);
            return Float.compare(da, db);
        });

        for (Bullet b : bullets) {
            if (!isEnemy(b, c)) continue;
            if (!inVision(c.centroid, b)) continue;
            if (hasAssignedShield(b)) continue;
            if (inOtherShieldRange(b, c)) continue;

            Shield assigned = null;
            if (assigned == null) assigned = tryAssignToPath(c, c.pathD, b);
            if (assigned == null) assigned = tryAssignToPath(c, c.pathC, b);
            if (assigned == null) assigned = tryAssignToPathB(c, b);
            if (assigned == null) assigned = tryAssignToPathA(c, b);

            if (assigned != null) {
                assigned.intercepting = true;
                assigned.interceptTarget = b;
                assigned.assigned = true;
            }
        }
    }

    private static void handleSingleIntercept(Unit unitA, Seq<Bullet> bullets) {
        bullets.sort((a, b) -> {
            float da = dist(unitA.x, unitA.y, a.x, a.y);
            float db = dist(unitA.x, unitA.y, b.x, b.y);
            return Float.compare(da, db);
        });

        for (Bullet b : bullets) {
            if (!isEnemy(b, unitA)) continue;
            if (!inVision(unitA.x, unitA.y, b)) continue;
            if (hasAssignedShield(b)) continue;

            Shield assigned = tryAssignToPathSingle(unitA, ShieldPath.Type.B, b);
            if (assigned == null) assigned = tryAssignToPathSingle(unitA, ShieldPath.Type.A, b);

            if (assigned != null) {
                assigned.intercepting = true;
                assigned.interceptTarget = b;
                assigned.assigned = true;
            }
        }
    }

    private static Shield tryAssignToPath(ShieldCluster c, ShieldPath path, Bullet b) {
        if (path == null) return null;
        Vec2 intersection = linePathIntersect(c.centroid, new Vec2(b.x, b.y), path);
        if (intersection == null) return null;

        Shield best = null;
        float bestDist = Float.MAX_VALUE;
        for (Unit u : c.units) {
            if (u.type != TianyouUnit.tianyou) continue;
            Seq<Shield> shields = ShieldSystem.getShields(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (s.state != ShieldState.ACTIVE) continue;
                if (s.assigned) continue;
                if (s.currentPath != path) continue;
                float d = s.position.dst(intersection);
                if (d < bestDist) { bestDist = d; best = s; }
            }
        }
        return best;
    }

    private static Shield tryAssignToPathB(ShieldCluster c, Bullet b) {
        Shield best = null;
        float bestDist = Float.MAX_VALUE;
        for (Unit u : c.units) {
            if (u.type != TianyouUnit.tianyou) continue;
            CirclePath pathB = ShieldSystem.getPathB(u);
            if (pathB == null) continue;
            Vec2 intersection = linePathIntersect(c.centroid, new Vec2(b.x, b.y), pathB);
            if (intersection == null) continue;

            Seq<Shield> shields = ShieldSystem.getShields(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (s.state != ShieldState.ACTIVE) continue;
                if (s.assigned) continue;
                if (s.currentPath != pathB) continue;
                float d = s.position.dst(intersection);
                if (d < bestDist) { bestDist = d; best = s; }
            }
        }
        return best;
    }

    private static Shield tryAssignToPathA(ShieldCluster c, Bullet b) {
        Shield best = null;
        float bestDist = Float.MAX_VALUE;
        for (Unit u : c.units) {
            if (u.type != TianyouUnit.tianyou) continue;
            CirclePath pathA = ShieldSystem.getPathA(u);
            if (pathA == null) continue;
            Vec2 intersection = linePathIntersect(c.centroid, new Vec2(b.x, b.y), pathA);
            if (intersection == null) continue;

            Seq<Shield> shields = ShieldSystem.getShields(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (s.state != ShieldState.ACTIVE) continue;
                if (s.assigned) continue;
                if (s.currentPath != pathA) continue;
                float d = s.position.dst(intersection);
                if (d < bestDist) { bestDist = d; best = s; }
            }
        }
        return best;
    }

    private static Shield tryAssignToPathSingle(Unit unitA, ShieldPath.Type type, Bullet b) {
        CirclePath path = type == ShieldPath.Type.A
            ? ShieldSystem.getPathA(unitA)
            : ShieldSystem.getPathB(unitA);
        if (path == null) return null;

        Vec2 intersection = linePathIntersect(new Vec2(unitA.x, unitA.y), new Vec2(b.x, b.y), path);
        if (intersection == null) return null;

        Seq<Shield> shields = ShieldSystem.getShields(unitA);
        if (shields == null) return null;

        Shield best = null;
        float bestDist = Float.MAX_VALUE;
        for (Shield s : shields) {
            if (s.state != ShieldState.ACTIVE) continue;
            if (s.assigned) continue;
            if (s.currentPath == null || s.currentPath.type != type) continue;
            float d = s.position.dst(intersection);
            if (d < bestDist) { bestDist = d; best = s; }
        }
        return best;
    }

    public static Vec2 linePathIntersect(Vec2 from, Vec2 to, ShieldPath path) {
        if (path instanceof CirclePath cp) {
            return lineCircleIntersect(cp.center, cp.radius, from, to);
        }
        return linePathIntersectApprox(from, to, path);
    }

    private static Vec2 lineCircleIntersect(Vec2 circleCenter, float radius, Vec2 p1, Vec2 p2) {
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float fx = p1.x - circleCenter.x;
        float fy = p1.y - circleCenter.y;

        float a = dx * dx + dy * dy;
        float b = 2 * (fx * dx + fy * dy);
        float c = fx * fx + fy * fy - radius * radius;

        float discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return null;

        float sqrtD = (float) Math.sqrt(discriminant);
        float t1 = (-b - sqrtD) / (2 * a);
        float t2 = (-b + sqrtD) / (2 * a);

        Vec2 i1 = (t1 >= 0 && t1 <= 1) ? new Vec2(p1.x + t1 * dx, p1.y + t1 * dy) : null;
        Vec2 i2 = (t2 >= 0 && t2 <= 1) ? new Vec2(p1.x + t2 * dx, p1.y + t2 * dy) : null;

        if (i1 == null) return i2;
        if (i2 == null) return i1;
        return i1.dst(p2) < i2.dst(p2) ? i1 : i2;
    }

    private static Vec2 linePathIntersectApprox(Vec2 p1, Vec2 p2, ShieldPath path) {
        Vec2 best = null;
        float bestDist = Float.MAX_VALUE;
        int samples = 200;
        for (int i = 0; i < samples; i++) {
            float t = i / (float) samples;
            Vec2 pt = path.pointAt(t);
            float d = pointToSegmentDistance(pt, p1, p2);
            if (d < 8f) {
                float distToBullet = pt.dst(p2);
                if (distToBullet < bestDist) {
                    bestDist = distToBullet;
                    best = pt;
                }
            }
        }
        return best;
    }

    private static float pointToSegmentDistance(Vec2 p, Vec2 a, Vec2 b) {
        float abx = b.x - a.x;
        float aby = b.y - a.y;
        float apx = p.x - a.x;
        float apy = p.y - a.y;
        float abLenSq = abx * abx + aby * aby;
        if (abLenSq < 0.0001f) return p.dst(a);
        float t = (apx * abx + apy * aby) / abLenSq;
        t = Mathf.clamp(t, 0f, 1f);
        float cx = a.x + abx * t;
        float cy = a.y + aby * t;
        float dx = p.x - cx;
        float dy = p.y - cy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static void updateInterceptingShields() {
        for (Unit u : Groups.unit) {
            if (u.type != TianyouUnit.tianyou) continue;
            Seq<Shield> shields = ShieldSystem.getShields(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (!s.intercepting || s.interceptTarget == null) continue;
                updateShieldInterceptPosition(s, u);
            }
        }
    }

    private static void updateShieldInterceptPosition(Shield s, Unit u) {
        Bullet b = (Bullet) s.interceptTarget;
        if (b == null || !b.isAdded()) {
            s.intercepting = false;
            s.interceptTarget = null;
            s.assigned = false;
            return;
        }

        Vec2 center = new Vec2(u.x, u.y);
        Vec2 intersection = linePathIntersect(center, new Vec2(b.x, b.y), s.currentPath);
        if (intersection == null) return;

        float targetT = findClosestPathParam(s.currentPath, intersection);
        float currentT = s.pathParam;
        float diff = targetT - currentT;
        if (diff > 0.5f) diff -= 1f;
        if (diff < -0.5f) diff += 1f;

        float speed = Time.delta * 0.002f;
        float move = Mathf.clamp(diff, -speed, speed);
        s.pathParam = (s.pathParam + move + 1f) % 1f;

        Vec2 pos = s.currentPath.pointAt(s.pathParam);
        s.position.set(pos);
        s.tangent.set(s.currentPath.tangentAt(s.pathParam));
    }

    private static float findClosestPathParam(ShieldPath path, Vec2 target) {
        float bestT = 0;
        float bestDist = Float.MAX_VALUE;
        int samples = 100;
        for (int i = 0; i < samples; i++) {
            float t = i / (float) samples;
            Vec2 pt = path.pointAt(t);
            float d = pt.dst(target);
            if (d < bestDist) { bestDist = d; bestT = t; }
        }
        return bestT;
    }

    private static boolean inVision(Vec2 center, Bullet b) {
        return inVision(center.x, center.y, b);
    }

    private static boolean inVision(float cx, float cy, Bullet b) {
        float dx = cx - b.x;
        float dy = cy - b.y;
        return dx * dx + dy * dy <= VISION_RADIUS * VISION_RADIUS;
    }

    private static boolean isEnemy(Bullet b, Unit owner) {
        return b.team != owner.team;
    }

    private static boolean isEnemy(Bullet b, ShieldCluster c) {
        if (c.units.isEmpty()) return false;
        return b.team != c.units.first().team;
    }

    private static boolean hasAssignedShield(Bullet b) {
        for (Unit u : Groups.unit) {
            if (u.type != TianyouUnit.tianyou) continue;
            Seq<Shield> shields = ShieldSystem.getShields(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (s.assigned && s.interceptTarget == b) return true;
            }
        }
        return false;
    }

    private static boolean inOtherShieldRange(Bullet b, ShieldCluster c) {
        for (Unit u : Groups.unit) {
            if (u.type != TianyouUnit.tianyou) continue;
            ShieldCluster other = ShieldSystem.unitToCluster.get(u);
            if (other == null || other == c) continue;
            Seq<Shield> shields = ShieldSystem.getShields(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (s.state != ShieldState.ACTIVE) continue;
                if (inShieldInterceptRange(s, b)) return true;
            }
        }
        return false;
    }

    private static boolean inShieldInterceptRange(Shield s, Bullet b) {
        float angle = Mathf.atan2(s.tangent.y, s.tangent.x);
        float dx = b.x - s.position.x;
        float dy = b.y - s.position.y;
        float localY = dx * Mathf.sin(-angle) + dy * Mathf.cos(-angle);
        return Math.abs(localY) <= 8f;
    }

    private static float distToNearestUnit(Bullet b, ShieldCluster c) {
        float min = Float.MAX_VALUE;
        for (Unit u : c.units) {
            float d = dist(u.x, u.y, b.x, b.y);
            if (d < min) min = d;
        }
        return min;
    }

    private static float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
