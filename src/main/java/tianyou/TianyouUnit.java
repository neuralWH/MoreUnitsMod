package tianyou;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.weapons.PointDefenseWeapon;

public class TianyouUnit {

    public static UnitType tianyou;

    public static void load() {
        tianyou = new UnitType("tianyou") {{
            constructor = MechTianyouUnit::create;
            health = 32000f;
            armor = 12f;
            hitSize = 3f;             // 6格直径
            itemCapacity = 180;
            speed = 6f / 60f;         // 6格/秒
            rotateSpeed = 2.2f;
            range = 48f;              // 48世界单位
            targetAir = true;
            targetGround = true;
            flying = false;
            canBoost = false;
            canDrown = false;
            mechStepParticles = true;

            localizedName = "@unit.tianyou.name";
            description = "@unit.tianyou.description";

            abilities.add(new SharedShieldAbility());

            // 主炮：穿透性等离子激光
            weapons.add(new Weapon("tianyou-cannon") {{
                x = 14f; y = -6f;
                reload = 72f;         // 1.2秒
                mirror = false;
                shootY = 12f;
                recoil = 8f;
                bullet = TianyouBullets.plasmaLaser;
            }});

            // 副炮：等离子追踪导弹
            weapons.add(new Weapon("tianyou-missile") {{
                x = 8f; y = -10f;
                reload = 120f;        // 2秒
                mirror = true;
                shootY = 8f;
                recoil = 4f;
                bullet = TianyouBullets.plasmaMissile;
            }});

            // 点防御炮台
            weapons.add(new PointDefenseWeapon("tianyou-pointdefense") {{
                x = -10f; y = 6f;
                reload = 180f;        // 3秒
                mirror = true;
                shootY = 5f;
                recoil = 2f;
                bullet = TianyouBullets.pointDefense;
            }});
        }};
    }
}
