package structure;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.units.UnitAssemblerModule;

public class AdvancedAssemblerModule extends UnitAssemblerModule {

    public AdvancedAssemblerModule(String name) {
        super(name);

        this.tier = 6;
        this.size = 3;
        this.health = 1200;
        this.buildTime = 60f * 20f;
        this.category = Category.units;

        this.requirements(Category.units, ItemStack.with(
            Items.silicon, 200,
            Items.thorium, 150,
            Items.oxide, 100
        ));

        this.consumePower(60f / 60f);
    }
}
