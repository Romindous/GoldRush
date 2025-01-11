package ru.romindous.invent;

import org.bukkit.enchantments.Enchantment;
import ru.komiss77.utils.TCUtil;
import ru.romindous.Main;
import ru.romindous.game.object.PlRusher;
import ru.romindous.game.object.Rusher;
import ru.romindous.game.object.build.Shop;
import ru.romindous.type.Upgrade;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

import java.util.ArrayList;
import java.util.List;

public class ShopInv implements InventoryProvider {

	private final Shop bld;

	private static final ItemStack[] emt = getEmpty();
	private static final String SL = "§n";

	public ShopInv(final Shop bd) {
		bld = bd;
	}

	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(bld.cLoc.getCenterLoc(), Sound.ENTITY_VILLAGER_TRADE, 2f, 0.8f);
		final Inventory inv = its.getInventory();
		inv.setContents(emt);
		
		its.set(4, ClickableItem.empty(new ItemBuilder(Material.LECTERN).name("§e§nВольный Магазин")
			.lore("§бОсмотрись, может что приглядишь!").lore("§бСнаряжение и улучшения на твой вкус.").build()));

		final PlRusher rs = Rusher.getPlRusher(pl);
		for (final Upgrade up : Upgrade.values()) marketClick(rs, up, its);
	}

	public void marketClick(final PlRusher rs, final Upgrade up, final InventoryContent its) {
		if (rs.team() == null) {
			its.set(up.slot, ClickableItem.empty(new ItemStack(Material.ITEM_FRAME)));
			return;
		}
		final List<String> lrs = new ArrayList<>();
		lrs.add(TCUtil.N + up.desc);
		lrs.add(" ");
		final boolean reqs = up.canBuy(rs, lrs);
		lrs.add(" ");
		if (rs.getUpg(up) != null) {
			lrs.add(TCUtil.P + "Уже приобретено!");
			its.set(up.slot, ClickableItem.empty(new ItemBuilder(up.icn).name(Upgrade.CLR+SL+up.name)
					.lore(lrs).enchant(Enchantment.MENDING).build()));
		} else if (reqs) {
			lrs.add("§a✔ " + TCUtil.P + up.price + " ур. опыта");
			lrs.add(" ");
			lrs.add("§e[§мКлик§e] §2- Приобрести");
			its.set(up.slot, ClickableItem.of(new ItemBuilder(up.icn).name(Upgrade.CLR+up.name).lore(lrs).build(), e -> {
				final Player pl = rs.getPlayer();
				if (up.canBuy(rs, null)) {
					rs.level(-up.price);
					up.addFor(rs);
					pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.6f);
					reopen(pl, its);
				} else {
					pl.sendMessage(Main.PRFX + "§4Не все требования выполнены!");
				}
			}));
		} else {
			lrs.add((rs.level() < up.price ? "§c❌ " : "§a✔ ") + TCUtil.P + up.price + " ур. опыта");
			lrs.add(" ");
			lrs.add("§4Не все требования выполнены!");
			its.set(up.slot, ClickableItem.empty(new ItemBuilder(up.icn).name(Upgrade.CLR+up.name).lore(lrs).build()));
		}
	}
	
	private static ItemStack[] getEmpty() {
		final ItemStack[] its = new ItemStack[54];
		for (int i = 0; i < 54; i++) {
			its[i] = new ItemBuilder((i & 1) == 0 ? Material.ITEM_FRAME : Material.GLOW_ITEM_FRAME).name("§0.").build();
		}
		its[0] = (its[8] = new ItemBuilder(Material.SHROOMLIGHT).name("§0.").build());
		for (int i = 17; i < 54; i+=9) {
			its[i] = (its[i - 8] = new ItemBuilder(i == 53 ? Material.GOLD_BLOCK : Material.POWERED_RAIL).name("§0.").build());
		}
		return its;
	}
}
