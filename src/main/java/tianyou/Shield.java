package tianyou;

import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.gen.Unit;

public class Shield {

    public int id;
    public Unit owner;
    public float hp = 6000f;
    public ShieldState state = ShieldState.ACTIVE;

    public ShieldPath currentPath;
    public float pathParam;
    public final Vec2 position = new Vec2();
    public final Vec2 tangent = new Vec2();

    private final Vec2 transitionFrom = new Vec2();
    private final Vec2 transitionTo = new Vec2();
    private float transitionProgress = 1f;

    public boolean intercepting = false;
    public Object interceptTarget = null;
    public boolean assigned = false;

    private float timeSinceDamage = 0f;

    public Shield(int id, Unit owner) {
        this.id = id;
        this.owner = owner;
    }

    public void update(float delta) {
        // 17秒无伤害回复
        if (hp < 6000f) {
            timeSinceDamage += delta;
            if (timeSinceDamage >= 17f) {
                hp = 6000f;
                state = ShieldState.ACTIVE;
                timeSinceDamage = 0f;
            }
        } else {
            timeSinceDamage = 0f;
        }

        // 平滑过渡
        if (transitionProgress < 1f) {
            transitionProgress = Math.min(1f, transitionProgress + delta / 0.5f);
            position.set(transitionFrom).lerp(transitionTo, transitionProgress);
        }
    }

    public void startTransition(Vec2 target) {
        if (target == null) return;
        transitionFrom.set(position);
        transitionTo.set(target);
        transitionProgress = 0f;
    }

    public void damage(float amount) {
        if (state == ShieldState.DESTROYED) return;
        hp -= amount;
        timeSinceDamage = 0f;
        if (hp <= 0f) {
            hp = 0f;
            state = ShieldState.DESTROYED;
            endAllTasks();
            CirclePath a = ShieldSystem.getPathA(owner);
            if (a != null) currentPath = a;
        }
    }

    public void endAllTasks() {
        intercepting = false;
        interceptTarget = null;
        assigned = false;
    }

    public void draw() {
        if (state == ShieldState.DESTROYED) return;
        if (currentPath == null) return;
        if (tangent == null) return;

        float angle = Mathf.atan2(tangent.y, tangent.x);
        float curve = 0.2f;

        Lines.stroke(2f);
        Lines.beginLine();
        for (int i = 0; i <= 8; i++) {
            float t = i / 8f;
            float lx = (t - 0.5f) * 6f * 8f;
            float ly = -curve * (1f - (2f * t - 1f) * (2f * t - 1f)) * 8f;
            float wx = position.x + lx * Mathf.cos(angle) - ly * Mathf.sin(angle);
            float wy = position.y + lx * Mathf.sin(angle) + ly * Mathf.cos(angle);
            Lines.linePoint(wx, wy);
        }
        Lines.endLine();
    }

    public boolean collidesWithBullet(float bx, float by) {
        if (state != ShieldState.ACTIVE) return false;
        if (tangent == null) return false;
        float angle = Mathf.atan2(tangent.y, tangent.x);
        float dx = bx - position.x;
        float dy = by - position.y;
        float localX = dx * Mathf.cos(-angle) - dy * Mathf.sin(-angle);
        float localY = dx * Mathf.sin(-angle) + dy * Mathf.cos(-angle);
        return Math.abs(localX) <= 24f && Math.abs(localY) <= 8f;
    }
}
