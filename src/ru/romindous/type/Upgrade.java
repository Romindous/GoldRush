package ru.romindous.type;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.romindous.game.Arena;
import ru.romindous.game.object.Rusher;

import javax.annotation.Nullable;
import java.util.List;

public enum Upgrade {

    ARMOR_0("Кожанная Бронь", "Первый уровень брони", Material.LEATHER_CHESTPLATE, BuildType.GOLD, 1, null, 10, 0),
    ARMOR_1("Кольчужная Бронь", "Второй уровень брони", Material.CHAINMAIL_CHESTPLATE, BuildType.BARRACK, 1, ARMOR_0, 19, 6),
    ARMOR_2("Алмазная Бронь", "Третий уровень брони", Material.DIAMOND_CHESTPLATE, BuildType.RANGE, 1, ARMOR_1, 28, 12),
    ARMOR_3("Незеритовая Бронь", "Четвертый уровень брони", Material.NETHERITE_CHESTPLATE, BuildType.TRIBUNE, 1, ARMOR_2, 37, 20),
    DAMAGE_0("Тупое Оружие", "Начальный урон оружия", Material.WOODEN_SWORD, BuildType.GOLD, 1, null, 12, 0),
    DAMAGE_1("Обычное Оружие", "Двойной урон оружия", Material.IRON_SWORD, BuildType.BARRACK, 1, DAMAGE_0, 21, 8),
    DAMAGE_2("Заточеное Оружие", "Тройной урон оружия", Material.DIAMOND_SWORD, BuildType.CAMP, 1, DAMAGE_1, 30, 16),
    DAMAGE_3("Остое Оружие", "'Четверной_Урон.mp3' оружия", Material.NETHERITE_SWORD, BuildType.ALTAR, 1, DAMAGE_2, 39, 24),
    MAP("Глобальная Карта", "Следи за своими зданиями!", Material.COMPASS, BuildType.GOLD, 1, null, 16, 0),
    TELEPORT("Телепорт к Зданиям", "Перемещайся по своей карте!", Material.RECOVERY_COMPASS, BuildType.ALTAR, 3, MAP, 25, 16),
    RUN("Прост Бег", "Стандартная скорость бега", Material.IRON_BOOTS, BuildType.GOLD, 1, null, 14, 0),
    SPRINT("Адреналин", "Повышеная скорость бега!", Material.FEATHER, BuildType.UPGRADE, 3, RUN, 23, 16),
    SOLO("PvE Бой", "Атаки по одному существу", Material.CHORUS_FRUIT, BuildType.GOLD, 1, null, 33, 0),
    AOE("Фарту Масти", "Урон оружия по области!", Material.POPPED_CHORUS_FRUIT, BuildType.TRIBUNE, 3, null, 42, 12),
    ;

    public static final String CLR = "§б";

    public final String name;
    public final String desc;
    public final Material icn;
    public final BuildType bt;
    public final byte lvl;
    public final Upgrade lst;
    public final int slot;
    public final int price;

    Upgrade(final String name, final String desc, final Material icn, final BuildType bt,
        final int lvl, final Upgrade lst, final int slot, final int price) {
        this.name = name;
        this.desc = desc;
        this.icn = icn;
        this.bt = bt;
        this.lvl = (byte) lvl;
        this.lst = lst;
        this.slot = slot;
        this.price = price;
    }

    public boolean canBuy(final Rusher rs, final @Nullable List<String> lrs) {
        final boolean prv = lst == null || rs.getUpg(lst) != null;
        if (!prv && lrs != null) lrs.add("§cСначала откройте " + CLR + lst.name);
        final boolean bld = rs.team() != null && rs.team().hasBuild(bt, lvl, lrs);
//        rs.ifPlayer(p -> p.sendMessage("nm-" + name + ", lvl-" + rs.level()));
        return prv && bld && rs.level() >= price;
    }

