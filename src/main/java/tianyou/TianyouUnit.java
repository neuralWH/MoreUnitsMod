package tianyou;

import mindustry.content.Planets;
import mindustry.content.UnitTypes;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.weapons.PointDefenseWeapon;

public class TianyouUnit {

    public static UnitType tianyou;

    public static void load() {
        tianyou = new UnitType("tianyou") {{
            // 直接使用天帝的实体构造器，继承其 AssemblerAI 和 BuildingTetherComp
            constructor = UnitTypes.collaris.constructor;

            health = 32000f;
            armor = 12f;
            hitSize = 44f;              // 与天帝一致，约 5.5 格直径
            itemCapacity = 180;
            speed = 6f / 60f;           // 6 格/秒
            rotateSpeed = 2.2f;
            range = 48f;                // 48 世界单位
            targetAir = true;
            targetGround = true;
            flying = false;
            canBoost = false;
            canDrown = false;
            allowedInPayloads = true;

            shownPlanets.add(Planets.erekir);

            localizedName = "@unit.tianyou.name";
            description = "@unit.tianyou.description";

            abilities.add(new SharedShieldAbility());

            // 主炮：穿透性等离子激光（1束）
            weapons.add(new Weapon("tianyou-cannon-laser") {{
                x = 14f; y = -6f;
                reload = 72f;          // 1.2秒
                mirror = false;
                shootY = 12f;
                recoil = 8f;
                bullet = TianyouBullets.plasmaLaser;
            }});

            // 主炮副发射：6颗等离子追踪炮弹
            weapons.add(new Weapon("tianyou-cannon-homing") {{
                x = 14f; y = -6f;
                reload = 72f;
                mirror = false;
                shootY = 12f;
                recoil = 4f;
                bullet = TianyouBullets.plasmaHoming;
                shoot = new ShootPattern() {{
                    shots = 6;
                    shotDelay = 4f;
                }};
            }});

            // 副炮：4发等离子追踪导弹
            weapons.add(new Weapon("tianyou-missile") {{
                x = 8f; y = -10f;
                reload = 120f;         // 2秒
                mirror = true;
                shootY = 8f;
                recoil = 4f;
                bullet = TianyouBullets.plasmaMissile;
                shoot = new ShootPattern() {{
                    shots = 4;
                    shotDelay = 6f;
                }};
            }});

            // 点防御炮台
            weapons.add(new PointDefenseWeapon("tianyou-pointdefense") {{
                x = -10f; y = 6f;
                reload = 180f;         // 3秒
                mirror = true;
                shootY = 5f;
                recoil = 2f;
                bullet = TianyouBullets.pointDefense;
            }});
        }};
    }
}
