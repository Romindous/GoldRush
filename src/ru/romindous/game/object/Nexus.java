package ru.romindous.game.object;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.displays.DisplayManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.romindous.game.Arena;
import ru.romindous.game.goal.MobGoal;
import ru.romindous.type.BuildType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

public class Nexus {

	public static final int ST_UPG = 20;
	public static final int TP_CD_TICK = 640;
	public static final double MAP_REL = 0.05d;

	public final Rusher rs;
	public final char cc;
	public final TextColor txc;
	public final HashSet<Build> blds;
	public final EnumMap<BuildType, Integer> lvls;
	
	public boolean alive;

	public int gold;
	public int dust;

	public int lvl;
//	public int mod;
	public int uptm;

	public float mobDmg;
	public float mobKb;
	public float mobSpd;
	public float mobHp;
	public float mobAr;
	public float mobCd;
	
	public Nexus(final Rusher rs, final char clr) {
		this.rs = rs;
		this.cc = clr;
		this.txc = TCUtil.getTextColor(clr);
		if (rs != null && txc instanceof NamedTextColor)
			rs.color((NamedTextColor) txc);
		this.lvls = new EnumMap<>(BuildType.class);
		this.blds = new HashSet<>();
		alive = true;
		gold = 0;
		dust = 0;
		mobDmg = 1f;
		mobKb = 1f;
		mobSpd = 1f;
		mobHp = 1f;
		mobAr = 1f;
		mobCd = 1f;

		uptm = ST_UPG;
		lvl = 0;
    }
	
	public String color() {
		return "§" + cc;
	}

	public WXYZ getCloseResp(final WXYZ to, final boolean rnd) {
		WXYZ rsp = null;
		int dst = Integer.MAX_VALUE;
		for (final Build b : blds) {
			if (b.type() == BuildType.NEXUS) {
				final int d = b.cLoc().dist2DSq(to);
				if (d < dst || rsp == null) {
					rsp = b.cLoc();
					dst = d;
				}
			}
		}
		return rsp == null ? null : (rnd ?
			new WXYZ(rsp.w, rsp.x + ApiOstrov.rndSignNum(BuildType.maxXZ, 3),
				rsp.y + 1, rsp.z + ApiOstrov.rndSignNum(BuildType.maxXZ, 3)) : rsp.clone());
	}

	public WXYZ getCloseBld(final WXYZ to, final boolean rnd) {
		WXYZ rsp = null;
		int dst = Integer.MAX_VALUE;
		for (final Build b : blds) {
			final int d = b.cLoc().dist2DSq(to);
			if (d < dst || rsp == null) {
				rsp = b.cLoc();
				dst = d;
			}
		}
		return rsp == null ? null : (rnd ?
			new WXYZ(rsp.w, rsp.x + ApiOstrov.rndSignNum(BuildType.maxXZ, 3),
				rsp.y + 1, rsp.z + ApiOstrov.rndSignNum(BuildType.maxXZ, 3)) : rsp.clone());
	}

	public void chgRecs(final int gld, final int dst) {
		gold += gld;
		dust += dst;
		rs.ifPlayer(p -> {
			if (gld == 0) {
				if (dst != 0) {
					((Oplayer) rs).score.getSideBar()
						.update(Arena.DUST, TCUtil.N + "Пыль: " + TCUtil.A + rs.team().dust + " 🔥");
				}
			} else {
				if (gld > 0) ApiOstrov.addStat(p, Stat.GR_gold, gld / 10);
				if (dst == 0) {
					((Oplayer) rs).score.getSideBar()
						.update(Arena.GOLD, TCUtil.N + "Золото: " + TCUtil.P + rs.team().gold + " ⛃");
				} else {
					((Oplayer) rs).score.getSideBar()
						.update(Arena.GOLD, TCUtil.N + "Золото: " + TCUtil.P + rs.team().gold + " ⛃")
						.update(Arena.DUST, TCUtil.N + "Пыль: " + TCUtil.A + rs.team().dust + " 🔥");
				}
			}
		});
	}

