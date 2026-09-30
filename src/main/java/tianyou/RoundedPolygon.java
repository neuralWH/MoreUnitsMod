package tianyou;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;

public class RoundedPolygon {

    public static Seq<Vec2> build(Seq<Vec2> hull, float radius) {
        int n = hull.size;
        Seq<Vec2> result = new Seq<>();
        if (n < 2) return result;

        if (n == 2) {
            return CapsulePath.sample(hull.get(0), hull.get(1), radius);
        }

        Seq<Vec2> filtered = mergeSharpVertices(hull, 20f);
        n = filtered.size;

        for (int i = 0; i < n; i++) {
            Vec2 prev = filtered.get((i - 1 + n) % n);
            Vec2 cur = filtered.get(i);
            Vec2 next = filtered.get((i + 1) % n);

            Vec2 d1 = new Vec2(cur).sub(prev).nor();
            Vec2 d2 = new Vec2(next).sub(cur).nor();

            Vec2 n1 = new Vec2(-d1.y, d1.x);
            Vec2 n2 = new Vec2(-d2.y, d2.x);

            Vec2 bis = new Vec2(n1).add(n2).nor();
            float cosHalf = bis.dot(n1);
            float d = cosHalf > 0.01f ? radius / cosHalf : radius;

            Vec2 arcCenter = new Vec2(cur).add(bis.scl(d));

            float startAngle = Mathf.atan2(n1.y, n1.x);
            float endAngle = Mathf.atan2(n2.y, n2.x);
            float sweep = endAngle - startAngle;
            if (sweep < 0) sweep += Mathf.PI2;

            int samples = Math.max(4, (int) (sweep / 0.2f));
            for (int s = 0; s <= samples; s++) {
                float t = s / (float) samples;
                float angle = startAngle + sweep * t;
                result.add(new Vec2(
                    arcCenter.x + Mathf.cos(angle) * radius,
                    arcCenter.y + Mathf.sin(angle) * radius
                ));
            }
        }

        return result;
    }

    public static Seq<Vec2> mergeSharpVertices(Seq<Vec2> hull, float angleDeg) {
        int n = hull.size;
        if (n < 3) return new Seq<>(hull);
        Seq<Vec2> result = new Seq<>();
        float cosLimit = Mathf.cosDeg(180f - angleDeg);
        for (int i = 0; i < n; i++) {
            Vec2 prev = hull.get((i - 1 + n) % n);
            Vec2 cur = hull.get(i);
            Vec2 next = hull.get((i + 1) % n);

            Vec2 d1 = new Vec2(cur).sub(prev).nor();
            Vec2 d2 = new Vec2(next).sub(cur).nor();
            float cos = d1.dot(d2);

            if (cos < cosLimit) result.add(new Vec2(cur));
        }
        return result.size < 3 ? new Seq<>(hull) : result;
    }
}
