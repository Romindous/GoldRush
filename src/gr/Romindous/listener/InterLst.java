package gr.Romindous.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import gr.Romindous.Main;
import gr.Romindous.game.Arena;
import gr.Romindous.game.goal.MobGoal;
import gr.Romindous.game.map.WXYZ;
import gr.Romindous.game.object.Build;
import gr.Romindous.game.object.Nexus;
import gr.Romindous.game.object.Rusher;
import gr.Romindous.invent.ArenaInv;
import gr.Romindous.invent.BuildInv;
import gr.Romindous.invent.DirInv;
import gr.Romindous.invent.PlaceInv;
import gr.Romindous.invent.SetupInv;
import gr.Romindous.invent.TeamInv;
import gr.Romindous.type.DirType;
import gr.Romindous.util.ItemUtil;
import gr.Romindous.util.LocUtil;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.SmartInventory;

public class InterLst implements Listener {
	
	private static int mbCnt;
	
	@EventHandler
	public void onInter(final PlayerInteractEvent e) {
		final ItemStack it = e.getItem();
		if (ItemUtil.isBlankItem(it, false)) return;
		final Player p = e.getPlayer();
		
		switch (e.getAction()) {
		case LEFT_CLICK_BLOCK:
			/*if (ItemUtils.compareItem(p.getInventory().getItemInMainHand(), Arena.pick, false)) {
				final Build bd = Main.getBldBlck(new WXYZ(e.getClickedBlock()));
				if (bd != null) {
					final Rusher rs = Rusher.getPlRusher(p.getName(), false);
					if (rs.arena() != null && rs.team().clr != bd.tm.clr) {
						LocUtil.forEntsIn2D(bd.cLoc.getCenterLoc(), 200, le -> {
							switch (le.getType()) {
							case DROWNED, ZOMBIE_VILLAGER, HUSK, SKELETON, STRAY, SPIDER:
							default:
								mbCnt += 1;
								break;
							case PILLAGER, VINDICATOR, ENDERMAN, WITCH, CREEPER:
								mbCnt += 2;
								break;
							case ZOGLIN, WITHER_SKELETON, EVOKER:
								mbCnt += 3;
								break;
							case RAVAGER:
								mbCnt += 4;
								break;
							}
						});
						if (bd.damage(4 + mbCnt)) {
							rs.brksI();
						}
						return;
					}
				}
			}*/
			break;
		case RIGHT_CLICK_BLOCK:
			if (ItemUtils.compareItem(it, Arena.pick, false)) {
				final Rusher rs = Rusher.getPlRusher(p.getName(), false);
				final WXYZ loc = new WXYZ(e.getClickedBlock());
				if (rs.arena() != null) {
					final Build bd = Main.getBldBlck(loc);
					if (bd == null) {
						if (rs.arena().validateLoc(loc)) {
							SmartInventory.builder()
							.size(6, 9)
				            .id("Place "+p.getName())
				            .provider(new PlaceInv(loc.add(0, 1, 0), rs.team()))
				            .title("§6§l       Строительство")
				            .build().open(p);
							return;
						} else {
							ApiOstrov.sendActionBarDirect(p, "§cЗдесь недостаточно §4места §cдля строительства!");
						}
					} else if (rs.team().clr == bd.tm.clr && bd.done) {
						SmartInventory.builder()
						.size(6, 9)
			            .id("Place "+p.getName())
			            .provider(new BuildInv(bd))
			            .title("§6§l         " + bd.type.nm)
			            .build().open(p);
						return;
					} else {
						ApiOstrov.sendActionBarDirect(p, "§cЭто §4здание §cв процессе постройки!");
					}
				}
			}
		case RIGHT_CLICK_AIR:
			if (ItemUtils.compareItem(it, Main.join, false)) {
				SmartInventory.builder()
				.size(6, 9)
	            .id("Game "+p.getName())
	            .provider(new ArenaInv())
	            .title("§6§l         Выбор Карты")
	            .build().open(p);
			} else if (ItemUtils.compareItem(it, Main.hub, false)) {
				p.performCommand("hub");
			} else if (ItemUtils.compareItem(it, Main.leave, false)) {
				p.performCommand("gr leave");
			} else if (ItemUtils.compareItem(it, Main.team, false) || ItemUtils.compareItem(it, Main.glow, false)) {
				SmartInventory.builder()
				.size(3, 9)
	            .id("Team "+p.getName())
	            .provider(new TeamInv())
	            .title("§6§l       Выбор Комманды")
	            .build().open(p);
			} else if (it.getType() == Material.EMERALD) {
				final SetupInv mm = SetupInv.edits.get(p.getUniqueId());
				if (mm != null) {
					SmartInventory.builder().size(3, 9)
                    .id("Map "+p.getName()).title("§6Редактор Карты §4" + mm.stp.nm)
                    .provider(mm).build().open(p);
				}
			} else {
				for (final DirType dt : DirType.values()) {
					if (ItemUtils.compareItem(it, dt.icn, false)) {
						SmartInventory.builder()
						.type(InventoryType.HOPPER)
			            .id("Dir "+p.getName())
			            .provider(new DirInv(e.getHand() == EquipmentSlot.HAND))
			            .title("§6§l     Управление Войском")
			            .build().open(p);
						e.setUseItemInHand(Result.DENY);
						break;
					}
				}
			}
			break;
		default:
			break;
		}
	}
	
	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {e.setCancelled(true);}
	
	@EventHandler
	public void onPick(final EntityPickupItemEvent e) {e.setCancelled(true);}
	
	@EventHandler
	public void onPlace(final BlockPlaceEvent e) {
		e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer(), false));
	}
	
	@EventHandler
	public void onBreak(final BlockBreakEvent e) {
		final Player p = e.getPlayer();
		if (p == null) return;
		if (ItemUtils.compareItem(p.getInventory().getItemInMainHand(), Arena.pick, false)) {
			final Build bd = Main.getBldBlck(new WXYZ(e.getBlock()));
			if (bd != null) {
				final Rusher rs = Rusher.getPlRusher(p.getName(), false);
				final ChatColor rtc = rs.team().clr;
				final ChatColor btc = bd.tm.clr;
				if (rs.arena() != null && rtc != btc) {
					LocUtil.forEntsIn2D(bd.cLoc, 16, le -> {
						final Nexus nx = le instanceof Mob ? MobGoal.getMobTeam((Mob) le) : null;
						if (nx != null) {
							switch (le.getType()) {
							case DROWNED, ZOMBIE_VILLAGER, HUSK, SKELETON, STRAY, SPIDER:
							default:
								if (nx.clr == rtc) mbCnt += 1;
								else if (nx.clr == btc) mbCnt -= 1;
								break;
							case PILLAGER, VINDICATOR, ENDERMAN, WITCH, CREEPER:
								if (nx.clr == rtc) mbCnt += 2;
								else if (nx.clr == btc) mbCnt -= 2;
								break;
							case ZOGLIN, WITHER_SKELETON, EVOKER:
								if (nx.clr == rtc) mbCnt += 3;
								else if (nx.clr == btc) mbCnt -= 3;
								break;
							case RAVAGER:
								if (nx.clr == rtc) mbCnt += 4;
								else if (nx.clr == btc) mbCnt -= 4;
								break;
							}
						}
					});
					if (bd.damage(Math.max(0, 4 + mbCnt))) {
						rs.brksI();
					}
					mbCnt = 0;
					return;
				}
			}
		}
		
		e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer(), false));
	}
}
