package tianyou;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

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
    public float transitionProgress = 1f;

    public boolean intercepting = false;
    public Object interceptTarget = null;
    public boolean assigned = false;

    private float timeSinceDamage = 0f;

    public Shield(int id, Unit owner) {
        this.id = id;
        this.owner = owner;
    }

    public void update(float delta) {
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

        TextureRegion region = Core.atlas.find("tecta-shield");
        if (!region.found()) return;

        float angle = Mathf.atan2(tangent.y, tangent.x) * Mathf.radDeg;
        float w = region.width * Draw.scl;
        float h = region.height * Draw.scl;

        Draw.z(Layer.shields);
        Draw.rect(region, position.x, position.y, w, h, angle);
        Draw.reset();
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
