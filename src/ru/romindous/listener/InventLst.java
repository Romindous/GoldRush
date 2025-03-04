package ru.romindous.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemUtil;

public class InventLst implements Listener {
	
	@EventHandler
	public void onInv(final InventoryClickEvent e) {
		final ItemStack it = e.getCurrentItem();
		if (e.getSlot() != e.getRawSlot() || ItemUtil.isBlank(it, false)) return;
		switch (it.getType()) {
		default:
			e.setCancelled(true);
		case IRON_SWORD, GOLDEN_AXE, BOW, FIRE_CHARGE, RECOVERY_COMPASS,
			ENDER_EYE, HEART_OF_THE_SEA, SLIME_BALL, COMPASS, SNOWBALL:
			break;
		}
	}
	
	@EventHandler
	public void onInv(final InventoryDragEvent e) {
		if (e.getView().getBottomInventory().getType() 
			!= e.getInventory().getType()) e.setCancelled(true);
	}
	
	@EventHandler
	public void onOpen(final InventoryOpenEvent e) {
		switch (e.getView().getType()) {
		case BARREL, BLAST_FURNACE, BREWING, 
		DISPENSER, DROPPER, ENCHANTING, ENDER_CHEST, 
		FURNACE, GRINDSTONE, LECTERN, SHULKER_BOX, 
		SMOKER:
			e.setCancelled(true);
		default:
			break;
		}
	}
}
