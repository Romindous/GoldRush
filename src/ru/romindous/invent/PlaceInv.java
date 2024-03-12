package ru.romindous.invent;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.romindous.game.object.Nexus;
import ru.romindous.type.BuildType;

public class PlaceInv implements InventoryProvider {
	
	private final Nexus tm;
	private final WXYZ at;
	private final BuildType sgg;
	
	private static final ItemStack[] emt = getEmpty();
	
	public PlaceInv(final WXYZ at, final Nexus tm, final BuildType sgg) {
		this.tm = tm;
		this.at = at;
		this.sgg = sgg;
	}

	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(pl.getLocation(), Sound.ITEM_TRIDENT_HIT, 1f, 0.6f);
		final Inventory inv = its.getInventory();
		inv.setContents(emt);

		its.set(21, tm.bldClick(at, BuildType.GOLD, sgg));
		
		its.set(29, tm.bldClick(at, BuildType.DUST, sgg));
		
		its.set(23, tm.bldClick(at, BuildType.BARRACK, sgg));
		
		its.set(33, tm.bldClick(at, BuildType.UPGRADE, sgg));

		its.set(31, tm.bldClick(at, BuildType.SPIRE, sgg));
	}
	
	private static ItemStack[] getEmpty() {
		final ItemStack[] its = new ItemStack[54];
		for (int i = 8; i < 54; i+=9) {
			its[i] = (its[i - 8] = new ItemBuilder(i == 8 ? Material.RAW_COPPER_BLOCK : Material.ACTIVATOR_RAIL).name("§0.").build());
		}
		its[46] = (its[52] = new ItemBuilder(Material.GUNPOWDER).name("§0.").build());
		its[47] = (its[51] = new ItemBuilder(Material.TRIPWIRE_HOOK).name("§0.").build());
		for (int i = 1; i < 8; i++) {
			its[i] = new ItemBuilder(Material.HANGING_ROOTS).name("§0.").build();
		}
		return its;
	}
}
