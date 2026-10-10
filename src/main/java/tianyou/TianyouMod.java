package tianyou;

import mindustry.content.UnitTypes;
import mindustry.mod.Mod;

public class TianyouMod extends Mod {

    public TianyouMod() {
    }

    @Override
    public void loadContent() {
        // 在内容加载阶段把护盾能力加到天帝身上
        UnitTypes.collaris.abilities.add(new SharedShieldAbility());
    }

    @Override
    public void init() {
        ShieldSystem.init();
        InterceptSystem.init();
    }
}
