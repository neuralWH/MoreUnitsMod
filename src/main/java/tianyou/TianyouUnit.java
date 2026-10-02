package tianyou;

import mindustry.content.Planets;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.weapons.PointDefenseWeapon;

public class TianyouUnit {

    public static UnitType tianyou;

    public static void load() {
        tianyou = new UnitType("tianyou") {{
            // LegsUnit 构造器：会查找 unit-tianyou.png 和 leg-tianyou*.png
            constructor = LegsUnit::create;

            health = 32000f;
            armor = 12f;
            hitSize = 44f;
            itemCapacity = 180;
            speed = 6f / 60f;
            rotateSpeed = 2.2f;
            range = 48f;
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

            weapons.add(new Weapon("tianyou-cannon-laser") {{
                x = 14f; y = -6f;
                reload = 72f;
                mirror = false;
                shootY = 12f;
                recoil = 8f;
                bullet = TianyouBullets.plasmaLaser;
            }});

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

            weapons.add(new Weapon("tianyou-missile") {{
                x = 8f; y = -10f;
                reload = 120f;
                mirror = true;
                shootY = 8f;
                recoil = 4f;
                bullet = TianyouBullets.plasmaMissile;
                shoot = new ShootPattern() {{
                    shots = 4;
                    shotDelay = 6f;
                }};
            }});

            weapons.add(new PointDefenseWeapon("tianyou-pointdefense") {{
                x = -10f; y = 6f;
                reload = 180f;
                mirror = true;
                shootY = 5f;
                recoil = 2f;
                bullet = TianyouBullets.pointDefense;
            }});
        }};
    }
}
