package gr.Romindous.game.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import gr.Romindous.Main;
import gr.Romindous.game.map.WXYZ;
import gr.Romindous.type.BuildType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;

public class Nexus {

	public final Rusher rs;
	public final ChatColor clr;
	public final HashSet<Build> blds;
	
	public boolean alive;
	public int gold;
	public int dust;
	public float mobDmg;
	public float mobKb;
	public float mobSpd;
	public float mobHp;
	public float mobDfs;
	public float mobAs;
	
	public Nexus(final Rusher rs, final ChatColor clr) {
		this.rs = rs;
		this.clr = clr;
		this.blds = new HashSet<>();
		alive = true;
		gold = 0;
		dust = 0;
		mobDmg = 1f;
		mobKb = 1f;
		mobSpd = 1f;
		mobHp = 1f;
		mobDfs = 1f;
		mobAs = 1f;
	}

	public String name(final boolean wTClr) {
		switch (clr) {
		case BLACK:
			return wTClr ? "§0Черная" : "Черная";
		case DARK_BLUE:
			return wTClr ? "§1Темно-Синяя" : "Темно-Синяя";
		case DARK_GREEN:
			return wTClr ? "§2Зеленая" : "Зеленая";
		case DARK_AQUA:
			return wTClr ? "§3Бирюзовая" : "Бирюзовая";
		case DARK_RED:
			return wTClr ? "§4Бардовая" : "Бардовая";
		case DARK_PURPLE:
			return wTClr ? "§5Пурпурная" : "Пурпурная";
		case GOLD:
			return wTClr ? "§6Золотая" : "Золотая";
		case GRAY:
			return wTClr ? "§7Серая" : "Серая";
		case DARK_GRAY:
			return wTClr ? "§8Темно-Серая" : "Темно-Серая";
		case BLUE:
			return wTClr ? "§9Синяя" : "Синяя";
		case GREEN:
			return wTClr ? "§aЛаймовая" : "Лаймовая";
		case AQUA:
			return wTClr ? "§bГолубая" : "Голубая";
		case RED:
			return wTClr ? "§cКрасная" : "Красная";
		case LIGHT_PURPLE:
			return wTClr ? "§dРозовая" : "Розовая";
		case YELLOW:
			return wTClr ? "§eЖелтая" : "Желтая";
		case WHITE:
			return wTClr ? "§fБелая" : "Белая";
		default:
			return "";
		}
	}

	public WXYZ getCloseResp(final WXYZ to) {
		WXYZ rsp = null;
		int dst = Integer.MAX_VALUE;
		for (final Build b : blds) {
			if (b.type == BuildType.NEXUS) {
				final int d = b.cLoc.dist2DSq(to);
				if (d < dst || rsp == null) {
					rsp = b.cLoc;
					dst = d;
				}
			}
		}
		return rsp == null ? null : 
			new WXYZ(rsp.w, rsp.x + Main.rngFrom(BuildType.maxXZ, 2), 
				rsp.y + 1, rsp.z + Main.rngFrom(BuildType.maxXZ, 2));
	}

	public WXYZ getCloseBld(final WXYZ to) {
		WXYZ rsp = null;
		int dst = Integer.MAX_VALUE;
		for (final Build b : blds) {
			final int d = b.cLoc.dist2DSq(to);
			if (d < dst || rsp == null) {
				rsp = b.cLoc;
				dst = d;
			}
		}
		return rsp == null ? null : 
			new WXYZ(rsp.w, rsp.x + Main.rngFrom(BuildType.maxXZ, 2), 
				rsp.y + 1, rsp.z + Main.rngFrom(BuildType.maxXZ, 2));
	}

	public void chgRecs(final int gld, final int dst) {
		gold += gld;
		dust += dst;
		rs.ifPlayer(p -> {
			if (gld == 0) {
				if (dst != 0) {
//					ApiOstrov.sendActionBarDirect(p, dst < 0 ? "§4" + dst + " 🔥" : "§c+" + dst + " 🔥");
					Main.chgSbdTm(p.getScoreboard(), "dust", "", "§4" + rs.team().dust + " 🔥");
				}
			} else {
				if (dst == 0) {
//					ApiOstrov.sendActionBarDirect(p, gld < 0 ? "§6" + gld + " ⛃" : "§e+" + gld + " ⛃");
					Main.chgSbdTm(p.getScoreboard(), "gold", "", "§6" + rs.team().gold + " ⛃");
				} else {
//					ApiOstrov.sendActionBarDirect(p, (gld < 0 ? "§6" + gld + " ⛃" : "§e+" + gld + " ⛃") 
//						+ (dst < 0 ? "§4" + dst + " 🔥" : "§c+" + dst + " 🔥"));
					Main.chgSbdTm(p.getScoreboard(), "gold", "", "§6" + rs.team().gold + " ⛃");
					Main.chgSbdTm(p.getScoreboard(), "dust", "", "§4" + rs.team().dust + " 🔥");
				}
			}
		});
	}

	public boolean hasBuild(final BuildType bt, final int minLvl) {
		for (final Build bd : blds) {
			if (bd.type == bt && bd.done && bd.lvl >= minLvl) return true;
		}
		return false;
	}

