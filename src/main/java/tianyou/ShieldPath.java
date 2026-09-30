package tianyou;

import arc.math.geom.Vec2;

public abstract class ShieldPath {
    public enum Type { A, B, C, D }

    public Type type;
    public boolean clockwise;

    public abstract Vec2 pointAt(float t);
    public abstract Vec2 tangentAt(float t);
    public abstract float length();

    public int capacity() {
        return (int) Math.floor((length() / 8f) / 12f);
    }
}