	public void updatePrg(final BuildType bt) {
		int lvl = 0;
		for (final Build bd : blds) {
			if (bd.type() == bt && bd.lvl() > lvl)
				lvl = bd.lvl();
		}
		lvls.put(bt, lvl);
	}

	public boolean hasBuild(final BuildType bt, final int lvl, final @Nullable List<String> lrs) {
		if (lvls.getOrDefault(bt, 0) >= lvl) {
			if (lrs != null) lrs.add("§a✔ §eПостроено: " + bt.nm + " ур." + lvl);
			return true;
		} else {
			if (lrs != null) lrs.add("§c❌ §eНужно: " + bt.nm + " ур." + lvl);
			return false;
		}
	}

	public boolean isEnemy(final LivingEntity le) {
		final Rusher rs = Rusher.getRusher(le);
		final Nexus nx;
		if (rs != null) {
			nx = rs.team();
		} else if (le instanceof Mob) {
			nx = MobGoal.getMobTeam((Mob) le);
		} else return false;

		return nx != null && nx.cc != cc;
	}

	public ClickableItem upgClick(final Build from, final BuildType to) {
		final List<String> lr = new ArrayList<>();
		lr.add(TCUtil.N + to.desc);
		lr.add(" ");
		final int lvl = to == from.type() ? from.lvl() + 1 : 1;
		boolean canUpg = lvl == 1 || hasBuild(BuildType.NEXUS, 1, lr);

		canUpg = (to.tier == 1 || hasBuild(BuildType.UPGRADE, to.tier - 1, lr)) && canUpg;

		lr.add(" ");
		final int gd = (int) ((to == from.type() ? to.gold >> (BuildType.maxLvl - lvl) : to.gold) * rs.race().mcst);
		if (gold < gd) {
			lr.add("§c❌ " + TCUtil.P + "-" + gd + " ⛃");
			canUpg = false;
		} else {
			lr.add("§a✔ " + TCUtil.P + "-" + gd + " ⛃");
		}

		final int dt = (int) ((to == from.type() ? to.dust >> (BuildType.maxLvl - lvl) : to.dust) * rs.race().mcst);
		if (dt != 0) {
			if (dust < dt) {
				lr.add("§c❌ " + TCUtil.A + "-" + dt + " 🔥");
				canUpg = false;
			} else {
				lr.add("§a✔ " + TCUtil.A + "-" + dt + " 🔥");
			}
		}

        lr.add(" ");
        if (canUpg) {
            lr.add("§aМожно Улучшить!");
			return ClickableItem.of(new ItemBuilder(to.getIcon(lvl)).lore(lr)
				.name(TCUtil.N + "[" + TCUtil.P + to.nm + TCUtil.N + "] Уровень: " + TCUtil.A + lvl +
					TCUtil.N + " (" + TCUtil.P + to.getProd(rs.race()) + TCUtil.N + ")").build(), e -> {
					if (to == BuildType.NEXUS && !hasBuild(to, 1, null)) rs.getEntity().setGlowing(true);
					final WXYZ lc = from.cLoc();
					from.remove(false);
					blds.remove(from);
					rs.race().build(this, lc, to, from.type() == to ? from.lvl() + 1 : 1);
					rs.ifPlayer(p -> {
						ScreenUtil.sendTitleDirect(p, "", TCUtil.N + "Прокачка Постройки '" +
							TCUtil.P + to.nm + TCUtil.N + "'", 12, 40, 12);
						p.closeInventory();
					});
					chgRecs(-gd, -dt);
				});
		} else {
            lr.add("§cНехватает Ресурсов!");
			return ClickableItem.empty(new ItemBuilder(to.getIcon(lvl)).lore(lr)
				.name(TCUtil.N + "[" + TCUtil.P + to.nm + TCUtil.N + "] Уровень: " + TCUtil.A + lvl +
					TCUtil.N + " (" + TCUtil.P + to.getProd(rs.race()) + TCUtil.N + ")").build());
		}
	}

