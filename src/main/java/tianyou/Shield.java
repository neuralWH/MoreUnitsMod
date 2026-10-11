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
    public float targetPathParam;
    public final Vec2 position = new Vec2();
    public final Vec2 tangent = new Vec2();

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

        // 不在拦截时，平滑调整 pathParam，并每帧从路径计算 position
        if (!intercepting && currentPath != null) {
            float diff = targetPathParam - pathParam;
            if (diff > 0.5f) diff -= 1f;
            if (diff < -0.5f) diff += 1f;

            float speed = delta / 0.5f;
            if (Math.abs(diff) > speed) {
                pathParam = (pathParam + Mathf.sign(diff) * speed + 1f) % 1f;
            } else {
                pathParam = targetPathParam;
            }

            position.set(currentPath.pointAt(pathParam));
            tangent.set(currentPath.tangentAt(pathParam));
        }
    }

    public void startTransition(Vec2 target) {
        // 保留空实现，兼容旧调用
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

        // 护盾尺寸：长6格、宽2格（1格=8世界单位）
        float w = 6f * 8f;
        float h = 2f * 8f;

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