	public ClickableItem upgClick(final Build from, final BuildType to) {
		final List<String> lr = new ArrayList<>();
		boolean canUpg = true;
		final int lvl = to == from.type ? from.lvl + 1 : 1;
		if (lvl != 1) {
			if (hasBuild(BuildType.NEXUS, 1)) {
				lr.add("§a✔ §eПостроен Нексус");
			} else {
				lr.add("§c❌ §eПостроен Нексус");
				canUpg = false;
			}
		}
		
		switch (to) {
		case MAEGUS, SIEGER:
			if (hasBuild(BuildType.UPGRADE, 1)) {
				lr.add("§a✔ §eПостроена Кузня");
			} else {
				lr.add("§c❌ §eПостроена Кузня");
				canUpg = false;
			}
			break;
		default:
			break;
		}
		
		final int gd = to == from.type ? to.gold >> (BuildType.maxLvl - lvl) : to.gold;
		if (gold < gd) {
			lr.add("§c❌ §6-" + gd + " ⛃");
			canUpg = false;
		} else {
			lr.add("§a✔ §6-" + gd + " ⛃");
		}
		final int dt = to == from.type ? to.dust >> (BuildType.maxLvl - lvl) : to.dust;
		if (dt != 0) {
			if (dust < dt) {
				lr.add("§c❌ §4-" + dt + " 🔥");
				canUpg = false;
			} else {
				lr.add("§a✔ §4-" + dt + " 🔥");
			}
		}
		
		if (canUpg) {
			lr.add(" ");
			lr.add("§6Можно Улучшить!");
			return ClickableItem.of(new ItemBuilder(to.getIcon((byte) lvl)).lore(lr)
				.name("§6Уровень: §4" + lvl + " §6(§4" + to.getProd((byte) lvl) + "§6)").build(), e -> {
					if (to == BuildType.NEXUS && !hasBuild(to, 1)) rs.getEntity().setGlowing(true);
					final WXYZ lc = from.cLoc;
					from.remove(false);
					blds.remove(from);
					blds.add(new Build(this, lc, to, from.type == to ? from.lvl + 1 : 1));
					rs.ifPlayer(p -> {
						ApiOstrov.sendTitle(p, "", "§6Прокачка Постройки §4'" + to.nm + "'", 12, 40, 12);
						p.closeInventory();
					});
					chgRecs(-gd, -dt);
				});
		} else {
			lr.add(" ");
			lr.add("§4Нехватает Ресурсов!");
			return ClickableItem.empty(new ItemBuilder(to.getIcon((byte) lvl)).lore(lr)
				.name("§6Уровень: §4" + lvl + " §6(§4" + to.getProd((byte) lvl) + "§6)").build());
		}
	}

	public ClickableItem bldClick(final WXYZ at, final BuildType bt) {
		final List<String> lr = new ArrayList<>();
		boolean canUpg = true;
		final int lvl = 1;
		
		final int gd = bt.gold;
		if (gold < gd) {
			lr.add("§c❌ §6-" + gd + " ⛃");
			canUpg = false;
		} else {
			lr.add("§a✔ §6-" + gd + " ⛃");
		}
		final int dt = bt.dust;
		if (dt != 0) {
			if (dust < dt) {
				lr.add("§c❌ §4-" + dt + " 🔥");
				canUpg = false;
			} else {
				lr.add("§a✔ §4-" + dt + " 🔥");
			}
		}
		
		switch (bt) {
		case GOLD:
			if (at.getBlock().getRelative(BlockFace.DOWN).getType() == Material.RAW_GOLD_BLOCK) {
				lr.add("§a✔ §6Золотая Жила");
			} else {
				canUpg = false;
				lr.add("§c❌ §6Золотая Жила");
			}
			break;
		case DUST:
			if (at.getBlock().getRelative(BlockFace.DOWN).getType() == Material.DEEPSLATE_REDSTONE_ORE) {
				lr.add("§a✔ §4Залежи Руды");
			} else {
				canUpg = false;
				lr.add("§c❌ §4Залежи Руды");
			}
			break;
		default:
			break;
		}
		
		if (canUpg) {
			lr.add(" ");
			lr.add("§6Можно Построить!");
			return ClickableItem.of(new ItemBuilder(bt.getIcon((byte) lvl)).lore(lr)
				.name("§6[§4" + bt.nm + "§6] Уровень: §4" + lvl + " §6(§4" + bt.getProd((byte) lvl) + "§6)").build(), e -> {
					blds.add(new Build(this, at, bt, 1));
					rs.ifPlayer(p -> {
						ApiOstrov.sendTitle(p, "", "§6Постройка §4'" + bt.nm + "' §6В Процессе", 12, 40, 12);
						p.setVelocity(p.getEyeLocation().getDirection().multiply(-1));
						p.closeInventory();
					});
					chgRecs(-gd, -dt);
				});
		} else {
			lr.add(" ");
			lr.add("§4Постройка Невозможна!");
			return ClickableItem.empty(new ItemBuilder(bt.getIcon((byte) lvl)).lore(lr)
				.name("§6[§4" + bt.nm + "§6] Уровень: §4" + lvl + " §6(§4" + bt.getProd((byte) lvl) + "§6)").build());
		}
	}
}
