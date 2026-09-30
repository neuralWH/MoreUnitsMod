package tianyou;

import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.gen.Unit;

public class ShieldCluster {

    public final Seq<Unit> units = new Seq<>();
    public final Vec2 centroid = new Vec2();
    public ShieldPath pathC, pathD;

    public boolean canAdd(Unit u) {
        if (u == null) return false;
        Seq<Unit> test = new Seq<>(units);
        test.add(u);
        Vec2 c = new Vec2();
        for (Unit t : test) c.add(t.x, t.y);
        c.scl(1f / test.size);
        for (Unit t : test) {
            if (t.dst(c) > SharedShieldAbility.CLUSTER_RADIUS) return false;
        }
        return true;
    }

    public void updateCentroid() {
        centroid.setZero();
        for (Unit u : units) centroid.add(u.x, u.y);
        if (units.size > 0) centroid.scl(1f / units.size);
    }

    public void rebuildPaths() {
        updateCentroid();

        Seq<Vec2> centers = new Seq<>();
        for (Unit u : units) centers.add(new Vec2(u.x, u.y));

        if (units.size == 2) {
            pathC = CapsulePath.build(centers.get(0), centers.get(1), 9f * 8f, true);
            pathD = CapsulePath.build(centers.get(0), centers.get(1), 12f * 8f, false);
        } else {
            Seq<Vec2> hull = ConvexHull.compute(centers);

            RoundedPolygonPath c = new RoundedPolygonPath(ShieldPath.Type.C, true);
            c.build(hull, 9f * 8f);
            pathC = c;

            RoundedPolygonPath d = new RoundedPolygonPath(ShieldPath.Type.D, false);
            d.build(hull, 12f * 8f);
            pathD = d;
        }
    }

    public void drawShields() {
        for (Unit u : units) {
            if (u == null) continue;
            Seq<Shield> shields = ShieldSystem.getShields(u);
            if (shields == null) continue;
            for (Shield s : shields) {
                if (s == null) continue;
                if (s.state != ShieldState.ACTIVE) continue;
                if (s.currentPath == null) continue;
                s.draw();
            }
        }
    }
}
