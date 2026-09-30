package tianyou;

import arc.graphics.g2d.Draw;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

public class SharedShieldAbility extends Ability {

    public static final float VISION_RADIUS = 28f * 8f;
    public static final float CONNECT_DIST = 24f * 8f;
    public static final float CLUSTER_RADIUS = 12f * 8f;

    @Override
    public void update(Unit unit) {
    }

    @Override
    public void draw(Unit unit) {
        if (unit == null) return;
        if (unit.type != TianyouUnit.tianyou) return;
        if (!unit.isValid()) return;

        try {
            Draw.z(Layer.shields);
            Draw.color(Pal.shield);

            ShieldCluster cluster = ShieldSystem.unitToCluster.get(unit);
            if (cluster != null) {
                cluster.drawShields();
            } else {
                ShieldSystem.drawSingleUnitShields(unit);
            }

            Draw.reset();
        } catch (Exception e) {
            // 忽略绘制异常
        }
    }
}
