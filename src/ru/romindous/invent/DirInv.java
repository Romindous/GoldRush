package ru.romindous.invent;

import ru.romindous.type.DirType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import ru.romindous.game.object.Rusher;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class DirInv implements InventoryProvider {
	
	private final boolean mhd;
	
	public DirInv(final boolean mhd) {
		this.mhd = mhd;
	}
	
	@Override
	public void init(final Player pl, final InventoryContent its) {
		pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, 1f, 1.2f);
		final Rusher rs = Rusher.getPlRusher(pl);
		its.set(0, ClickableItem.of(DirType.ATTACK.icn, e -> {
			if (mhd) {
				pl.getInventory().setItemInMainHand(DirType.ATTACK.icn);
			} else {
				pl.getInventory().setItemInOffHand(DirType.ATTACK.icn);
			}
			pl.getWorld().playSound(pl, Sound.ITEM_GOAT_HORN_SOUND_2, 10f, 1.2f);
			pl.getWorld().spawnParticle(Particle.SONIC_BOOM, pl.getEyeLocation(), 2);
			rs.dir(DirType.ATTACK);
			pl.closeInventory();
		}));
		its.set(1, ClickableItem.of(DirType.HOLDPS.icn, e -> {
			if (mhd) {
				pl.getInventory().setItemInMainHand(DirType.HOLDPS.icn);
			} else {
				pl.getInventory().setItemInOffHand(DirType.HOLDPS.icn);
			}
			pl.getWorld().playSound(pl, Sound.ITEM_GOAT_HORN_SOUND_7, 10f, 1.2f);
			pl.getWorld().spawnParticle(Particle.SONIC_BOOM, pl.getEyeLocation(), 2);
			rs.dir(DirType.HOLDPS);
			pl.closeInventory();
		}));
		its.set(2, ClickableItem.empty(new ItemBuilder(Material.CRYING_OBSIDIAN)
				.name("§5<= §фВыбери Действие §5=>").build()));
		/*its.set(2, ClickableItem.of(Main.toBase, e -> {
			if (rs.arena() != null && rs.team() != null) {
				final WXYZ rsp = rs.team().getCloseResp(new WXYZ(pl.getLocation(), false), true);
				if (rsp == null) {
					ApiOstrov.sendActionBarDirect(pl, "§cПостройте первый §4Нексус §cдля телепорта!");
				} else {
					pl.teleport(rsp.getCenterLoc());
					pl.getWorld().playSound(pl, Sound.ENTITY_ENDERMAN_AMBIENT, 4f, 1.6f);
					pl.getWorld().spawnParticle(Particle.PORTAL, pl.getLocation(), 40, 0.2d, 1d, 0.2d);
				}
			}
			pl.closeInventory();
		}));*/
		its.set(3, ClickableItem.of(DirType.FOLLOW.icn, e -> {
			if (mhd) {
				pl.getInventory().setItemInMainHand(DirType.FOLLOW.icn);
			} else {
				pl.getInventory().setItemInOffHand(DirType.FOLLOW.icn);
			}
			pl.getWorld().playSound(pl, Sound.ITEM_GOAT_HORN_SOUND_0, 10f, 1.2f);
			pl.getWorld().spawnParticle(Particle.SONIC_BOOM, pl.getEyeLocation(), 2);
			rs.dir(DirType.FOLLOW);
			pl.closeInventory();
		}));
		its.set(4, ClickableItem.of(DirType.DEFEND.icn, e -> {
			if (mhd) {
				pl.getInventory().setItemInMainHand(DirType.DEFEND.icn);
			} else {
				pl.getInventory().setItemInOffHand(DirType.DEFEND.icn);
			}
			pl.getWorld().playSound(pl, Sound.ITEM_GOAT_HORN_SOUND_1, 10f, 1.2f);
			pl.getWorld().spawnParticle(Particle.SONIC_BOOM, pl.getEyeLocation(), 2);
			rs.dir(DirType.DEFEND);
			pl.closeInventory();
		}));
	}
}
