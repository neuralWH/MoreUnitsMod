package tianyou;

import arc.Core;
import arc.Events;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.AtlasRegion;
import arc.util.Log;
import mindustry.content.Planets;
import mindustry.content.UnitTypes;
import mindustry.entities.pattern.ShootPattern;
import mindustry.game.EventType.ContentInitEvent;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.weapons.PointDefenseWeapon;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TianyouUnit {

    public static UnitType tianyou;

    public static void load() {
        tianyou = new UnitType("tianyou") {{
            // 使用天帝的构造器，继承 AssemblerAI 和 BuildingTetherComp
            constructor = UnitTypes.collaris.constructor;

            health = 32000f;
            armor = 12f;
            hitSize = 44f;              // 5.5格，与天帝一致
            itemCapacity = 180;
            speed = 6f / 60f;           // 6格/秒
            rotateSpeed = 2.2f;
            range = 48f;                // 48世界单位
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

        // 内容加载完成后，手动注册贴图到 atlas
        Events.on(ContentInitEvent.class, e -> {
            registerSprite("unit-tianyou", "sprites/unit-tianyou.png");
            registerSprite("leg-tianyou", "sprites/leg-tianyou.png");
            registerSprite("leg-tianyou-base", "sprites/leg-tianyou-base.png");
            registerSprite("weapon-tianyou-cannon-laser", "sprites/weapon-tianyou-cannon-laser.png");
            registerSprite("weapon-tianyou-cannon-homing", "sprites/weapon-tianyou-cannon-homing.png");
            registerSprite("weapon-tianyou-missile", "sprites/weapon-tianyou-missile.png");
            registerSprite("weapon-tianyou-pointdefense", "sprites/weapon-tianyou-pointdefense.png");

            tianyou.region = Core.atlas.find("unit-tianyou");
            tianyou.legRegion = Core.atlas.find("leg-tianyou");
            tianyou.baseRegion = Core.atlas.find("leg-tianyou-base");
        });
    }

    private static void registerSprite(String regionName, String path) {
        if (Core.atlas.has(regionName)) return;
        try {
            // 从 classpath 读取 jar 内的贴图文件
            InputStream is = TianyouUnit.class.getClassLoader()
                .getResourceAsStream("assets/" + path);
            if (is == null) {
                Log.err("Sprite not found in classpath: assets/" + path);
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            is.close();
            byte[] data = baos.toByteArray();

            // 从字节数组创建 Pixmap
            Pixmap pixmap = new Pixmap(data);
            Texture tex = new Texture(pixmap);
            pixmap.dispose();

            // 创建 AtlasRegion 并注册到 atlas
            AtlasRegion region = new AtlasRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
            region.name = regionName;
            Core.atlas.addRegion(region);
        } catch (Exception ex) {
            Log.err("Failed to load sprite: " + path, ex);
        }
    }
}
