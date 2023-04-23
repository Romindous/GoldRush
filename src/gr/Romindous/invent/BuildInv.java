package gr.Romindous.invent;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import gr.Romindous.game.object.Build;
import gr.Romindous.type.BuildType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class BuildInv implements InventoryProvider {
	
	private final Build bld;
	
	private static final ItemStack[] emt = getEmpty();
	private static final ItemStack upg = new ItemBuilder(Material.IRON_NUGGET).name("§7§l==>").build();
	private static final ItemStack adv = new ItemBuilder(Material.SUGAR).name("§7§l//\\\\").build();
	
	public BuildInv(final Build bd) {
		bld = bd;
	}

	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(pl.getLocation(), Sound.ITEM_AXE_SCRAPE, 2f, 0.6f);
		final Inventory inv = its.getInventory();
		inv.setContents(emt);
		
		its.set(4, ClickableItem.of(new ItemBuilder(Material.TNT).name("§4Разрушить Здание!")
			.lore(Arrays.asList("§c§lОсторожно!", "§eПринесет §6" + bld.getGCost() + " ⛃ §eи §4" + bld.getDCost() + " 🔥")).build(), e -> {
				bld.tm.blds.remove(bld);
				bld.remove(true);
				pl.closeInventory();
			}));
		its.set(49, ClickableItem.empty(bld.getInfoItem()));
		
		switch (bld.lvl) {
		case 3:
			its.set(29, ClickableItem.empty(new ItemBuilder(bld.type.getIcon((byte) 1))
				.name("§6Уровень: §41 §6(" + bld.type.getProd((byte) 1) + "§6)").build()));
			
			its.set(30, ClickableItem.empty(upg));
			
			its.set(31, ClickableItem.empty(new ItemBuilder(bld.type.getIcon((byte) 2))
				.name("§6Уровень: §42 §6(" + bld.type.getProd((byte) 2) + "§6)").build()));
			
			its.set(32, ClickableItem.empty(upg));
			
			its.set(33, ClickableItem.empty(new ItemBuilder(bld.type.getIcon((byte) 3)).enchantment(Enchantment.BINDING_CURSE)
				.name("§6Уровень: §43 §6(" + bld.type.getProd((byte) 3) + "§6)").build()));
			
			switch (bld.type) {
			case GOLD:
				its.set(20, ClickableItem.empty(adv));
				its.set(11, bld.tm.upgClick(bld, BuildType.NEXUS));
				break;
			case KNIGHT:
				its.set(20, ClickableItem.empty(adv));
				its.set(11, bld.tm.upgClick(bld, BuildType.RANGER));
				its.set(22, ClickableItem.empty(adv));
				its.set(13, bld.tm.upgClick(bld, BuildType.KILLER));
				break;
			case KILLER:
				its.set(22, ClickableItem.empty(adv));
				its.set(13, bld.tm.upgClick(bld, BuildType.SIEGER));
				break;
			case RANGER:
				its.set(22, ClickableItem.empty(adv));
				its.set(13, bld.tm.upgClick(bld, BuildType.MAEGUS));
				break;
			default:
				break;
			}
			break;
		case 2:
			its.set(29, ClickableItem.empty(new ItemBuilder(bld.type.getIcon((byte) 1))
				.name("§6Уровень: §41 §6(" + bld.type.getProd((byte) 1) + "§6)").build()));
			
			its.set(30, ClickableItem.empty(upg));
			
			its.set(31, ClickableItem.empty(new ItemBuilder(bld.type.getIcon((byte) 2)).enchantment(Enchantment.BINDING_CURSE)
				.name("§6Уровень: §42 §6(" + bld.type.getProd((byte) 2) + "§6)").build()));
			
			its.set(32, ClickableItem.empty(upg));
			
			its.set(33, bld.tm.upgClick(bld, bld.type));
			
			switch (bld.type) {
			case GOLD:
				its.set(20, ClickableItem.empty(adv));
				its.set(11, bld.tm.upgClick(bld, BuildType.NEXUS));
				break;
			case KNIGHT:
				its.set(20, ClickableItem.empty(adv));
				its.set(11, bld.tm.upgClick(bld, BuildType.RANGER));
				its.set(22, ClickableItem.empty(adv));
				its.set(13, bld.tm.upgClick(bld, BuildType.KILLER));
				break;
			case KILLER:
				its.set(22, ClickableItem.empty(adv));
				its.set(13, bld.tm.upgClick(bld, BuildType.SIEGER));
				break;
			case RANGER:
				its.set(22, ClickableItem.empty(adv));
				its.set(13, bld.tm.upgClick(bld, BuildType.MAEGUS));
				break;
			default:
				break;
			}
			break;
		case 1:
			its.set(29, ClickableItem.empty(new ItemBuilder(bld.type.getIcon((byte) 1)).enchantment(Enchantment.BINDING_CURSE)
				.name("§6Уровень: §41 §6(" + bld.type.getProd((byte) 1) + "§6)").build()));
			
			its.set(30, ClickableItem.empty(upg));
			
			its.set(31, bld.tm.upgClick(bld, bld.type));
			
			switch (bld.type) {
			case GOLD:
				its.set(20, ClickableItem.empty(adv));
				its.set(11, bld.tm.upgClick(bld, BuildType.NEXUS));
				break;
			case KNIGHT:
				its.set(20, ClickableItem.empty(adv));
				its.set(11, bld.tm.upgClick(bld, BuildType.RANGER));
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
	
	private static ItemStack[] getEmpty() {
		final ItemStack[] its = new ItemStack[54];
		its[1] = (its[7] = new ItemBuilder(Material.REDSTONE).name("§0.").build());
		its[2] = (its[6] = new ItemBuilder(Material.LIGHT_WEIGHTED_PRESSURE_PLATE).name("§0.").build());
		for (int i = 45; i < 54; i++) {
			its[i] = new ItemBuilder(Material.IRON_BARS).name("§0.").build();
		}
		for (int i = 8; i < 54; i+=9) {
			its[i] = (its[i - 8] = new ItemBuilder(i == 53 ? Material.GOLD_BLOCK : Material.POWERED_RAIL).name("§0.").build());
		}
		return its;
	}
}
