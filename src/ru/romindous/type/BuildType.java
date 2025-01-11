package ru.romindous.type;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import ru.komiss77.modules.world.Schematic;
import ru.romindous.game.goal.*;
import ru.romindous.game.object.Nexus;

import java.io.File;
import java.util.EnumMap;

public enum BuildType {
	
	NEXUS(	"Нексус", 	"Дает больше золота и возраждает при смерти!",100, 	16, 	200, 	60,	2, false),
	GOLD(	"Рудник", 	"Добывает золото на местах с его рудной!", 	60, 	16, 	80, 	0,	1, false),
	DUST(	"Шахта", 	"Добывает пыль на местах с ее залежами!", 	40, 	12, 	40, 	0,	1, false),
	BARRACK("Бараки", 	"Производит стандартных солдат рассы!", 		50, 	20, 	100, 	20,	1, true),
	RANGE(	"Полигон", "Производит солдат дальнего боя рассы!", 		40, 	16, 	180, 	120,	2, true),//ALTAR
	ALTAR(	"Алтарь", 	"Производит магических солдат рассы!", 		50, 	24, 	320, 	240,	3, true),//CAMP
	CAMP(	"Лагерь", 	"Производит солдат-убийц рассы!", 			40, 	20, 	240, 	100,	2, true),//TRIBUNE
	TRIBUNE("Трибуны", "Производит крупных солдат рассы!", 			80, 	22, 	400, 	160,	3, true),//RANGE
	UPGRADE("Кузня", 	"Улучшает характеристики армии!", 			60, 	60, 	280, 	240,	1, false),
	SPIRE(	"Шпиль", 	"Статическая оборона территории!", 			20, 	1, 	160, 	80,	2, false),
	;

	public final int hp;
	public final int cld;
	public final int gold;
	public final int dust;
	public final byte tier;
	public final String nm;
	public final String desc;
	public final boolean close;

	public static final int maxLvl = 3;
	public static final int maxXZ = 4;
	public static final int maxY = 10;
	public static final int dsLoc = 2;
	public static final BlockType gSite = BlockType.RAW_GOLD_BLOCK;
	public static final BlockType dSite = BlockType.DEEPSLATE_REDSTONE_ORE;
	public static final EnumMap<BuildType, Schematic[]> schems = getSchems();
	public static final Schematic SHOP = new Schematic(Bukkit.getConsoleSender(),
		new File(Bukkit.getPluginsFolder().getAbsolutePath() + "/Ostrov/schematics/SHOP.schem"), false);

	private static EnumMap<BuildType, Schematic[]> getSchems() {
		final EnumMap<BuildType, Schematic[]> schms = new EnumMap<>(BuildType.class);
		final File dir = new File(Bukkit.getPluginsFolder().getAbsolutePath() + "/Ostrov/schematics");
		final BuildType[] bts = BuildType.values();
		final RaceType[] rts = RaceType.values();
		if (dir.exists()) {
			final CommandSender cmd = Bukkit.getConsoleSender();
			cmd.sendMessage("Loading schems:");
			cnt : for (final File fl : dir.listFiles()) {
				final int dot = fl.getName().lastIndexOf('.');
				if (dot == -1) continue;
				final String fnm = fl.getName().substring(0, dot);
				if (fnm.length() < 3) continue;
				for (final RaceType rt : rts) {//RACE|BUILD|LVL
					if (rt.name().charAt(0) != fnm.charAt(0)) continue;
					for (final BuildType bt : bts) {
						if (bt.name().charAt(0) != fnm.charAt(1)) continue;
						final int i = fnm.charAt(fnm.length() - 1) - 48;
						if (i < 1 || i > maxLvl) continue cnt;
						final Schematic sch = new Schematic(cmd, fl, false);
						cmd.sendMessage("Loaded sch " + bt.nm + " lvl " + i);
						if (schms.containsKey(bt)) {
							schms.get(bt)[rt.ordinal() * maxLvl + i - 1] = sch;
						} else {
							final Schematic[] ns = new Schematic[rts.length * maxLvl];
							ns[rt.ordinal() * maxLvl + i - 1] = sch;
							schms.put(bt, ns);
						}
						continue cnt;
					}
				}
			}
		} else {
			for (final BuildType bt : bts) {
				final Schematic[] ns = new Schematic[maxLvl];
				schms.put(bt, ns);
			}
		}
		return schms;
	}
	
	BuildType(final String nm, final String desc, final int hp, final int cld,
		  final int gold, final int dust, final int tier, final boolean close) {
		this.nm = nm;
		this.desc = desc;
		this.hp = hp;
		this.cld = cld;
		this.gold = gold;
		this.dust = dust;
		this.tier = (byte) tier;
        this.close = close;
    }
	
