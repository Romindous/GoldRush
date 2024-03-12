package ru.romindous.listener;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.Nms;
import ru.romindous.Main;
import ru.romindous.game.Arena;
import ru.romindous.game.goal.MobGoal;
import ru.romindous.game.object.Build;
import ru.romindous.game.object.Nexus;
import ru.romindous.game.object.Rusher;
import ru.romindous.game.object.build.Shop;
import ru.romindous.invent.*;
import ru.romindous.type.BuildType;
import ru.romindous.type.DirType;
import ru.romindous.type.RaceType;

public class InterLst implements Listener {
	
	private static int mbCnt;
	private static final ItemStack sbl = new ItemStack(Material.FIRE_CHARGE);
	
	@EventHandler
	public void onInter(final PlayerInteractEvent e) {
		final ItemStack it = e.getItem();
		if (ItemUtils.isBlank(it, false)) return;
		final Player p = e.getPlayer();
		
		switch (e.getAction()) {
		case RIGHT_CLICK_BLOCK:
			if (ItemUtils.compareItem(it, Arena.pick, false)) {
				e.setUseInteractedBlock(Result.DENY);
				final Rusher rs = Rusher.getPlRusher(p);
				final WXYZ loc = new WXYZ(e.getClickedBlock());
				if (rs.arena() != null) {
					final Build bd = Build.getByArea(rs.arena(), loc);
					if (bd == null) {
						if (Build.getByArea(rs.arena(), loc) != null) {
							ApiOstrov.sendActionBarDirect(p, "§cЗдесь недостаточно §4места §cдля строительства!");
							return;
						}

						if (LocationUtil.getClsChEnt(new WXYZ(p.getLocation()), MobGoal.FAR_RANGE, LivingEntity.class, le -> rs.team().isEnemy(le)) != null) {
							ApiOstrov.sendActionBarDirect(p, "§cВы слижком близко к §4вражеской §cармии!");
							return;
						}

						final World w = loc.w;
						BuildType sgg = BuildType.BARRACK;
						final int X = loc.x, Y = loc.y, Z = loc.z;
						for (int x = -BuildType.maxXZ; x <= BuildType.maxXZ; x++) {
							for (int z = -BuildType.maxXZ; z <= BuildType.maxXZ; z++) {
								final Material mt = Nms.getFastMat(w, X + x, Y, Z + z);
								if (mt.isAir() || !Nms.getFastMat(w, X + x, Y + 1, Z + z).isAir()) {
									ApiOstrov.sendActionBarDirect(p, "§cЗдесь недостаточно §4места §cдля строительства!");
									return;
								} else if (mt == BuildType.gSite) {
									sgg = BuildType.GOLD;
								} else if (mt == BuildType.dSite) {
									sgg = BuildType.DUST;
								}
							}
						}
						SmartInventory.builder()
						.size(6, 9)
						.id("Place "+p.getName())
						.provider(new PlaceInv(loc.add(0, 1, 0), rs.team(), sgg))
						.title(rs.race().clr + "§l       Строительство")
						.build().open(p);
						return;
					} else if (bd.has(loc)) {
						if (bd instanceof final Shop sh) {
							SmartInventory.builder()
								.size(6, 9)
								.id("Shop "+p.getName())
								.provider(new ShopInv(sh))
								.title(TCUtils.P + "§l         Магазин")
								.build().open(p);
							return;
						} else if (rs.team().cc == bd.tm().cc) {
							if (bd.done()) {
								SmartInventory.builder()
									.size(6, 9)
									.id("Place "+p.getName())
									.provider(new BuildInv(bd))
									.title(bd.race().clr + "§l         " + bd.type().nm)
									.build().open(p);
							} else {
								ApiOstrov.sendActionBarDirect(p, "§cЭто §4здание §cв процессе постройки!");
							}
							return;
						}
					} else {
						ApiOstrov.sendActionBarDirect(p, "§cЗдесь недостаточно §4места §cдля строительства!");
					}
				}
			}
		case RIGHT_CLICK_AIR:
			if (ItemUtils.compareItem(it, Main.join, false)) {
				SmartInventory.builder()
				.size(6, 9)
	            .id("Game "+p.getName())
	            .provider(new ArenaInv())
	            .title(TCUtils.P + "§l         Выбор Карты")
	            .build().open(p);
			} else if (ItemUtils.compareItem(it, Main.hub, false)) {
				p.performCommand("hub");
			} else if (ItemUtils.compareItem(it, Main.leave, false)) {
				p.performCommand("gr leave");
			} else if (ItemUtils.compareItem(it, Arena.map, false)) {
				final Rusher rs = Rusher.getPlRusher(p);
				if (rs.team() != null && !p.hasCooldown(Arena.map.getType())) rs.team().buildMap(p, p.getEyeLocation(), false);
			} else if (ItemUtils.compareItem(it, Arena.tpMap, false)) {
				final Rusher rs = Rusher.getPlRusher(p);
				if (rs.team() != null && !p.hasCooldown(Arena.tpMap.getType())) rs.team().buildMap(p, p.getEyeLocation(), true);
			} else if (ItemUtils.compareItem(it, Main.color, false) || ItemUtils.compareItem(it, Main.glow, false)) {
				SmartInventory.builder()
				.size(3, 9)
	            .id("Team "+p.getName())
	            .provider(new TeamInv())
	            .title(TCUtils.P + "§l        Выбор Цвета")
	            .build().open(p);
			} else if (it.getType() == Material.EMERALD) {
				final SetupInv mm = SetupInv.edits.get(p.getUniqueId());
				if (mm != null) {
					SmartInventory.builder().size(3, 9)
                    .id("Map "+p.getName()).title(TCUtils.P + "Редактор Карты §4" + mm.stp.nm)
                    .provider(mm).build().open(p);
				}
			} else if (ItemUtils.compareItem(it, Main.race, false)) {
				SmartInventory.builder()
				.type(InventoryType.HOPPER)
	            .id("Race "+p.getName())
	            .provider(new RaceInv())
	            .title(TCUtils.P + "§l        Выбор Рассы")
	            .build().open(p);
			} else {
				for (final DirType dt : DirType.values()) {
					if (ItemUtils.compareItem(it, dt.icn, false)) {
						SmartInventory.builder()
						.type(InventoryType.HOPPER)
			            .id("Dir "+p.getName())
			            .provider(new DirInv(e.getHand() == EquipmentSlot.HAND))
			            .title(TCUtils.P + "§l     Управление Войском")
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
        if (ItemUtils.compareItem(p.getInventory().getItemInMainHand(), Arena.pick, false)) {
			final Rusher rs = Rusher.getPlRusher(p);
			final WXYZ loc = new WXYZ(e.getBlock());
			final Build bd = rs.arena() == null ? null :
				Build.getByArea(rs.arena(), loc);
			if (bd == null || bd instanceof Shop || !bd.has(loc)) {
				e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer(), false));
				return;
			}

			final char rtc = rs.team().cc;
			final char btc = bd.tm().cc;
			if (rs.arena() != null && rtc != btc) {
				for (final Mob mb : LocationUtil.getChEnts(bd.cLoc(), 16, Mob.class, mb -> true)) {
					final Nexus nx = MobGoal.getMobTeam(mb);
					if (nx != null) {
						switch (BuildType.getTypeFor(mb.getType())) {
							case BARRACK, RANGE:
								if (nx.cc == rtc) mbCnt += 1;
								else if (nx.cc == btc) mbCnt -= 1;
								break;
							case CAMP:
								if (nx.cc == rtc) mbCnt += 2;
								else if (nx.cc == btc) mbCnt -= 2;
								break;
							case ALTAR:
								if (nx.cc == rtc) mbCnt += 3;
								else if (nx.cc == btc) mbCnt -= 3;
								break;
							case TRIBUNE:
								if (nx.cc == rtc) mbCnt += 5;
								else if (nx.cc == btc) mbCnt -= 5;
								break;
						}
					}
				}
				final int dmg = Math.max(0, 4 + mbCnt);
				if (dmg > 0 && rs.race() == RaceType.ILLAGER)
					rs.team().gold += dmg;
				if (bd.damage(dmg)) {
					rs.level(bd.lvl());
					rs.brksI();
				}
				mbCnt = 0;
				return;
			}
		}
		
		e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer(), false));
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onExp(final PlayerExpChangeEvent e) {
		final Player p = e.getPlayer();
		int nxp = p.getTotalExperience() + e.getAmount();
		p.giveExp(e.getAmount());
		final int l = (int) (Math.sqrt((float) nxp / 1.5f));
		p.setLevel(l);
		p.setExp((nxp / 1.5f - (l * l)) / (FastMath.square(l + 1) - (l * l)));
//		Rusher.getPlRusher(p);
		e.setAmount(0);
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onShoot(final EntityShootBowEvent e) {
		if (e.getEntityType() == EntityType.PIGLIN) {
			e.setCancelled(true);
			return;
		}

		if (e.getBow() != null && e.getBow().containsEnchantment(Enchantment.ARROW_FIRE)) {
			final LivingEntity le = e.getEntity();
			final Snowball sb = le.launchProjectile(Snowball.class, le.getEyeLocation()
				.getDirection().multiply(e.getForce() + 0.2f));
			sb.setItem(sbl); sb.setVisualFire(true);
			e.getProjectile().remove();
		}
	}
}
