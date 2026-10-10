package tianyou;

import mindustry.content.UnitTypes;
import mindustry.mod.Mod;

public class TianyouMod extends Mod {

    public TianyouMod() {
    }

    @Override
    public void loadContent() {
        // 只需要加载护盾系统的初始化
    }

    @Override
    public void init() {
        ShieldSystem.init();
        InterceptSystem.init();

        // 把护盾能力加到天帝身上
        UnitTypes.collaris.abilities.add(new SharedShieldAbility());
    }
}