	public Schematic getSchem(final RaceType race, final int lvl) {
		return schems.get(this)[race.ordinal() * maxLvl + lvl - 1];
	}

	public Material getIcon(final int lvl) {
        return switch (lvl) {
            case 3 -> switch (this) {
                case NEXUS -> Material.REINFORCED_DEEPSLATE;
                case GOLD -> Material.RAW_GOLD_BLOCK;
                case DUST -> Material.REDSTONE_BLOCK;
                case CAMP -> Material.NETHERITE_SWORD;
                case BARRACK -> Material.IRON_CHESTPLATE;
                case ALTAR -> Material.HEART_OF_THE_SEA;
                case RANGE -> Material.CROSSBOW;
                case TRIBUNE -> Material.STICKY_PISTON;
                case UPGRADE -> Material.BLAST_FURNACE;
				case SPIRE -> Material.LODESTONE;
            };
            case 2 -> switch (this) {
                case NEXUS -> Material.BEACON;
                case GOLD -> Material.DEEPSLATE_GOLD_ORE;
                case DUST -> Material.DEEPSLATE_REDSTONE_ORE;
                case CAMP -> Material.IRON_AXE;
                case BARRACK -> Material.CHAINMAIL_CHESTPLATE;
                case ALTAR -> Material.ENDER_EYE;
                case RANGE -> Material.TIPPED_ARROW;
                case TRIBUNE -> Material.PISTON;
                case UPGRADE -> Material.ANVIL;
				case SPIRE -> Material.LODESTONE;
            };
            case 1 -> switch (this) {
                case NEXUS -> Material.CONDUIT;
                case GOLD -> Material.GOLD_ORE;
                case DUST -> Material.REDSTONE_ORE;
                case CAMP -> Material.WOODEN_SHOVEL;
                case BARRACK -> Material.LEATHER_CHESTPLATE;
                case ALTAR -> Material.ENDER_PEARL;
                case RANGE -> Material.ARROW;
                case TRIBUNE -> Material.DISPENSER;
                case UPGRADE -> Material.STONECUTTER;
				case SPIRE -> Material.LODESTONE;
            };
            default -> Material.GRAY_DYE;
        };
    }

	public String getProd(final RaceType rc) {
		return switch (this) {
		case NEXUS -> "Респавн";
		case GOLD -> "Золото";
		case DUST -> "Пыль";
		case BARRACK -> switch (rc) {
			case ILLAGER -> "Поборники";
			case MONSTER -> "Зомби";
			case PIGLIN -> "Пиглины";
		};
		case RANGE -> switch (rc) {
			case ILLAGER -> "Разбойники";
			case MONSTER -> "Зимогоры";
			case PIGLIN -> "Стрельцы";
		};
		case ALTAR -> switch (rc) {
			case ILLAGER -> "Чародеи";
			case MONSTER -> "Эндеры";
			case PIGLIN -> "Элементалы";
		};
		case CAMP -> switch (rc) {
			case ILLAGER -> "Иллюзоры";
			case MONSTER -> "Пауки";
			case PIGLIN -> "Брутники";
		};
		case TRIBUNE -> switch (rc) {
			case ILLAGER -> "Разорители";
			case MONSTER -> "Криперы";
			case PIGLIN -> "Хоглины";
		};
		case UPGRADE -> "Прокачка";
		case SPIRE -> "Оборона";};
	}
	
	public MobGoal goal(final Mob mb, final Nexus nx, final int hp, final float ar,
		final float spd, final float dmg, final float cd, final float kb) {
		return switch (this) {
		case BARRACK -> new KnightGoal(mb, nx, hp, ar, spd, dmg, cd, kb);
		case RANGE -> new RangerGoal(mb, nx, hp, ar, spd, dmg, cd, kb);
		case ALTAR -> new MaegusGoal(mb, nx, hp, ar, spd, dmg, cd, kb);
		case CAMP -> new KillerGoal(mb, nx, hp, ar, spd, dmg, cd, kb);
		case TRIBUNE -> new SiegerGoal(mb, nx, hp, ar, spd, dmg, cd, kb);
		default -> null;};
	}

	public static BuildType getTypeFor(final EntityType tp) {
		return switch (tp) {
			case VINDICATOR, ZOMBIE_VILLAGER, PIGLIN -> BARRACK;
			case PILLAGER, STRAY -> RANGE;
			case EVOKER, WITCH, BLAZE, ENDERMAN -> ALTAR;
			case ILLUSIONER, SPIDER, PIGLIN_BRUTE -> CAMP;
			case RAVAGER, CREEPER, HOGLIN -> TRIBUNE;
            default -> BARRACK;
        };
	}
}
