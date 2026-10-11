package tianyou;

import arc.graphics.g2d.Draw;
import arc.struct.Seq;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

public class SharedShieldAbility extends Ability {

    public static final float VISION_RADIUS = 40f * 8f;
    public static final float CONNECT_DIST = 24f * 8f;
    public static final float CLUSTER_RADIUS = 12f * 8f;

    @Override
    public void update(Unit unit) {
    }

    @Override
    public void draw(Unit unit) {
        if (unit == null) return;
        if (unit.type != UnitTypes.collaris) return;
        if (!unit.isValid()) return;

        try {
            Draw.z(Layer.shields);
            Draw.color(Pal.shield);

            Seq<Shield> shields = ShieldSystem.getShields(unit);
            if (shields != null) {
                for (Shield s : shields) {
                    if (s == null) continue;
                    if (s.state != ShieldState.ACTIVE) continue;
                    if (s.currentPath == null) continue;
                    s.draw();
                }
            }

            Draw.reset();
        } catch (Exception e) {
        }
    }
}