	public ClickableItem bldClick(final WXYZ at, final BuildType bt, final BuildType sugg) {
		final List<String> lr = new ArrayList<>();
		lr.add(TCUtil.N + bt.desc);
		lr.add(" ");
		final boolean sg = bt == sugg;
		boolean canBld = bt.tier == 1 || hasBuild(BuildType.UPGRADE, bt.tier - 1, lr);
		final int lvl = 1;

		lr.add(" ");
		final int gd = (int) (bt.gold * rs.race().mcst);
		if (gold < gd) {
			lr.add("§c❌ §6-" + gd + " ⛃");
			canBld = false;
		} else {
			lr.add("§a✔ §6-" + gd + " ⛃");
		}
		final int dt = (int) (bt.dust * rs.race().mcst);
		if (dt != 0) {
			if (dust < dt) {
				lr.add("§c❌ §4-" + dt + " 🔥");
				canBld = false;
			} else {
				lr.add("§a✔ §4-" + dt + " 🔥");
			}
		}

		lr.add(" ");
		switch (bt) {
		case GOLD:
			if (sg) {
				lr.add("§a✔ " + TCUtil.P + "Золотая Жила");
			} else {
				canBld = false;
				lr.add("§c❌ " + TCUtil.P + "Золотая Жила");
			}
			break;
		case DUST:
			if (sg) {
				lr.add("§a✔ " + TCUtil.A + "Залежи Руды");
			} else {
				canBld = false;
				lr.add("§c❌ " + TCUtil.A + "Залежи Руды");
			}
			break;
		default:
			break;
		}

        lr.add(" ");
        if (canBld) {
            lr.add("§6Можно Построить!");
			return sg ? ClickableItem.of(new ItemBuilder(bt.getIcon(lvl)).lore(lr).enchant(Enchantment.MENDING)
			.name(TCUtil.N + "[" + TCUtil.P + bt.nm + TCUtil.N + "] Уровень: " + TCUtil.A + lvl +
				TCUtil.N + " (" + TCUtil.P + bt.getProd(rs.race()) + TCUtil.N + ")").build(), e -> {
				rs.race().build(this, at, bt, 1);
				rs.ifPlayer(p -> {
					ScreenUtil.sendTitleDirect(p, "", TCUtil.N + "Постройка '" + TCUtil.P + bt.nm +
							TCUtil.N + "' В Процессе", 12, 40, 12);
					p.setVelocity(p.getEyeLocation().getDirection().multiply(-1));
					p.closeInventory();
				});
				chgRecs(-gd, -dt);
			})
			: ClickableItem.of(new ItemBuilder(bt.getIcon(lvl)).lore(lr)
			.name(TCUtil.N + "[" + TCUtil.P + bt.nm + TCUtil.N + "] Уровень: " + TCUtil.A + lvl +
				TCUtil.N + " (" + TCUtil.P + bt.getProd(rs.race()) + TCUtil.N + ")").build(), e -> {
				rs.race().build(this, at, bt, 1);
				rs.ifPlayer(p -> {
					ScreenUtil.sendTitleDirect(p, "", TCUtil.N + "Постройка '" + TCUtil.P + bt.nm +
						TCUtil.N + "' В Процессе", 12, 40, 12);
					p.setVelocity(p.getEyeLocation().getDirection().multiply(-1));
					p.closeInventory();
				});
				chgRecs(-gd, -dt);
			});
		} else {
            lr.add("§4Постройка Невозможна!");
			return sg ? ClickableItem.empty(new ItemBuilder(bt.getIcon(lvl)).lore(lr).enchant(Enchantment.MENDING)
				.name(TCUtil.N + "[" + TCUtil.P + bt.nm + TCUtil.N + "] Уровень: " + TCUtil.A + lvl +
					TCUtil.N + " (" + TCUtil.P + bt.getProd(rs.race()) + TCUtil.N + ")").build())
				: ClickableItem.empty(new ItemBuilder(bt.getIcon(lvl)).lore(lr)
				.name(TCUtil.N + "[" + TCUtil.P + bt.nm + TCUtil.N + "] Уровень: " + TCUtil.A + lvl +
					TCUtil.N + " (" + TCUtil.P + bt.getProd(rs.race()) + TCUtil.N + ")").build());
		}
	}

