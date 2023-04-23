package gr.Romindous.invent;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import gr.Romindous.game.map.WXYZ;
import gr.Romindous.game.object.Nexus;
import gr.Romindous.type.BuildType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class PlaceInv implements InventoryProvider {
	
	private final Nexus tm;
	private final WXYZ at;
	
	private static final ItemStack[] emt = getEmpty();
	
	public PlaceInv(final WXYZ at, final Nexus tm) {
		this.tm = tm;
		this.at = at;
	}

	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(pl.getLocation(), Sound.ITEM_TRIDENT_HIT, 1f, 0.6f);
		final Inventory inv = its.getInventory();
		inv.setContents(emt);

		its.set(21, tm.bldClick(at, BuildType.GOLD));
		
		its.set(29, tm.bldClick(at, BuildType.DUST));
		
		its.set(23, tm.bldClick(at, BuildType.KNIGHT));
		
		its.set(33, tm.bldClick(at, BuildType.UPGRADE));
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
