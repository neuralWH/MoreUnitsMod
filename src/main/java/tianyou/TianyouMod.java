package tianyou;

import mindustry.content.Blocks;
import mindustry.content.TechTree;
import mindustry.content.UnitTypes;
import mindustry.mod.Mod;
import mindustry.type.ItemStack;
import structure.AdvancedAssemblerModule;
import structure.AdvancedUnitFactory;

public class TianyouMod extends Mod {

    public static AdvancedUnitFactory advancedUnitFactory;
    public static AdvancedAssemblerModule advancedAssemblerModule;

    public TianyouMod() {
    }

    @Override
    public void loadContent() {
        TianyouBullets.load();
        TianyouUnit.load();
        advancedUnitFactory = new AdvancedUnitFactory("tianyou-assembler");
        advancedAssemblerModule = new AdvancedAssemblerModule("advanced-assembler-module");
    }

    @Override
    public void init() {
        ShieldSystem.init();
        InterceptSystem.init();

        // 天佑挂到天帝节点下方
        TechTree.TechNode unitParent = UnitTypes.collaris.techNode;
        if (unitParent != null) {
            new TechTree.TechNode(unitParent, TianyouUnit.tianyou, new ItemStack[0]);
        }

        // 高级单位组装厂挂到原版机甲组装厂下方
        TechTree.TechNode assemblerParent = Blocks.mechAssembler.techNode;
        if (assemblerParent != null) {
            new TechTree.TechNode(assemblerParent, advancedUnitFactory, new ItemStack[0]);
        }

        // 高级组装模块挂到原版基础组装模块下方
        TechTree.TechNode moduleParent = Blocks.basicAssemblerModule.techNode;
        if (moduleParent != null) {
            new TechTree.TechNode(moduleParent, advancedAssemblerModule, new ItemStack[0]);
        }
    }
}
