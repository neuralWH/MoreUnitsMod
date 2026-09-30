package structure;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.units.UnitFactory;
import tianyou.TianyouUnit;

public class AdvancedUnitFactory extends UnitFactory {

    public AdvancedUnitFactory(String name) {
        super(name);

        this.size = 3;
        this.health = 600;
        this.itemCapacity = 10;
        this.buildTime = 60f * 5f;
        this.category = Category.units;

        this.requirements(Category.units, ItemStack.with(
            Items.silicon, 100,
            Items.lead, 80
        ));

        this.consumePower(1.2f);

        this.plans.add(new UnitFactory.UnitPlan(
            TianyouUnit.tianyou,
            60f * 15f,
            ItemStack.with(
                Items.silicon, 10,
                Items.lead, 10
            )
        ));
    }
}
