package tianyou;

import arc.math.geom.Vec2;
import arc.struct.Seq;

public class ConvexHull {

    public static Seq<Vec2> compute(Seq<Vec2> points) {
        if (points.size < 3) return new Seq<>(points);

        Seq<Vec2> pts = new Seq<>(points);
        pts.sort((a, b) -> a.x != b.x
            ? Float.compare(a.x, b.x)
            : Float.compare(a.y, b.y));

        Seq<Vec2> lower = new Seq<>();
        for (Vec2 p : pts) {
            while (lower.size >= 2 &&
                   cross(lower.get(lower.size - 2), lower.get(lower.size - 1), p) <= 0)
                lower.pop();
            lower.add(p);
        }

        Seq<Vec2> upper = new Seq<>();
        for (int i = pts.size - 1; i >= 0; i--) {
            Vec2 p = pts.get(i);
            while (upper.size >= 2 &&
                   cross(upper.get(upper.size - 2), upper.get(upper.size - 1), p) <= 0)
                upper.pop();
            upper.add(p);
        }

        lower.pop();
        upper.pop();
        lower.addAll(upper);
        return lower;
    }

    private static float cross(Vec2 o, Vec2 a, Vec2 b) {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
    }
}
