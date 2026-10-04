package tianyou;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.content.TechTree;
import mindustry.content.UnitTypes;
import mindustry.mod.Mod;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.blocks.units.UnitAssembler.AssemblerUnitPlan;
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

        TechTree.TechNode unitParent = UnitTypes.collaris.techNode;
        if (unitParent != null) {
            new TechTree.TechNode(unitParent, TianyouUnit.tianyou, new ItemStack[0]);
        }

        TechTree.TechNode assemblerParent = Blocks.mechAssembler.techNode;
        if (assemblerParent != null) {
            new TechTree.TechNode(assemblerParent, advancedUnitFactory, new ItemStack[0]);
        }

        TechTree.TechNode moduleParent = Blocks.basicAssemblerModule.techNode;
        if (moduleParent != null) {
            new TechTree.TechNode(moduleParent, advancedAssemblerModule, new ItemStack[0]);
        }

        if (Blocks.mechAssembler instanceof UnitAssembler assembler) {
            assembler.plans.add(new AssemblerUnitPlan(
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

    // 关键：重写 packSprites 方法，将自定义贴图打包进游戏图集
    @Override
    public void packSprites(MultiPacker packer) {
        packer.add(MultiPacker.PageType.main, "unit-tianyou", "sprites/unit-tianyou.png");
        packer.add(MultiPacker.PageType.main, "leg-tianyou", "sprites/leg-tianyou.png");
        packer.add(MultiPacker.PageType.main, "leg-tianyou-base", "sprites/leg-tianyou-base.png");
        packer.add(MultiPacker.PageType.main, "weapon-tianyou-cannon-laser", "sprites/weapon-tianyou-cannon-laser.png");
        packer.add(MultiPacker.PageType.main, "weapon-tianyou-cannon-homing", "sprites/weapon-tianyou-cannon-homing.png");
        packer.add(MultiPacker.PageType.main, "weapon-tianyou-missile", "sprites/weapon-tianyou-missile.png");
        packer.add(MultiPacker.PageType.main, "weapon-tianyou-pointdefense", "sprites/weapon-tianyou-pointdefense.png");
    }
}
