package ru.romindous.invent;

import java.util.Map.Entry;

import ru.romindous.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.romindous.game.object.Nexus;
import ru.romindous.game.object.Rusher;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class TeamInv implements InventoryProvider {
	
	private final WXYZ[] ela = new WXYZ[0];
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(pl.getLocation(), Sound.BLOCK_BEEHIVE_ENTER, 1f, 1.2f);
		its.getInventory().setContents(getEmpty());
		final Rusher rs = Rusher.getPlRusher(pl);
		if (rs.arena() == null) {
			pl.closeInventory();
			return;
		}
		
		its.set(4, ClickableItem.of(new ItemBuilder(Material.CONDUIT).name("§eРандом Комманда").build(), e -> {
			final WXYZ lc = Main.rndElmt(rs.arena().orbs.keySet().toArray(ela));
			final Nexus nx = rs.arena().orbs.get(lc);
			pl.teleport(lc.getCenterLoc());
			pl.playSound(pl.getLocation(), Sound.BLOCK_BAMBOO_SAPLING_BREAK, 1f, 0.6f);
			pl.sendMessage(Main.PRFX + "Это спавн " + TCUtils.nameOf(nx.cc, "ой", true) + TCUtils.N + " базы.");
		}));
		
		int i = 9;
		for (final Entry<WXYZ, Nexus> en : rs.arena().orbs.entrySet()) {
			final Nexus nx = en.getValue();
			its.set(i = i == 16 ? 19 : i + 1, ClickableItem.of(new ItemBuilder(Material.getMaterial(
				TCUtils.getDyeColor(TCUtils.getTextColor(nx.cc)).name() + "_CONCRETE_POWDER"))
				.name(TCUtils.nameOf(nx.cc, "ая", true)).build(), e -> {
				pl.teleport(en.getKey().getCenterLoc());
				pl.playSound(pl.getLocation(), Sound.BLOCK_BAMBOO_SAPLING_BREAK, 1f, 0.6f);
				pl.sendMessage(Main.PRFX + "Это спавн " + TCUtils.nameOf(nx.cc, "ой", true) + TCUtils.N + " базы.");
			}));
		}
	}
	
	private static ItemStack[] getEmpty() {
		final ItemStack[] its = new ItemStack[27];
		for (int i = 0; i < 9; i++) {
			its[i] = new ItemBuilder((i & 1) == 0 ? Material.YELLOW_STAINED_GLASS_PANE : Material.WHITE_STAINED_GLASS_PANE).name("§0.").build();
		}
		its[17] = (its[9] = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("§0.").build());
		its[26] = (its[18] = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§0.").build());
		return its;
	}
}