    public void onGet(final Rusher rs) {
        final LivingEntity le;
        switch (this) {
            case ARMOR_0:
                final Color cl = TCUtil.getBukkitColor(TCUtil.getTextColor(rs.team().cc));
                rs.item(new ItemBuilder(Material.LEATHER_HELMET).name(TCUtil.P + "Шапка")
                    .unbreak(true).color(cl).build(), EquipmentSlot.HEAD);
                rs.item(new ItemBuilder(Material.LEATHER_CHESTPLATE).name(TCUtil.P + "Куртка")
                    .unbreak(true).color(cl).build(), EquipmentSlot.CHEST);
                rs.item(new ItemBuilder(Material.LEATHER_LEGGINGS).name(TCUtil.P + "Штаны")
                    .unbreak(true).color(cl).build(), EquipmentSlot.LEGS);
                rs.item(new ItemBuilder(Material.LEATHER_BOOTS).name(TCUtil.P + "Сапоги")
                    .unbreak(true).color(cl).build(), EquipmentSlot.FEET);
                break;
            case ARMOR_1:
                rs.item(new ItemBuilder(Material.CHAINMAIL_HELMET).name(TCUtil.P + "Шапка")
                    .unbreak(true).build(), EquipmentSlot.HEAD);
                rs.item(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).name(TCUtil.P + "Куртка")
                    .unbreak(true).build(), EquipmentSlot.CHEST);
                rs.item(new ItemBuilder(Material.CHAINMAIL_LEGGINGS).name(TCUtil.P + "Джинсы")
                    .unbreak(true).build(), EquipmentSlot.LEGS);
                rs.item(new ItemBuilder(Material.CHAINMAIL_BOOTS).name(TCUtil.P + "Сапоги")
                    .unbreak(true).build(), EquipmentSlot.FEET);
                break;
            case ARMOR_2:
                rs.item(new ItemBuilder(Material.DIAMOND_HELMET).name(TCUtil.P + "Шапка")
                    .unbreak(true).build(), EquipmentSlot.HEAD);
                rs.item(new ItemBuilder(Material.DIAMOND_CHESTPLATE).name(TCUtil.P + "Куртка")
                    .unbreak(true).build(), EquipmentSlot.CHEST);
                rs.item(new ItemBuilder(Material.DIAMOND_LEGGINGS).name(TCUtil.P + "Штаны")
                    .unbreak(true).build(), EquipmentSlot.LEGS);
                rs.item(new ItemBuilder(Material.DIAMOND_BOOTS).name(TCUtil.P + "Сапоги")
                    .unbreak(true).build(), EquipmentSlot.FEET);
                break;
            case ARMOR_3:
                rs.item(new ItemBuilder(Material.NETHERITE_HELMET).name(TCUtil.P + "Шапка")
                    .unbreak(true).build(), EquipmentSlot.HEAD);
                rs.item(new ItemBuilder(Material.NETHERITE_CHESTPLATE).name(TCUtil.P + "Куртка")
                    .unbreak(true).build(), EquipmentSlot.CHEST);
                rs.item(new ItemBuilder(Material.NETHERITE_LEGGINGS).name(TCUtil.P + "Штаны")
                    .unbreak(true).build(), EquipmentSlot.LEGS);
                rs.item(new ItemBuilder(Material.NETHERITE_BOOTS).name(TCUtil.P + "Сапоги")
                    .unbreak(true).build(), EquipmentSlot.FEET);
                break;
            case DAMAGE_0:
                le = rs.getEntity();
                le.removePotionEffect(PotionEffectType.STRENGTH);
                break;
            case DAMAGE_1:
                le = rs.getEntity();
                le.removePotionEffect(PotionEffectType.STRENGTH);
                le.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,
                    9999999, 0, true, false , false));
                break;
            case DAMAGE_2:
                le = rs.getEntity();
                le.removePotionEffect(PotionEffectType.STRENGTH);
                le.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,
                    9999999, 1, true, false , false));
                break;
            case DAMAGE_3:
                le = rs.getEntity();
                le.removePotionEffect(PotionEffectType.STRENGTH);
                le.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,
                    9999999, 2, true, false , false));
                break;
            case MAP:
                rs.inv().remove(Arena.tpMap.getType());
                rs.item(Arena.map,4);
                break;
            case TELEPORT:
                rs.inv().remove(Arena.map.getType());
                rs.item(Arena.tpMap,4);
                break;
            case RUN:
                le = rs.getEntity();
                le.removePotionEffect(PotionEffectType.SPEED);
                break;
            case SPRINT:
                le = rs.getEntity();
                le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    9999999, 2, true, false , false));
                break;
            case SOLO:
                for (final ItemStack it : rs.inv()) {
                    if (it == null) continue;
                    if (it.getType() == Arena.sword.getType()) {
                        it.removeEnchantment(Enchantment.SWEEPING_EDGE);
                    } else if (it.getType() == Arena.bow.getType()) {
                        it.removeEnchantment(Enchantment.FLAME);
                    }
                }
                break;
            case AOE:
                for (final ItemStack it : rs.inv()) {
                    if (it == null) continue;
                    if (it.getType() == Arena.sword.getType()) {
                        it.addEnchantment(Enchantment.SWEEPING_EDGE, Enchantment.SWEEPING_EDGE.getMaxLevel());
                    } else if (it.getType() == Arena.bow.getType()) {
                        it.addEnchantment(Enchantment.FLAME, Enchantment.FLAME.getMaxLevel());
                    }
                }
                break;
        }
    }

    public boolean addFor(final Rusher rs) {
        final boolean add = rs.addUpg(this);
        if (add) onGet(rs);
        return add;
    }

    public boolean remFor(final Rusher rs) {
        if (this.lst == null) return false;
        final Boolean rem = rs.getUpg(this);
        if (rem == null || !rem) return false;
        lst.onGet(rs);
        return rs.remUpg(this);
    }
}
