package gr.Romindous.type;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.utils.ItemBuilder;

public enum DirType {
	
	ATTACK(new ItemBuilder(Material.FIRE_CHARGE).name("§6Контроль (§cНападение§6)").build()),
	HOLDPS(new ItemBuilder(Material.SNOWBALL).name("§6Контроль (§7Удержание§6)").build()),
	FOLLOW(new ItemBuilder(Material.HEART_OF_THE_SEA).name("§6Контроль (§3Подчинение§6)").build()),
	DEFEND(new ItemBuilder(Material.SLIME_BALL).name("§6Контроль (§aЗащита§6)").build()),
	;
	
	public final ItemStack icn;
	
	private DirType(final ItemStack icn) {
		this.icn = icn;
	}
}
