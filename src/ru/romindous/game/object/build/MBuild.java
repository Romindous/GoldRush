package ru.romindous.game.object.build;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.Schematic;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.Main;
import ru.romindous.game.Arena;
import ru.romindous.game.goal.MobGoal;
import ru.romindous.game.map.LocData;
import ru.romindous.game.object.Build;
import ru.romindous.game.object.Nexus;
import ru.romindous.type.BuildType;
import ru.romindous.type.RaceType;
import ru.romindous.type.Upgrade;

import java.util.*;

public class MBuild implements Build {

	public static final float SPIRE_DMG = 8f;

	private static final int GOLDi = 60;
	private static final int DUSTi = 24;
	private static final double STEP = 0.4d;

	private final HashSet<WXYZ> blks;

	private final Nexus tm;
	private final WXYZ cLoc;
	private final RaceType race;
	private final BuildType type;
	private final byte lvl;
	private final int cld;

	private int tick;
	private int health;
	private boolean done;

	public MBuild(final Nexus tm, final WXYZ cLoc, final BuildType bt, final int lvl) {
		this.tm = tm;
		this.cLoc = cLoc;
		this.type = bt;
		this.lvl = (byte) lvl;
		tick = 0;

		this.race = tm.rs.race();
		this.cld = (int) (type.cld * race.mcd);
		health = (int) (bt.hp * lvl * race.mbhp);

		final Schematic sch = bt.getSchem(race, lvl);
		final int width = (BuildType.maxXZ << 1) + 1, dX = Math.min(width, sch.getSizeX()),
			dY = Math.min(BuildType.maxY, sch.getSizeY()), dZ = Math.min(width, sch.getSizeZ());
		final WXYZ pos = cLoc.clone().add(-(dX >> 1), 0, -(dZ >> 1));
		final LinkedList<LocData> lds = new LinkedList<>();
		final World w = pos.w;
		final String wnm = w.getName();
		final String bclr = TCUtil.getDyeColor(tm.txc).name();
		for (int x = 0; x != dX; x++) {
			for (int z = 0; z != dZ; z++) {
				for (int y = 0; y != dY; y++) {
					final XYZ lc = new XYZ(wnm, y, x, z);
					final Material mt = sch.getMaterial(lc);
					if (mt == null || mt.isAir()) continue;
					final BlockData bd = sch.getBlockData(lc);
					lds.add(bd == null ? new LocData(new WXYZ(w, pos.x + x, pos.y + y, pos.z + z), clrMat(mt, bclr))
							: new LocData(new WXYZ(w, pos.x + x, pos.y + y, pos.z + z), bd));
				}
			}
		}
		saves.set(new XYZ(cLoc.getCenterLoc()).toString(), new XYZ(cLoc.w.getName(), dX, dY, dZ).toString());
		saves.saveConfig();

		final Arena ar = tm.rs.arena();
		final WXYZ top = new WXYZ(w, 1 + ((pos.x + dX) >> BuildType.dsLoc),
				1 + ((pos.y + dY) >> BuildType.dsLoc), 1 + ((pos.z + dZ) >> BuildType.dsLoc));
		for (int x = pos.x >> BuildType.dsLoc; x < top.x; x++) {
			for (int z = pos.z >> BuildType.dsLoc; z < top.z; z++) {
				for (int y = pos.y >> BuildType.dsLoc; y < top.y; y++) {
					ar.blocs.putIfAbsent(new WXYZ(w, x, y, z).getSLoc(), this);
				}
			}
		}

		Collections.shuffle(lds, Main.srnd);
		w.playSound(cLoc.getCenterLoc(), Sound.BLOCK_CHAIN_BREAK, 2f, 0.6f);
		tm.blds.add(this);
		tm.updatePrg(type);

		done = false;
		blks = new HashSet<>();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (lds.isEmpty() || health == 0) {
					done = true;
					cancel();
					return;
				}

				final LocData ld = lds.removeFirst();
				blks.add(ld.loc);
				if (ld.bd == null) {
					w.playSound(ld.loc.getCenterLoc(), Sound.BLOCK_DEEPSLATE_BRICKS_PLACE, 2f, 0.8f);
					ld.loc.getBlock().setType(ld.mt, false);
				} else {
					w.playSound(ld.loc.getCenterLoc(), ld.bd.getSoundGroup().getPlaceSound(), 1f, 0.8f);
					ld.loc.getBlock().setBlockData(ld.bd, false);
				}
			}
		}.runTaskTimer(Main.plug, 0, 2);
	}

	private Material clrMat(final Material mt, final String bclr) {
		final String mtn = mt.name();
		if (mtn.startsWith(dft)) {
			return Material.getMaterial(bclr + mtn.substring(dft.length()));
		}
		return mt;
	}

	@Override
	public Nexus tm() {return tm;}

	@Override
	public WXYZ cLoc() {return cLoc;}

	@Override
	public byte lvl() {return lvl;}

	@Override
	public BuildType type() {return type;}

	@Override
	public RaceType race() {return race;}

	@Override
	public HashSet<WXYZ> blks() {return blks;}

	@Override
	public boolean done() {return done;}

	@Override
	public String prcHlth() {
		final int pc = health * 100 / (int) (type.hp * lvl * race.mbhp);
		return switch (pc / 10) {
			case 10, 9, 8, 7 -> "§a" + pc + "%";
			case 6, 5, 4, 3 -> "§e" + pc + "%";
			default -> "§c" + pc + "%";
		};
	}

	public boolean damage(final int dmg) {
		if (dmg < 1) return false;
		if (dmg < health) {
			health -= dmg;
			int cnt = (int) ((float) dmg * 0.8f * blks.size() / (type.hp * lvl)) - 1;
			final Iterator<WXYZ> it = blks.iterator();
			while (it.hasNext()) {
				final WXYZ p = it.next();
				if ((cnt--) < 0) break;
				final Location loc = p.getCenterLoc();
				final Block b = loc.getBlock();
				final BlockData bd = b.getBlockData();
				cLoc.w.spawnParticle(Particle.BLOCK, loc, 20, 0.4d, 0.4d, 0.4d, 0.2d, bd, false);
				cLoc.w.playSound(loc, bd.getSoundGroup().getBreakSound(), 1f, 0.8f);
				b.setType(Material.AIR, false);
				it.remove();
			}
			tm.rs.ifPlayer(p -> {
				ScreenUtil.sendTitleDirect(p, "", TCUtil.N + "Постройка " +
					TCUtil.P + type.nm + TCUtil.N + " Под Осадой!", 12, 40, 12);
			});
			return false;
		} else {
			tm.blds.remove(this);
			remove(false);
			if (tm.blds.isEmpty()) {
				tm.rs.arena().killTeam(tm);
			}
			return true;
		}
	}

	public void remove(final boolean pay) {
		health = 0;
		for (final WXYZ p : blks) {
			final Location loc = p.getCenterLoc();
			final Block b = loc.getBlock();
			final BlockData bd = b.getBlockData();
			cLoc.w.spawnParticle(Particle.BLOCK, loc, 20, 0.4d, 0.4d, 0.4d, 0.2d, bd, false);
			cLoc.w.playSound(loc, bd.getSoundGroup().getBreakSound(), 1f, 0.8f);
			b.setType(Material.AIR, false);
		}
		saves.removeKey(new XYZ(cLoc.getCenterLoc()).toString());
		saves.saveConfig();

		tm.updatePrg(type);
		if (tm.rs.arena() != null) {
			final Arena ar = tm.rs.arena();
			ar.lobby.w.playSound(cLoc.getCenterLoc(), Sound.BLOCK_DECORATED_POT_SHATTER, 2f, 0.6f);
			final int width = (BuildType.maxXZ << 1) + 1;
			final WXYZ pos = cLoc.clone().add(-(width >> 1), 0, -(width >> 1));
			final WXYZ top = new WXYZ(pos.w, 1 + ((pos.x + width) >> BuildType.dsLoc),
					1 + ((pos.y + BuildType.maxY) >> BuildType.dsLoc), 1 + ((pos.z + width) >> BuildType.dsLoc));
			for (int x = pos.x >> BuildType.dsLoc; x < top.x; x++) {
				for (int z = pos.z >> BuildType.dsLoc; z < top.z; z++) {
					for (int y = pos.y >> BuildType.dsLoc; y < top.y; y++) {
						ar.blocs.remove(new WXYZ(pos.w, x, y, z).getSLoc(), this);
					}
				}
			}
		}
		if (type == BuildType.NEXUS && !tm.hasBuild(type, 1, null)) tm.rs.getEntity().setGlowing(false);
		final StringBuilder sb = new StringBuilder();
		for (final Upgrade up : Upgrade.values()) {
			if (!tm.hasBuild(up.bt, up.lvl, null) && up.remFor(tm.rs)) {
				sb.append(TCUtil.N).append("\n - ").append(Upgrade.CLR).append(up.name);
			}
		}

		tm.rs.ifPlayer(p -> {
			if (!sb.isEmpty()) p.sendMessage(Main.PRFX + "Потеряны улучшения:" + sb.toString());
			ScreenUtil.sendTitleDirect(p, "", TCUtil.N + "Постройка " +
				TCUtil.A + type.nm + TCUtil.N + " Разрушена!", 12, 40, 12);
		});

		if (pay) tm.chgRecs(getGCost(), getDCost());

	}

	public boolean act() {
		if (!done || (tick++) % cld != 0) return false;
		tick += Main.srnd.nextInt(Math.max(cld >> 1, 1));

		if (type.close && cLoc.distSq(tm.rs.getEntity().getLocation()) > ACT_DST_SQ) return false;
		final Location loc = cLoc.getCenterLoc();
		switch (type) {
		case NEXUS:
			tm.chgRecs(GOLDi * (lvl + 2), 0);
			Build.animateAct(loc, 120, 1.6d, Sound.BLOCK_NETHER_GOLD_ORE_HIT, 0.6f, Material.GOLD_BLOCK.createBlockData(), 5);
			break;
		case GOLD:
			tm.chgRecs(GOLDi * lvl, 0);
			Build.animateAct(loc, 80, 1d, Sound.BLOCK_GILDED_BLACKSTONE_BREAK, 0.6f, Material.RAW_GOLD_BLOCK.createBlockData(), 3);
			break;
		case DUST:
			tm.chgRecs(0, DUSTi * lvl);
			Build.animateAct(loc, 60, 0.6d, Sound.BLOCK_WART_BLOCK_BREAK, 0.6f, Material.REDSTONE_BLOCK.createBlockData(), 2);
			break;
		case BARRACK:
			Build.animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_TOOLSMITH, 0.6f, Material.POLISHED_DIORITE.createBlockData(), 4);
			switch (lvl) {
			case 1:
				spawnMob(loc, EntityType.ZOMBIE_VILLAGER, "Зомбi ур.1", Material.WOODEN_SWORD, null, Material.LEATHER_HELMET, 
					Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
				break;
			case 2:
				spawnMob(loc, EntityType.ZOMBIE_VILLAGER, "Зомбi ур.2", Material.STONE_SWORD, Material.SHIELD, Material.CHAINMAIL_HELMET, 
					Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
				break;
			case 3:
				spawnMob(loc, EntityType.ZOMBIE_VILLAGER, "Зомбi ур.3", Material.IRON_SWORD, Material.SHIELD, Material.IRON_HELMET, 
					Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
				break;
			default:
				return false;
			}
			break;
		case RANGE:
			Build.animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_FLETCHER, 0.8f, Material.TARGET.createBlockData(), 2);
			switch (lvl) {
			case 1:
				spawnMob(loc, EntityType.STRAY, "Зимогор ур.1", Material.BOW, null, Material.LEATHER_HELMET, 
					Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
				break;
			case 2:
				spawnMob(loc, EntityType.STRAY, "Зимогор ур.2", Material.BOW, null, Material.CHAINMAIL_HELMET, 
					Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
				break;
			case 3:
				spawnMob(loc, EntityType.STRAY, "Зимогор ур.3",Material.BOW, null, Material.IRON_HELMET, 
					Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
				break;
			default:
				return false;
			}
			break;
		case ALTAR:
			Build.animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_CLERIC, 0.6f, Material.BREWING_STAND.createBlockData(), 3);
			spawnMob(loc, EntityType.ENDERMAN, "Эндер ур." + String.valueOf(lvl), null, null, null, null, null, null);
			break;
		case CAMP:
			Build.animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_WEAPONSMITH, 0.6f, Material.SMITHING_TABLE.createBlockData(), 3);
			spawnMob(loc, EntityType.SPIDER, "Павук ур." + String.valueOf(lvl), null, null, null, null, null, null);
			break;
		case TRIBUNE:
			Build.animateAct(loc, 60, 0.4d, Sound.ENTITY_VILLAGER_WORK_MASON, 0.6f, Material.PISTON.createBlockData(), 3);
			spawnMob(loc, EntityType.CREEPER, "Крипер ур." + String.valueOf(lvl), null, null, null, null, null, null);
			break;
		case UPGRADE:
			Build.animateAct(loc, 80, 1.0d, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, Material.ENCHANTING_TABLE.createBlockData(), 3);
			tm.uptm++;
			switch (lvl) {
			case 3:
				tm.mobDmg += 0.4f / tm.uptm;
				tm.mobKb += 0.2f / tm.uptm;
			case 2:
				tm.mobSpd += 0.04f / tm.uptm;
				tm.mobCd += 0.1f / tm.uptm;
			case 1:
				tm.mobAr += 0.06f / tm.uptm;
				tm.mobHp += 0.2f / tm.uptm;
			default:
				break;
			}
			break;
		case SPIRE:
			loc.add(0d, BuildType.maxY >> 1, 0d);
			final ArrayList<LivingEntity> near = new ArrayList<>();
			final LivingEntity le = LocUtil.getClsChEnt(new WXYZ(loc), MobGoal.FAR_RANGE, Mob.class, e -> {
				if (tm.isEnemy(e)) {
					near.add(e);
					return true;
				}
				return false;
			});

			if (le != null) {
				final LivingEntity re = tm.rs.getEntity();
				final double dmg = tm.rs.mdDmg(SPIRE_DMG * (SPIRE_LB * lvl + 1f));
				loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 40, 0.4d, 0.4d, 0.4d, 0.2d);
				loc.getWorld().playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_HURT, 2f, 1.6f);
				final Vector vc = le.getEyeLocation().subtract(loc).toVector().normalize();
				loc.add(vc); vc.multiply(STEP);
				for (int i = (int) (MobGoal.FAR_RANGE / STEP); i != 0; i--) {
					loc.add(vc);
					loc.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, loc, 4, 0.2d, 0.2d, 0.2d, 0d);
					for (final LivingEntity e : near) {
						if (e.getBoundingBox().contains(loc.getX(), loc.getY(), loc.getZ())) {
							e.damage(dmg, re);
						}
					}
				}
			}
			break;
		}
		return true;
	}
	
	public int hp() {
		return switch (type) {
		case BARRACK -> 2 * lvl + 14;
		case RANGE -> 1 * lvl + 12;
		case ALTAR -> 4 * lvl + 14;
		case CAMP -> 2 * lvl + 8;
		case TRIBUNE -> 5 * lvl + 20;
		default -> 10;};
	}
	
	public float ar() {
		return switch (type) {
		case BARRACK -> 0.01f * lvl + 0.12f;
		case RANGE -> 0.02f * lvl + 0.06f;
		case ALTAR -> 0.02f * lvl + 0.02f;
		case CAMP -> 0.02f * lvl + 0.08f;
		case TRIBUNE -> 0.02f * lvl + 0.14f;
		default -> 0f;};
	}
	
	public float spd() {
		return switch (type) {
		case BARRACK -> 0.02f * lvl + 1.20f;
		case RANGE -> 0.02f * lvl + 1.12f;
		case ALTAR -> 0.01f * lvl + 1.08f;
		case CAMP -> 0.04f * lvl + 1.10f;
		case TRIBUNE -> 0.04f * lvl + 1.14f;
		default -> 1.00f;};
	}
	
	public float dmg() {
		return switch (type) {
		case BARRACK -> 0.5f * lvl + 1.0f;
		case RANGE -> 1.0f * lvl + 1.5f;
		case ALTAR -> 1.0f * lvl + 3.5f;
		case CAMP -> 1.0f * lvl + 1.0f;
		case TRIBUNE -> 2.5f * lvl + 8.0f;
		default -> 1.0f;};
	}
	
	public float cd() {
		return switch (type) {
		case BARRACK -> 0.1f * lvl + 1.2f;
		case RANGE -> 0.2f * lvl + 0.6f;
		case ALTAR -> 0.1f * lvl + 0.4f;
		case CAMP -> 0.1f * lvl + 0.8f;
		case TRIBUNE -> 0.10f * lvl + 0.5f;
		default -> 1.0f;};
	}
	
	public float kb() {
		return switch (type) {
		case BARRACK -> 0.1f * lvl + 0.4f;
		case RANGE -> 0.0f * lvl + 0.5f;
		case ALTAR -> 0.2f * lvl + 0.6f;
		case CAMP -> 0.1f * lvl + 0.4f;
		case TRIBUNE -> 0.4f * lvl + 0.6f;
		default -> 0.0f;};
	}

	public int getGCost() {
		int gld = 0;
		for (int i = 0; i != lvl; i++) {
			gld += type.gold >> (i + REF_DEL);
		}
		return (int) (gld * race.mcst);
	}

	public int getDCost() {
		int dst = 0;
		for (int i = 0; i != lvl; i++) {
			dst += type.dust >> (i + REF_DEL);
		}
		return (int) (dst * race.mcst);
	}

	public Mob spawnMob(final Location loc, final EntityType etp, final String name, final Material hand,
			final Material ofhd, final Material helm, final Material chest, final Material legs, final Material boots) {
		final Mob mb = (Mob) loc.getWorld().spawnEntity(loc.add(ApiOstrov.rndSignNum(BuildType.maxXZ, 2),
				1, ApiOstrov.rndSignNum(BuildType.maxXZ, 2)), etp, false);
		mb.customName(TCUtil.form(tm.color() + name)); mb.setCustomNameVisible(false);
		mb.setRemoveWhenFarAway(false); mb.setPersistent(true); final EntityEquipment eq = mb.getEquipment();
		if (hand != null) eq.setItemInMainHand(new ItemBuilder(hand).unbreak(true).build(), false);
		if (ofhd != null) eq.setItemInOffHand(new ItemBuilder(ofhd).unbreak(true).build(), false);
		if (helm != null) eq.setHelmet(new ItemBuilder(helm).unbreak(true).build(), false);
		if (chest != null) eq.setChestplate(new ItemBuilder(chest).unbreak(true).build(), false);
		if (legs != null) eq.setLeggings(new ItemBuilder(legs).unbreak(true).build(), false);
		if (boots != null) eq.setBoots(new ItemBuilder(boots).unbreak(true).build(), false);

		if (mb instanceof final PiglinAbstract pa) pa.setImmuneToZombification(true);
		if (mb instanceof final Hoglin hg) hg.setImmuneToZombification(true);

		mgs.removeAllGoals(mb);
		mgs.addGoal(mb, 0, type.goal(mb, tm, (int) (hp() * tm.mobHp), ar() * tm.mobAr,
			spd() * tm.mobSpd, dmg() * tm.mobDmg, cd() * tm.mobCd, kb() * tm.mobKb));
		return mb;
	}

	public ItemStack getInfoItem() {
		final ItemBuilder ib = new ItemBuilder(Material.CAMPFIRE)
				.name(TCUtil.N + "[" + TCUtil.P + type.nm + TCUtil.N + "] Уровня: " + TCUtil.A + lvl).amount(lvl);
		final LinkedList<String> lr = new LinkedList<>();
		lr.add(TCUtil.N + "Здоровье: §c" + health + TCUtil.N + "/§c" + (int) (type.hp * lvl * race.mbhp));
		lr.add(" ");
		switch (type) {
			case NEXUS:
				lr.add(TCUtil.N + "Точка " + TCUtil.P + "спавна " + TCUtil.N + "комманды!");
				lr.add("§eДобывает: " + TCUtil.P + (GOLDi * (lvl + 1)) + " ⛃ §eза §c" + cld + " сек");
				break;
			case GOLD:
				lr.add("§eДобывает: " + TCUtil.P + (GOLDi * lvl) + " ⛃ §eза §c" + cld + " сек");
				break;
			case DUST:
				lr.add("§eДобывает: " + TCUtil.A + (DUSTi * lvl) + " 🔥 §eза §c" + cld + " сек");
				break;
			case SPIRE:
				lr.add("§eНаносит " + StringUtil.toSigFigs((float) tm.rs.mdDmg(SPIRE_DMG), (byte) 1) + " 💢 §eвраждебным");
				lr.add("§eсуществам в линии, каждые §c" + cld + " сек");
				break;
			case UPGRADE:
				lr.add("§eЭффективность - " + TCUtil.P + StringUtil.toSigFigs(100f / tm.uptm, (byte) 2) + "%");
				lr.add("§eКаждые §с" + cld + " сек§e, повышает:");
				switch (lvl) {
					case 3:
						lr.add("§e- §cУрон §e(сейчас §c+" + Build.getRelInc(tm.mobDmg) + " §e)");
						lr.add("§e- §сОтдачу §e(сейчас §с+" + Build.getRelInc(tm.mobKb) + " §e)");
					case 2:
						lr.add("§e- §мСкорость §e(сейчас §м+" + Build.getRelInc(tm.mobSpd) + " §e)");
						lr.add("§e- §6Ловкость §e(сейчас §6+" + Build.getRelInc(tm.mobCd) + " §e)");
					case 1:
						lr.add("§e- §кЗдоровье §e(сейчас §к+" + Build.getRelInc(tm.mobHp) + " §e)");
						lr.add("§e- §bЗащиту §e(сейчас §b+" + Build.getRelInc(tm.mobAr) + " §e)");
					default:
						break;
				}
				break;
			default:
				lr.add("§eПризывает: " + tm.color() + type.getProd(race) + " §eкаждые §c" + type.cld + " сек");
				lr.add("§eХарактеристики:");
				lr.add(TCUtil.N + "- Здоровье: §к" + (int) (hp() * tm.mobHp) + " ❤");
				lr.add(TCUtil.N + "- Защита: §b" + (int) (ar() * tm.mobAr * 100f) + "% 🛡");
				lr.add(TCUtil.N + "- Скорость: §м" + StringUtil.toSigFigs(spd() * tm.mobSpd, (byte) 2) + " ⯮");
				lr.add(TCUtil.N + "- Урон: §c" + StringUtil.toSigFigs(dmg() * tm.mobDmg, (byte) 1) + " 💢");
				lr.add(TCUtil.N + "- Атакует раз в §6" + StringUtil.toSigFigs(0.2f / (cd() * tm.mobCd), (byte) 1) + " сек");
				lr.add(TCUtil.N + "- Сила отброса: §с" + (int) (kb() * tm.mobKb * 10f) + " ◎");
				break;
		}
		return ib.lore(lr).build();
	}

	public boolean has(final WXYZ loc) {
		return blks.contains(loc);
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof Build && ((Build) o).cLoc().equals(cLoc);
	}

	@Override
	public int hashCode() {
		return cLoc.hashCode();
	}
}
