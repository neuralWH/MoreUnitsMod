package tianyou;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.bullet.*;

public class TianyouBullets {

    public static BulletType plasmaLaser;
    public static BulletType plasmaMissile;
    public static BulletType pointDefense;

    public static void load() {
        // 穿透性等离子激光（主炮）：1400伤，穿透3层
        plasmaLaser = new LaserBulletType(1400f) {{
            length = 48f;
            width = 14f;
            lifetime = 30f;
            pierce = true;
            pierceCap = 3;
            hitEffect = Fx.hitLaserBlast;
            colors = new Color[]{
                Color.valueOf("ffd7a0"),
                Color.valueOf("ffb054"),
                Color.valueOf("ffd7a0")
            };
        }};

        // 等离子追踪导弹（副炮）：800伤，3格直径范围
        plasmaMissile = new MissileBulletType() {{
            speed = 6f;
            damage = 800f;
            lifetime = 90f;
            homingPower = 0.15f;
            homingRange = 200f;
            splashDamage = 800f;
            splashDamageRadius = 12f;   // 3格直径
            hitEffect = Fx.blastExplosion;
            trailEffect = Fx.missileTrail;
            trailParam = 4f;
            weaveScale = 6f;
            weaveMag = 2f;
            frontColor = Color.valueOf("ffd7a0");
            backColor = Color.valueOf("ffb054");
        }};

        // 点防御子弹：伤害40，击毁敌方炮弹
        pointDefense = new BasicBulletType() {{
            speed = 16f;
            damage = 40f;
            lifetime = 20f;
            width = 6f;
            height = 6f;
            maxRange = 48f;
            hitEffect = Fx.hitLaserBlast;
        }};
    }
}
