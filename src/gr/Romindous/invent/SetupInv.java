package gr.Romindous.invent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import gr.Romindous.Main;
import gr.Romindous.game.map.Setup;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class SetupInv implements InventoryProvider {
	
	public static final HashMap<UUID, SetupInv> edits = new HashMap<>();
	
	public Setup stp;
	
	public SetupInv(final String name) {
		final Setup ar = Main.nonactive.get(name);
		stp = ar == null ? new Setup(name) : 
		new Setup(ar.nm, ar.min, ar.lobby, ar.bases, ar.bots, ar.fin);
	}
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		edits.put(pl.getUniqueId(), this);
		
		its.set(0, new InputButton(InputType.ANVILL, //имя
			new ItemBuilder(Material.GLOBE_BANNER_PATTERN).name("§5" + stp.nm).lore(Arrays.asList("§dКлик §7- изменить имя")).build(), "Карта", nm -> {
			stp = new Setup(nm, stp.min, stp.lobby, stp.bases, stp.bots, stp.fin);
			reopen(pl, its);
		}));
		
		its.set(1, ClickableItem.of(new ItemBuilder(Material.RABBIT_HIDE).name("§7Минимум Игроков")
			.setAmount(stp.min).lore(Arrays.asList("§dЛКМ §7= +1", "§cПКМ §7= -1")).build(), e -> {
			switch (e.getClick()) {
			case RIGHT, SHIFT_RIGHT:
				if (stp.min == 1) return;
				stp = new Setup(stp.nm, (byte) (stp.min - 1), stp.lobby, stp.bases, stp.bots, stp.fin);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
				break;
			default:
				stp = new Setup(stp.nm, (byte) (stp.min + 1), stp.lobby, stp.bases, stp.bots, stp.fin);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			reopen(pl, its);
		}));
		
		final List<String> glr = new ArrayList<>();
		glr.add("§6ЛКМ §7- добавить");
		glr.add("§4ПКМ §7- удалить");
		glr.add("§eШифт+ЛКМ §7- показать");
		glr.add("§eШифт+ПКМ §7- убрать показ");
		glr.add(" ");
		final boolean gds = stp.bases != null;
		if (gds) {
			for (final XYZ lc : stp.bases) {
				glr.add("§6" + lc.toString());
			}
		} else {
			glr.add("§7Точек еще нет...");
		}
		its.set(2, ClickableItem.of(new ItemBuilder(Material.GOLD_BLOCK).name("§6Золотые Месторождения").lore(glr).build(), e -> {
			switch (e.getClick()) {
			case SHIFT_LEFT:
				if (gds) {
					final World w = pl.getWorld();
					for (final XYZ lc : stp.bases) {
						w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.BEACON, false);
					}
				}
				pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
				break;
			case SHIFT_RIGHT:
				if (gds) {
					final World w = pl.getWorld();
					for (final XYZ lc : stp.bases) {
						w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.AIR, false);
					}
				}
				pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
				break;
			case RIGHT:
				if (stp.bases == null) return;
				pl.sendMessage(Main.prf() + "§6Убрана предыдущая точка (§4" + (stp.bases.length-1) + "§6)");
				final XYZ prv = stp.bases[stp.bases.length - 1];
				pl.getWorld().getBlockAt(prv.x, prv.y, prv.z).setType(Material.AIR, false);
				stp = new Setup(stp.nm, stp.min, stp.lobby, rmv(stp.bases), stp.bots, stp.fin);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
				reopen(pl, its);
				return;
			default:
				final Location loc = pl.getLocation();
				stp = new Setup(stp.nm, stp.min, stp.lobby, add(stp.bases, new XYZ(loc)), stp.bots, stp.fin);
				loc.getBlock().setType(Material.BEACON, false);
				pl.sendMessage(Main.prf() + "§6Точка поставлена на §4" + 
					new XYZ(loc).toString() + " §6(§4" + stp.bases.length + "§6)");
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			pl.closeInventory();
		}));
		
		/*final List<String> dlr = new ArrayList<>();
		dlr.add("§6ЛКМ §7- добавить");
		dlr.add("§4ПКМ §7- удалить");
		dlr.add("§eШифт+ЛКМ §7- показать");
		dlr.add("§eШифт+ПКМ §7- убрать показ");
		dlr.add(" ");
		final boolean dts = stp.dust == null;
		if (dts) {
			for (final XYZ lc : stp.dust) {
				glr.add("§6" + lc.toString());
			}
		} else {
			glr.add("§7Точек еще нет...");
		}
		its.set(4, ClickableItem.of(new ItemBuilder(Material.GOLD_BLOCK).name("§4Месторождения Руды").lore(dlr).build(), e -> {
			switch (e.getClick()) {
			case SHIFT_LEFT:
				if (dts) {
					final World w = pl.getWorld();
					for (final XYZ lc : stp.dust) {
						w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.RAW_GOLD_BLOCK, false);
					}
				}
				pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
				break;
			case SHIFT_RIGHT:
				if (dts) {
					final World w = pl.getWorld();
					for (final XYZ lc : stp.dust) {
						w.getBlockAt(lc.x, lc.y, lc.z).setType(Material.AIR, false);
					}
				}
				pl.playSound(pl.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
				break;
			case RIGHT:
				if (stp.dust == null) return;
				pl.sendMessage(Main.prf() + "§6Убрана предыдущая точка (§4" + (stp.dust.length-1) + "§6)");
				final XYZ prv = stp.dust[stp.dust.length - 1];
				pl.getWorld().getBlockAt(prv.x, prv.y, prv.z).setType(Material.AIR, false);
				stp = new Setup(stp.nm, stp.min, stp.lobby, stp.bases, rmv(stp.dust), stp.bots, stp.fin);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 0.6f);
				reopen(pl, its);
				return;
			default:
				final Location loc = pl.getLocation();loc.setY(loc.getY() + 1d);
				stp = new Setup(stp.nm, stp.min, stp.lobby, stp.bases, add(stp.dust, new XYZ(loc)), stp.bots, stp.fin);
				loc.getBlock().setType(Material.BLUE_STAINED_GLASS, false);
				pl.sendMessage(Main.prf() + "§6Точка поставлена на §4" + 
				new XYZ(loc).toString() + " §6(§4" + stp.dust.length + "§6)");
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			pl.closeInventory();
		}));*/
		
		its.set(3, ClickableItem.of(new ItemBuilder(Material.DIAMOND_BLOCK).name("§6Точка Лобби")
			.lore(Arrays.asList("§dКлик §7- поставить", "§eШифт+Клик §7- показать", stp.lobby == null ? "§7Не поставлена..." : "§6Точка: §4" + stp.lobby.toString())).build(), e -> {
			switch (e.getClick()) {
			case SHIFT_LEFT, SHIFT_RIGHT:
				if (stp.lobby != null) {
					pl.teleport(new Location(pl.getWorld(), stp.lobby.x + 0.5d, stp.lobby.y + 0.5d, stp.lobby.z + 0.5d));
					pl.sendMessage(Main.prf() + "§6Тут точка лобби!");
				}
				break;
			default:
				final Location loc = pl.getLocation();
				stp = new Setup(stp.nm, stp.min, new XYZ(loc), stp.bases, stp.bots, stp.fin);
				pl.sendMessage(Main.prf() + "§6Точка лобби теперь на §4" + stp.lobby.toString());
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				break;
			}
			pl.closeInventory();
		}));
		
		if (stp.bots) {
			its.set(4, ClickableItem.of(new ItemBuilder(Material.FERMENTED_SPIDER_EYE).name("§6Боты: §aВкл").lore(Arrays.asList("§eКлик §7- Выкл")).build(), e -> {
				stp = new Setup(stp.nm, stp.min, stp.lobby, stp.bases, false, stp.fin);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
		} else {
			its.set(4, ClickableItem.of(new ItemBuilder(Material.SPIDER_EYE).name("§6Боты: §cВыкл").lore(Arrays.asList("§eКлик §7- Вкл")).build(), e -> {
				stp = new Setup(stp.nm, stp.min, stp.lobby, stp.bases, true, stp.fin);
				pl.playSound(pl.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1.4f);
				reopen(pl, its);
			}));
		}
		
		if (stp.isReady()) {
			stp = new Setup(stp.nm, stp.min, stp.lobby, stp.bases, stp.bots, true);
			its.set(5, ClickableItem.of(new ItemBuilder(Material.KNOWLEDGE_BOOK).name("§aГотово").lore(Arrays.asList("§6Закрыть редактор!")).build(), e -> {
				pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
				pl.sendMessage(Main.prf() + "§6Карта §4" + stp.nm + " §6сохранена!");
				Main.nonactive.put(stp.nm, stp);
				edits.remove(pl.getUniqueId());
				stp.save();
				pl.closeInventory();
			}));
		} else {
			stp = new Setup(stp.nm, stp.min, stp.lobby, stp.bases, stp.bots, false);
			its.set(5, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE).name("§cНе Готово").lore(Arrays.asList("§4Какие-то поля пустые!")).build()));
		}
		
		its.set(6, new InputButton(InputType.ANVILL, new ItemBuilder(Material.BARRIER)
		.name("§4Удалить").lore(Arrays.asList("§7Невозвратимо!")).build(), stp.nm, e -> {
			pl.playSound(pl.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
			pl.sendMessage(Main.prf() + "§6Карта §4" + stp.nm + " §6удалена!");
			edits.remove(pl.getUniqueId());
			stp.delete(true);
			pl.closeInventory();
		}));
	}
	
	private static XYZ[] add(final XYZ[] ar, final XYZ el) {
		if (ar == null) return new XYZ[] {el};
		final XYZ[] na = new XYZ[ar.length + 1];
		for (int i = 0; i < ar.length; i++) {na[i] = ar[i];}
		na[ar.length] = el;
		return na;
	}
	
	private static XYZ[] rmv(final XYZ[] ar) {
		if (ar == null || ar.length == 1) return null;
		final XYZ[] na = new XYZ[ar.length - 1];
		for (int i = 0; i < na.length; i++) {na[i] = ar[i];}
		return na;
	}
}
