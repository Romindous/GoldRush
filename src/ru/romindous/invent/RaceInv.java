package ru.romindous.invent;

import java.util.ArrayList;

import ru.romindous.game.Arena;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import ru.romindous.game.object.PlRusher;
import ru.romindous.game.object.Rusher;
import ru.romindous.type.RaceType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class RaceInv implements InventoryProvider {
	
	public RaceInv() {}
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 1.2f);
		
		final PlRusher rs = Rusher.getPlRusher(pl);
		final Arena ar = rs.arena();
		if (ar == null) {
			pl.closeInventory();
			return;
		}
		
		final RaceType rc = rs.race();
		if (rc == null) {
			its.fill(ClickableItem.empty(new ItemBuilder(Material.HEART_OF_THE_SEA).name(TCUtil.N + "Рандом")
				.lore(getComp(null, ar)).enchant(Enchantment.MENDING).build()));
		} else {
			its.fill(ClickableItem.of(new ItemBuilder(Material.HEART_OF_THE_SEA).name(TCUtil.N + "Рандом")
				.lore(getComp(null, ar)).lore(" ").lore(TCUtil.P + "КЛИК " + TCUtil.N + " - выбрать").build(), e -> {
				pl.playSound(pl.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 2f, 1.2f);
				rs.race(null);
				reopen(pl, its);
			}));
		}
		
		for (final RaceType rt : RaceType.values()) {
			if (rc == rt) {
				its.set(rt.ordinal() << 1, ClickableItem.empty(new ItemBuilder(rt.icn).name(rt.clr + rt.name)
					.lore(getComp(rt, ar)).enchant(Enchantment.MENDING).build()));
			} else {
				its.set(rt.ordinal() << 1, ClickableItem.of(new ItemBuilder(rt.icn).name(rt.clr + rt.name)
						.lore(getComp(rt, ar)).lore(" ").lore(TCUtil.P + "КЛИК " + TCUtil.N + " - выбрать").build(), e -> {
					pl.playSound(pl.getLocation(), rt.snd, 2f, 0.8f);
					rs.race(rt);
					reopen(pl, its);
				}));
			}
		}
	}

	private String[] getComp(final RaceType rc, final Arena ar) {
		final ArrayList<String> rcs = new ArrayList<>();
		for (final Rusher rs : ar.pls) {
			if (rs.race() == rc) rcs.add((rc == null ? TCUtil.N : rc.clr) + "✦ " + TCUtil.N + rs.name());
		}
		return rcs.toArray(new String[0]);
	}
}
