package tianyou;

import mindustry.content.Blocks;
import mindustry.content.TechTree;
import mindustry.content.UnitTypes;
import mindustry.mod.Mod;
import mindustry.type.ItemStack;
import structure.AdvancedUnitFactory;

public class TianyouMod extends Mod {

    public static AdvancedUnitFactory advancedUnitFactory;

    public TianyouMod() {
    }

    @Override
    public void loadContent() {
        // 加载子弹、单位、高级单位组装厂
        TianyouBullets.load();
        TianyouUnit.load();
        advancedUnitFactory = new AdvancedUnitFactory("tianyou-assembler");
    }

    @Override
    public void init() {
        // 初始化护盾系统和拦截系统
        ShieldSystem.init();
        InterceptSystem.init();

        // 将天佑单位挂到天帝节点下方
        TechTree.TechNode unitParent = UnitTypes.collaris.techNode;
        if (unitParent != null) {
            new TechTree.TechNode(unitParent, TianyouUnit.tianyou, new ItemStack[0]);
        }

        // 将高级单位组装厂挂到原版机甲组装厂下方
        TechTree.TechNode assemblerParent = Blocks.mechAssembler.techNode;
        if (assemblerParent != null) {
            new TechTree.TechNode(assemblerParent, advancedUnitFactory, new ItemStack[0]);
        }
    }
}
