package tianyou;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;

public class CapsulePath extends ShieldPath {

    public final Seq<Vec2> points = new Seq<>();
    public float[] cumLen;
    public float totalLen;

    public CapsulePath(Type type, boolean clockwise) {
        this.type = type;
        this.clockwise = clockwise;
    }

    public static CapsulePath build(Vec2 a, Vec2 b, float radius, boolean clockwise) {
        CapsulePath p = new CapsulePath(Type.C, clockwise);
        p.points.addAll(sample(a, b, radius));
        p.computeArcLength();
        return p;
    }

    public static Seq<Vec2> sample(Vec2 a, Vec2 b, float radius) {
        Seq<Vec2> result = new Seq<>();
        Vec2 dir = new Vec2(b).sub(a).nor();
        float angle = Mathf.atan2(dir.y, dir.x);

        for (int i = 0; i <= 16; i++) {
            float t = i / 16f;
            float ang = angle + Mathf.PI / 2f + t * Mathf.PI;
            result.add(new Vec2(
                a.x + Mathf.cos(ang) * radius,
                a.y + Mathf.sin(ang) * radius
            ));
        }
        for (int i = 0; i <= 16; i++) {
            float t = i / 16f;
            float ang = angle - Mathf.PI / 2f + t * Mathf.PI;
            result.add(new Vec2(
                b.x + Mathf.cos(ang) * radius,
                b.y + Mathf.sin(ang) * radius
            ));
        }
        return result;
    }

    public void computeArcLength() {
        int n = points.size;
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
