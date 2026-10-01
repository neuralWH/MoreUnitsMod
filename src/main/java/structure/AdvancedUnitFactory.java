package structure;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.world.blocks.units.UnitAssembler;
import tianyou.TianyouUnit;

public class AdvancedUnitFactory extends UnitAssembler {

    public AdvancedUnitFactory(String name) {
        super(name);

        // 套用原版机甲组装厂外观与属性
        this.size = 5;
        this.health = 3600;
        this.buildTime = 60f * 46.75f;
        this.itemCapacity = 10;
        this.liquidCapacity = 120;
        this.category = Category.units;

        // 关键：确保载荷源能显示大型单位（6格单位 = 48世界单位）
        this.clipSize = 120;

        // 建造花费
        this.requirements(Category.units, ItemStack.with(
            Items.silicon, 600,
            Items.oxide, 1000,
            Items.thorium, 550,
            Items.carbide, 200,
            Items.phaseFabric, 200
        ));

        // 耗电与耗液体
        this.consumePower(180f / 60f);
        this.consumeLiquid(Liquids.ozone, 12f / 60f);

        // 只生产天佑
        this.plans.add(new AssemblerUnitPlan(
            TianyouUnit.tianyou,
            60f * 300f,   // 300秒
            Seq.with(
                new PayloadStack(UnitTypes.merui, 6),
                new PayloadStack(UnitTypes.cleroi, 8),
                new PayloadStack(Blocks.reinforcedSurgeWallLarge, 16),
                new PayloadStack(Blocks.carbideWallLarge, 10)
            )
        ));
    }
}