	public void buildMap(final Player p, final Location org, final boolean canTp) {
		if (LocUtil.getClsChEnt(new WXYZ(p.getLocation()), MobGoal.FAR_RANGE, LivingEntity.class, le -> isEnemy(le)) != null) {
			ScreenUtil.sendActionBarDirect(p, "§cВы слижком близко к §4вражеской §cармии!");
			return;
		}

		p.setCooldown(canTp ? Arena.tpMap.getType() : Arena.map.getType(), 32);
		final World w = p.getWorld();
		w.playSound(org, Sound.ENTITY_SNIFFER_EAT, 2f, 0.6f);
		w.playSound(org, Sound.BLOCK_CONDUIT_ATTACK_TARGET, 2f, 1.4f);
		w.spawnParticle(Particle.PORTAL, org, 80, 0.2d, 0.6d, 0.2d);
		DisplayManager.rmvDis(p);
		for (final Build bd : blds) {
			final Location bl = bd.cLoc().getCenterLoc();
			final Location dlc = new Location(bl.getWorld(), (bl.getX() - org.getX()) * MAP_REL + org.getX(),
				(bl.getY() - org.getY()) * MAP_REL + org.getY(), (bl.getZ() - org.getZ()) * MAP_REL + org.getZ());
			p.spawnParticle(Particle.BUBBLE_POP, dlc, 4, 0.2d, 0.2d, 0.2d, 0d);
			DisplayManager.fakeItemAnimate(p, dlc).setRotate(false).setItem(new ItemStack(bd.type().getIcon(bd.lvl())))
				.setName(rs.race().clr + "Ур." + bd.lvl() + " " + TCUtil.P + bd.type().nm + " " +
					bd.prcHlth() + (canTp ? TCUtil.N + " [" + TCUtil.P + "ТП" + TCUtil.N + "]" : ""))
				.setNameVis(false).setScale(0.6f).setFollow(false).setOnClick((pl, dis) -> {
					DisplayManager.rmvDis(pl);
					if (!canTp) return;
					bl.add(0d, BuildType.maxY, 0d);
					w.spawnParticle(Particle.REVERSE_PORTAL, pl.getLocation(), 80, 0.2d, 0.6d, 0.2d, 1d);
					w.spawnParticle(Particle.WITCH, bl, 40, 0.4d, 0.6d, 0.4d, 1d);
					w.playSound(bl, Sound.ENTITY_GLOW_SQUID_SQUIRT, 2f, 1.4f);
					final Location lc = pl.getLocation();
					pl.teleport(bl);
					pl.setCooldown(Arena.tpMap.getType(), TP_CD_TICK);
					for (final Mob mb : LocUtil.getChEnts(new WXYZ(lc), MobGoal.FAR_RANGE, Mob.class, mb -> {
						final Nexus on = MobGoal.getMobTeam(mb); return on != null && on.cc == cc;
					})) mb.teleport(bl.clone().add(mb.getLocation().subtract(lc)));
				}).setOnLook((pl, dis) -> {
					dis.setItem(new ItemStack(bd.type().getIcon(bd.lvl())))
						.setName(rs.race().clr + "Ур." + bd.lvl() + " " + TCUtil.P + bd.type().nm + " " +
							bd.prcHlth() + (canTp ? TCUtil.N + " [" + TCUtil.P + "ТП" + TCUtil.N + "]" : ""));
				}).setIsDone(tk -> tk == 1000 || p.isSneaking() || !blds.contains(bd)).create();
		}
		/*ApiOstrov.sendBossbarDirect(p, TCUtil.P + "Создание Карты (" + TCUtil.A + MAP_SEC +
			" сек" + TCUtil.P + ")", MAP_SEC, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
		Ostrov.sync(() -> {
			genMap = false;
			if (rs.team() != null && p.getEyeLocation().distanceSquared(org) < 1d) {
			} else {
				w.playSound(org, Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 1f);
				ScreenUtil.sendTitleDirect(p, " ", "§cВы не стояли на месте " +
					TCUtil.A + MAP_SEC + " сек", 4, 20, 8);
			}
		}, MAP_SEC * 20);*/
	}
}
