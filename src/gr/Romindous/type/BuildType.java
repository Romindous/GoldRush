package gr.Romindous.type;

import java.io.File;
import java.util.EnumMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import gr.Romindous.Main;
import ru.komiss77.modules.world.Schematic;

public enum BuildType {
	
	NEXUS(	"Нексус", 	100, 	16, 	200, 	60),
	GOLD(	"Рудник", 	60, 	16, 	80, 	0),
	DUST(	"Шахта", 	40, 	12, 	40, 	0),
	KNIGHT(	"Бараки", 	50, 	20, 	100, 	20),
	RANGER(	"Полигон", 	40, 	16, 	180, 	120),//MAEGUS
	MAEGUS(	"Алтарь", 	50, 	24, 	320, 	240),//KILLER
	KILLER(	"Лагерь", 	40, 	20, 	240, 	100),//SIEGER
	SIEGER(	"Трибуны", 	80, 	22, 	400, 	160),//RANGER
	UPGRADE("Кузня", 	60, 	60, 	280, 	240),
	;
	
	public final int hp;
	public final int cld;
	public final int gold;
	public final int dust;
	public final String nm;
	
	public static final int maxLvl = 3;
	public static final int maxXZ = 4;
	public static final int maxY = 8;
	public static final EnumMap<BuildType, Schematic[]> schems = getSchems();

	private static EnumMap<BuildType, Schematic[]> getSchems() {
		final EnumMap<BuildType, Schematic[]> schms = new EnumMap<>(BuildType.class);
		final File dir = new File(Main.plug.getDataFolder().getAbsolutePath() + "/schems/");
		final BuildType[] bts = BuildType.values();
		if (dir.exists()) {
			Bukkit.getConsoleSender().sendMessage("Loading schems:");
			final CommandSender cmd = Bukkit.getConsoleSender();
			cnt : for (final File fl : dir.listFiles()) {
				final String fnm = fl.getName().substring(0, fl.getName().lastIndexOf('.'));
				for (final BuildType bt : bts) {
					if (fnm.startsWith(bt.name().toLowerCase())) {
						final int i = fnm.charAt(fnm.length() - 1) - 48;
						if (i < 1 || i > maxLvl) continue cnt;
						final Schematic sch = new Schematic(cmd, fl, false);
						if (sch != null) {
							Bukkit.getConsoleSender().sendMessage("Loaded sch " + bt.nm + " lvl " + i);
						}
						if (schms.containsKey(bt)) {
							schms.get(bt)[i - 1] = sch;
						} else {
							final Schematic[] ns = new Schematic[maxLvl];
							ns[i - 1] = sch;
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
	
	private BuildType(final String nm, final int hp, final int cld, final int gold, final int dust) {
		this.nm = nm;
		this.hp = hp;
		this.cld = cld;
		this.gold = gold;
		this.dust = dust;
	}
	
	public Schematic getSchem(final int lvl) {
		return schems.get(this)[lvl - 1];
	}

	public Material getIcon(final byte lvl) {
		switch (lvl) {
		case 3:
			switch (this) {
			case NEXUS:
				return Material.REINFORCED_DEEPSLATE;
			case GOLD:
				return Material.RAW_GOLD_BLOCK;
			case DUST:
				return Material.REDSTONE_BLOCK;
			case KILLER:
				return Material.NETHERITE_SWORD;
			case KNIGHT:
				return Material.IRON_CHESTPLATE;
			case MAEGUS:
				return Material.HEART_OF_THE_SEA;
			case RANGER:
				return Material.CROSSBOW;
			case SIEGER:
				return Material.STICKY_PISTON;
			case UPGRADE:
				return Material.BLAST_FURNACE;
			}
		case 2:
			switch (this) {
			case NEXUS:
				return Material.BEACON;
			case GOLD:
				return Material.DEEPSLATE_GOLD_ORE;
			case DUST:
				return Material.DEEPSLATE_REDSTONE_ORE;
			case KILLER:
				return Material.IRON_AXE;
			case KNIGHT:
				return Material.CHAINMAIL_CHESTPLATE;
			case MAEGUS:
				return Material.ENDER_EYE;
			case RANGER:
				return Material.TIPPED_ARROW;
			case SIEGER:
				return Material.PISTON;
			case UPGRADE:
				return Material.ANVIL;
			}
		case 1:
			switch (this) {
			case NEXUS:
				return Material.CONDUIT;
			case GOLD:
				return Material.GOLD_ORE;
			case DUST:
				return Material.REDSTONE_ORE;
			case KILLER:
				return Material.WOODEN_SHOVEL;
			case KNIGHT:
				return Material.LEATHER_CHESTPLATE;
			case MAEGUS:
				return Material.ENDER_PEARL;
			case RANGER:
				return Material.ARROW;
			case SIEGER:
				return Material.DISPENSER;
			case UPGRADE:
				return Material.STONECUTTER;
			}
		default:
			break;
		}
		return Material.GRAY_DYE;
	}

	public String getProd(final byte lvl) {
		switch (lvl) {
		case 3:
			switch (this) {
			case NEXUS:
				return "Респавн";
			case GOLD:
				return "Золото";
			case DUST:
				return "Пыль";
			case KILLER:
				return "Иссушенные";
			case KNIGHT:
				return "Погребенные";
			case MAEGUS:
				return "Чародеи";
			case RANGER:
				return "Разбойники";
			case SIEGER:
				return "Разорители";
			case UPGRADE:
				return "Прокачка";
			}
		case 2:
			switch (this) {
			case NEXUS:
				return "Респавн";
			case GOLD:
				return "Золото";
			case DUST:
				return "Пыль";
			case KILLER:
				return "Поборники";
			case KNIGHT:
				return "Зомби";
			case MAEGUS:
				return "Ведьмы";
			case RANGER:
				return "Зимогоры";
			case SIEGER:
				return "Хоглины";
			case UPGRADE:
				return "Прокачка";
			}
		case 1:
			switch (this) {
			case NEXUS:
				return "Респавн";
			case GOLD:
				return "Золото";
			case DUST:
				return "Пыль";
			case KILLER:
				return "Пауки";
			case KNIGHT:
				return "Моряки";
			case MAEGUS:
				return "Эндеры";
			case RANGER:
				return "Скелеты";
			case SIEGER:
				return "Криперы";
			case UPGRADE:
				return "Прокачка";
			}
		default:
			break;
		}
		return "";
	}
}
