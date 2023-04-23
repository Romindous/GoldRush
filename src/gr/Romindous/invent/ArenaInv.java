package gr.Romindous.invent;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import gr.Romindous.Main;
import gr.Romindous.game.Arena;
import gr.Romindous.game.map.Setup;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class ArenaInv implements InventoryProvider {

	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(pl.getLocation(), Sound.BLOCK_NETHER_GOLD_ORE_STEP, 1f, 0.6f);
		its.getInventory().setContents(getEmpty());
		its.set(4, ClickableItem.of(new ItemBuilder(Material.BELL).name("§eБыстрый Поиск").build(), e -> {
			pl.performCommand("gr join");
		}));
		int i = 8;
		for (final Setup stp : Main.nonactive.values()) {
			if (i++ == 45) break;
			final Arena ar = Main.active.get(stp.nm);
			if (ar == null) {
				its.set(i, ClickableItem.of(new ItemBuilder(Material.GOLD_NUGGET).name("§6" + stp.nm)
					.lore(Arrays.asList(" ", "§eСтадия: §4Ожидание", "§eИгроков: §40§6/§4" + stp.min)).build(), e -> {
					pl.performCommand("gr join " + stp.nm);
					pl.closeInventory();
				}));
			} else {
				switch (ar.gst) {
				case ОЖИДАНИЕ:
					its.set(i, ClickableItem.of(new ItemBuilder(Material.GOLD_NUGGET).name("§6" + ar.name)
						.lore(Arrays.asList(" ", "§eСтадия: §4Ожидание", "§eИгроков: §4" + ar.pls.size() + "§6/§4" + ar.min)).build(), e -> {
						pl.performCommand("gr join " + ar.name);
						pl.closeInventory();
					}));
					break;
				case СТАРТ:
					its.set(i, ClickableItem.of(new ItemBuilder(Material.RAW_GOLD).name("§6" + ar.name)
						.lore(Arrays.asList(" ", "§eСтадия: §4Старт", "§eИгроков: §4" + ar.pls.size() + "§6/§4" + ar.max)).build(), e -> {
						pl.performCommand("gr join " + ar.name);
						pl.closeInventory();
					}));
					break;
				case ИГРА:
					its.set(i, ClickableItem.of(new ItemBuilder(Material.RAW_GOLD_BLOCK).name("§6" + ar.name)
						.lore(Arrays.asList(" ", "§eСтадия: §4Игра", "§eИгроков: §4" + ar.pls.size() + "§6/§4" + ar.max, "§cМожно зайти зрителем!")).build(), e -> {
							pl.performCommand("gr join " + ar.name);
							pl.closeInventory();
						}));
					break;
				case ФИНИШ:
					its.set(i, ClickableItem.empty(new ItemBuilder(Material.GOLD_BLOCK).name("§6" + ar.name)
						.lore(Arrays.asList(" ", "§eСтадия: §4Финиш", "§eИгроков: §4" + ar.pls.size() + "§6/§4" + ar.max)).build()));
					break;
				default:
					break;
				}
			}
		}
	}
	
	private static ItemStack[] getEmpty() {
		final ItemStack[] its = new ItemStack[54];
		for (int i = 0; i < 9; i++) {
			its[i] = new ItemBuilder((i & 1) == 0 ? Material.ORANGE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE).name("§0.").build();
			its[i + 45] = new ItemBuilder((i & 1) == 0 ? Material.RED_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE).name("§0.").build();
		}
		return its;
	}
}
