package tianyou;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.content.TechTree;
import mindustry.content.UnitTypes;
import mindustry.graphics.MultiPacker;
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

        // 向原版机甲组装厂添加天佑配方
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

    // 将自定义贴图打包进游戏图集
    @Override
    public void packSprites(MultiPacker packer) {
        addSprite(packer, "unit-tianyou", "unit-tianyou.png");
        addSprite(packer, "leg-tianyou", "leg-tianyou.png");
        addSprite(packer, "leg-tianyou-base", "leg-tianyou-base.png");
        addSprite(packer, "weapon-tianyou-cannon-laser", "weapon-tianyou-cannon-laser.png");
        addSprite(packer, "weapon-tianyou-cannon-homing", "weapon-tianyou-cannon-homing.png");
        addSprite(packer, "weapon-tianyou-missile", "weapon-tianyou-missile.png");
        addSprite(packer, "weapon-tianyou-pointdefense", "weapon-tianyou-pointdefense.png");
    }

    private void addSprite(MultiPacker packer, String regionName, String fileName) {
        try {
            // 从 jar 内读取贴图文件
            Fi file = Core.files.internal("assets/sprites/" + fileName);
            if (!file.exists()) {
                Log.err("Sprite not found: " + file.path());
                return;
            }
            // 用 Pixmap 读取 PNG
            Pixmap pixmap = new Pixmap(file);
            // 交给 MultiPacker 管理，不要手动 dispose
            packer.add(MultiPacker.PageType.main, regionName, pixmap);
        } catch (Exception e) {
            Log.err("Failed to load sprite: " + fileName, e);
        }
    }
}
