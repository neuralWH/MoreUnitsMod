package tianyou;

import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
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

import java.io.InputStream;

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
            InputStream is = getClass().getClassLoader()
                .getResourceAsStream("assets/sprites/" + fileName);
            if (is == null) {
                Log.err("Sprite not found: assets/sprites/" + fileName);
                return;
            }
            Pixmap pixmap = PixmapIO.readPNG(is);
            packer.add(MultiPacker.PageType.main, regionName, pixmap);
            // 不手动 dispose，交给 MultiPacker 统一释放
        } catch (Exception e) {
            Log.err("Failed to load sprite: " + fileName, e);
        }
    }
}
