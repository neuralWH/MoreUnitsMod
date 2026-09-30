package tianyou;

import arc.math.Mathf;
import arc.math.geom.Vec2;

public class CirclePath extends ShieldPath {

    public final Vec2 center = new Vec2();
    public float radius;

    public CirclePath(Type type, float radius, boolean clockwise) {
        this.type = type;
        this.radius = radius;
        this.clockwise = clockwise;
    }

    @Override
    public Vec2 pointAt(float t) {
        float angle = t * Mathf.PI2 * (clockwise ? -1f : 1f);
        return new Vec2(
            center.x + Mathf.cos(angle) * radius,
            center.y + Mathf.sin(angle) * radius
        );
    }

    @Override
    public Vec2 tangentAt(float t) {
        float angle = t * Mathf.PI2 * (clockwise ? -1f : 1f);
        float sign = clockwise ? 1f : -1f;
        return new Vec2(
            -Mathf.sin(angle) * sign,
             Mathf.cos(angle) * sign
        ).nor();
    }

    @Override
    public float length() {
        return Mathf.PI2 * radius;
    }
}
