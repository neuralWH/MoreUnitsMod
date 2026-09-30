package tianyou;

import arc.math.geom.Vec2;
import arc.struct.Seq;

public class RoundedPolygonPath extends ShieldPath {

    public final Seq<Vec2> points = new Seq<>();
    public float[] cumLen;
    public float totalLen;

    public RoundedPolygonPath(Type type, boolean clockwise) {
        this.type = type;
        this.clockwise = clockwise;
    }

    public void build(Seq<Vec2> hull, float targetDistance) {
        Seq<Vec2> filtered = RoundedPolygon.mergeSharpVertices(hull, 20f);
        points.clear();
        points.addAll(RoundedPolygon.build(filtered, targetDistance));
        computeArcLength();
    }

    public void computeArcLength() {
        int n = points.size;
        if (n < 2) { totalLen = 0; return; }
        cumLen = new float[n];
        for (int i = 1; i < n; i++) {
            cumLen[i] = cumLen[i - 1] + points.get(i).dst(points.get(i - 1));
        }
        totalLen = cumLen[n - 1] + points.get(0).dst(points.get(n - 1));
    }

    @Override
    public Vec2 pointAt(float t) {
        if (points.isEmpty()) return new Vec2();
        float target = t * totalLen;
        for (int i = 1; i < points.size; i++) {
            if (cumLen[i] >= target) {
                float segT = (cumLen[i] - cumLen[i - 1]) > 0.001f
                    ? (target - cumLen[i - 1]) / (cumLen[i] - cumLen[i - 1])
                    : 0f;
                return new Vec2(points.get(i - 1)).lerp(points.get(i), segT);
            }
        }
        return new Vec2(points.first());
    }

    @Override
    public Vec2 tangentAt(float t) {
        Vec2 p1 = pointAt(t);
        Vec2 p2 = pointAt((t + 0.001f) % 1f);
        return new Vec2(p2).sub(p1).nor();
    }

    @Override
    public float length() {
        return totalLen;
    }
}
