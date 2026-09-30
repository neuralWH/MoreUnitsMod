package structure;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.world.blocks.units.UnitAssembler;
import tianyou.TianyouUnit;

public class AdvancedUnitFactory extends UnitAssembler {

    public AdvancedUnitFactory(String name) {
        super(name);

        this.size = 5;
        this.health = 3600;
        this.buildTime = 60f * 46.75f;
        this.category = Category.units;

        this.requirements(Category.units, ItemStack.with(
            mindustry.content.Items.silicon, 600,
            mindustry.content.Items.oxide, 1000,
            mindustry.content.Items.thorium, 550,
            mindustry.content.Items.carbide, 200,
            mindustry.content.Items.phaseFabric, 200
        ));

        this.consumePower(180f / 60f);

        // 生产天佑的配方
        this.plans.add(new AssemblerUnitPlan(
            TianyouUnit.tianyou,
            60f * 300f,
            Seq.with(
                new PayloadStack(UnitTypes.merui, 6),
                new PayloadStack(UnitTypes.cleroi, 8),
                new PayloadStack(Blocks.reinforcedSurgeWallLarge, 16),
                new PayloadStack(Blocks.carbideWallLarge, 10)
            )
        ));
    }
}
