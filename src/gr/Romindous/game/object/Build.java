package gr.Romindous.game.object;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.destroystokyo.paper.entity.ai.MobGoals;

import gr.Romindous.Main;
import gr.Romindous.game.goal.KillerGoal;
import gr.Romindous.game.goal.KnightGoal;
import gr.Romindous.game.goal.MaegusGoal;
import gr.Romindous.game.goal.RangerGoal;
import gr.Romindous.game.goal.SiegerGoal;
import gr.Romindous.game.map.LocData;
import gr.Romindous.game.map.WXYZ;
import gr.Romindous.type.BuildType;
import net.kyori.adventure.text.Component;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.Schematic;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;

public class Build {

	public final Nexus tm;
	public final WXYZ cLoc;
	public final BuildType type;
	public final HashSet<WXYZ> blks;
	public final byte lvl;
	
	public int tick;
	public int health;
	public boolean done;
	
	private static final MobGoals mgs = Bukkit.getMobGoals();
	
	public Build(final Nexus tm, final WXYZ cLoc, final BuildType bt, final int lvl) {
		this.tm = tm;
		this.cLoc = cLoc;
		this.type = bt;
		this.lvl = (byte) lvl;
		health = bt.hp * lvl;
		tick = 0;
		
		final Schematic sch = bt.getSchem(lvl);
		final int dX = sch.getSizeX(), dZ = sch.getSizeZ();
		final XYZ pos = cLoc.add(0 - (dX >> 1), 0, 0 - (dZ >> 1));
		final LinkedList<LocData> lds = new LinkedList<>();
		final World w = cLoc.w;
		final String wnm = w.getName();
		for (int x = 0; x < dX; x++) {
			for (int z = 0; z < dZ; z++) {
				for (int y = 0; y < sch.getSizeY(); y++) {
					final XYZ lc = new XYZ(wnm, x, y, z);
					final Material mt = sch.getMaterial(lc);
					if (mt == null || mt.isAir()) continue;
					final BlockData bd = sch.getBlockData(lc);
					lds.add(bd == null ? new LocData(new WXYZ(w, pos.x + x, pos.y + y, pos.z + z), mt) 
						: new LocData(new WXYZ(w, pos.x + x, pos.y + y, pos.z + z), bd));
				}
			}
		}
		
		Collections.shuffle(lds, Main.srnd);
		w.playSound(cLoc.getCenterLoc(), Sound.BLOCK_CHAIN_BREAK, 2f, 0.6f);
		
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
		}.runTaskTimer(Main.plug, 0, 4);
	}
	
	public boolean damage(final int dmg) {
		if (dmg == 0) return false;
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
				cLoc.w.spawnParticle(Particle.BLOCK_CRACK, loc, 20, 0.4d, 0.4d, 0.4d, 0.2d, bd, false);
				cLoc.w.playSound(loc, bd.getSoundGroup().getBreakSound(), 1f, 0.8f);
				b.setType(Material.AIR, false);
				it.remove();
			}
			tm.rs.ifPlayer(p -> {
				ApiOstrov.sendTitle(p, "", "§6Постройка §4" + type.nm + " §6Под Осадой!", 12, 40, 12);
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
			cLoc.w.spawnParticle(Particle.BLOCK_CRACK, loc, 20, 0.4d, 0.4d, 0.4d, 0.2d, bd, false);
			cLoc.w.playSound(loc, bd.getSoundGroup().getBreakSound(), 1f, 0.8f);
			b.setType(Material.AIR, false);
		}
		if (type == BuildType.NEXUS && !tm.hasBuild(type, 1)) tm.rs.getEntity().setGlowing(false);
		tm.rs.ifPlayer(p -> {
			ApiOstrov.sendTitle(p, "", "§6Постройка §4" + type.nm + " §6Разрушена!", 12, 40, 12);
		});
		
		if (pay) {
			tm.chgRecs(getGCost(), getDCost());
		}
	}

	public int getGCost() {
		int gld = 0;
		for (int i = 0; i < lvl; i++) {
			gld += type.gold >> (BuildType.maxLvl - i);
		}
		return gld;
	}

	public int getDCost() {
		int dst = 0;
		for (int i = 0; i < lvl; i++) {
			dst += type.dust >> (BuildType.maxLvl - i);
		}
		return dst;
	}

	public void act() {
		final Location loc = cLoc.getCenterLoc();
		switch (type) {
		case NEXUS:
			tm.chgRecs(80 * lvl, 0);
			animateAct(loc, 120, 1.6d, Sound.BLOCK_NETHER_GOLD_ORE_HIT, 0.6f, Material.GOLD_BLOCK.createBlockData(), 5);
			break;
		case GOLD:
			tm.chgRecs(60 * lvl, 0);
			animateAct(loc, 80, 1d, Sound.BLOCK_GILDED_BLACKSTONE_BREAK, 0.6f, Material.RAW_GOLD_BLOCK.createBlockData(), 3);
			break;
		case DUST:
			tm.chgRecs(0, 20 * lvl);
			animateAct(loc, 60, 0.6d, Sound.BLOCK_WART_BLOCK_BREAK, 0.6f, Material.REDSTONE_BLOCK.createBlockData(), 2);
			break;
		case KNIGHT:
			animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_TOOLSMITH, 0.6f, Material.POLISHED_DIORITE.createBlockData(), 4);
			switch (lvl) {
			case 1:
				spawnMob(loc, EntityType.DROWNED, 16, 0.12f, "Моряк", 1.22f, Material.WOODEN_SWORD, null, Material.LEATHER_HELMET, 
					Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, 1.5f, 0.4f, 0.4f);
				break;
			case 2:
				spawnMob(loc, EntityType.ZOMBIE_VILLAGER, 20, 0.125f, "Зомбi", 1.24f, Material.STONE_SWORD, Material.SHIELD, Material.CHAINMAIL_HELMET, 
					Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, 2.0f, 0.4f, 0.4f);
				break;
			case 3:
				spawnMob(loc, EntityType.HUSK, 24, 0.125f, "Погребенный", 1.24f, Material.IRON_SWORD, Material.SHIELD, Material.IRON_HELMET, 
					Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, 2.5f, 0.4f, 0.4f);
				break;
			default:
				return;
			}
			break;
		case RANGER:
			animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_FLETCHER, 0.8f, Material.TARGET.createBlockData(), 2);
			switch (lvl) {
			case 1:
				spawnMob(loc, EntityType.SKELETON, 12, 0.10f, "Скiлет", 1.22f, Material.BOW, null, Material.LEATHER_HELMET, 
					Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, 2.5f, 0.2f, 2f);
				break;
			case 2:
				spawnMob(loc, EntityType.STRAY, 14, 0.12f, "Зимогор", 1.24f, Material.BOW, null, Material.CHAINMAIL_HELMET, 
					Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, 3.5f, 0.2f, 2f);
				break;
			case 3:
				spawnMob(loc, EntityType.PILLAGER, 16, 0.14f, "Розбiйник", 0.96f, 
					Material.CROSSBOW, null, null, null, null, null, 2.5f, 0.25f, 2.5f);
				break;
			default:
				return;
			}
			break;
		case MAEGUS:
			animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_CLERIC, 0.6f, Material.BREWING_STAND.createBlockData(), 3);
			switch (lvl) {
			case 1:
				spawnMob(loc, EntityType.ENDERMAN, 14, 0.08f, "Эндер", 1.00f, 
					null, null, null, null, null, null, 2.5f, 0.15f, 0.4f);
				break;
			case 2:
				spawnMob(loc, EntityType.WITCH, 12, 0.12f, "Вiдьма", 0.92f, 
					null, null, null, null, null, null, 2.0f, 0.1f, 1.2f);
				break;
			case 3:
				spawnMob(loc, EntityType.EVOKER, 8, 0.12f, "Чародiй", 0.64f, 
					null, null, null, null, null, null, 4.5f, 0.04f, 0.5f);
				break;
			default:
				return;
			}
			break;
		case KILLER:
			animateAct(loc, 40, 0.4d, Sound.ENTITY_VILLAGER_WORK_WEAPONSMITH, 0.6f, Material.SMITHING_TABLE.createBlockData(), 3);
			switch (lvl) {
			case 1:
				spawnMob(loc, EntityType.SPIDER, 8, 0.10f, "Павук", 1.04f, 
					null, null, null, null, null, null, 2.0f, 0.6f, 0.4f);
				break;
			case 2:
				spawnMob(loc, EntityType.VINDICATOR, 14, 0.12f, "Поберник", 1.12f, 
					Material.GOLDEN_AXE, null, null, null, null, null, 4.0f, 0.5f, 0.4f);
				break;
			case 3:
				spawnMob(loc, EntityType.WITHER_SKELETON, 16, 0.14f, "Иссушенный", 1.20f, Material.NETHERITE_SWORD, 
					Material.BOW, null, Material.NETHERITE_CHESTPLATE, null, Material.NETHERITE_BOOTS, 4.5f, 0.5f, 0.6f);
				break;
			default:
				return;
			}
			break;
		case SIEGER:
			animateAct(loc, 60, 0.4d, Sound.ENTITY_VILLAGER_WORK_MASON, 0.6f, Material.PISTON.createBlockData(), 3);
			switch (lvl) {
			case 1:
				spawnMob(loc, EntityType.CREEPER, 16, 0.12f, "Крипер", 1.28f, 
					null, null, null, null, null, null, 10f, 0.8f, 0.4f);
				break;
			case 2: 
				spawnMob(loc, EntityType.ZOGLIN, 20, 0.16f, "Зоглiн", 1.22f, 
					null, null, null, null, null, null, 2.0f, 0.5f, 0.6f);
				break;
			case 3:
				spawnMob(loc, EntityType.RAVAGER, 24, 0.20f, "Разоритель", 1.24f, 
					null, null, null, null, null, null, 2.5f, 0.4f, 0.8f);
				break;
			default:
				return;
			}
			break;
		case UPGRADE:
			animateAct(loc, 80, 1.0d, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, Material.ENCHANTING_TABLE.createBlockData(), 3);
			switch (lvl) {
			case 3:
				tm.mobDmg += 0.1f / tm.mobDmg;
				tm.mobKb += 0.002f / tm.mobKb;
			case 2:
				tm.mobSpd += 0.002f / tm.mobSpd;
				tm.mobAs += 0.01f / tm.mobAs;
			case 1:
				tm.mobDfs += 0.01f / tm.mobDfs;
				tm.mobHp += 0.04f / tm.mobHp;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	private Mob spawnMob(final Location loc, final EntityType etp, final int hp, final float ar, final String name, 
		final float spd, final Material hand, final Material ofhd, final Material helm, final Material chest, 
		final Material legs, final Material boots, final float dmg, final float cd, final float kb) {
		final Mob mb = (Mob) loc.getWorld().spawnEntity(loc.add(Main.rngFrom(BuildType.maxXZ, 1), 1, Main.rngFrom(BuildType.maxXZ, 1)), etp, false);
		mb.customName(Component.text(tm.clr + name)); mb.setCustomNameVisible(true);
		mb.setRemoveWhenFarAway(false); mb.setPersistent(true);
		final EntityEquipment eq = mb.getEquipment();
		if (hand != null) eq.setItemInMainHand(new ItemBuilder(hand).setUnbreakable(true).build(), false);
		if (ofhd != null) eq.setItemInOffHand(new ItemBuilder(ofhd).setUnbreakable(true).build(), false);
		if (helm != null) eq.setHelmet(new ItemBuilder(helm).setUnbreakable(true).build(), false);
		if (chest != null) eq.setChestplate(new ItemBuilder(chest).setUnbreakable(true).build(), false);
		if (legs != null) eq.setLeggings(new ItemBuilder(legs).setUnbreakable(true).build(), false);
		if (boots != null) eq.setBoots(new ItemBuilder(boots).setUnbreakable(true).build(), false);
		
		mgs.removeAllGoals(mb);
		switch (etp) {
		case DROWNED, ZOMBIE_VILLAGER, HUSK:
			mgs.addGoal(mb, 0, new KnightGoal(mb, tm, hp, ar, spd, dmg, cd, kb));
			break;
		case SKELETON, STRAY, PILLAGER:
			mgs.addGoal(mb, 0, new RangerGoal(mb, tm, hp, ar, spd, dmg, cd, kb));
			break;
		case ENDERMAN, WITCH, EVOKER:
			mgs.addGoal(mb, 0, new MaegusGoal(mb, tm, hp, ar, spd, dmg, cd, kb));
			break;
		case SPIDER, VINDICATOR, WITHER_SKELETON:
			mgs.addGoal(mb, 0, new KillerGoal(mb, tm, hp, ar, spd, dmg, cd, kb));
			break;
		case CREEPER, ZOGLIN, RAVAGER:
			mgs.addGoal(mb, 0, new SiegerGoal(mb, tm, hp, ar, spd, dmg, cd, kb));
			break;
		default:
			break;
		}
		return mb;
	}

	private void animateAct(final Location loc, final int pn, final double dY, final Sound snd, final float pt, final BlockData bd, final int tms) {
		new BukkitRunnable() {
			int i = 0;
			@Override
			public void run() {
				loc.setY(loc.getY() + dY);
				cLoc.w.spawnParticle(Particle.BLOCK_CRACK, loc, pn, pn >> 5, dY, pn >> 5, 0.2d, bd, false);
				cLoc.w.playSound(loc, snd, 1f, pt);
				
				if ((i++) == tms) cancel();
			}
		}.runTaskTimer(Main.plug, 0, 2);
	}

	public ItemStack getInfoItem() {
		final ItemBuilder ib = new ItemBuilder(Material.CAMPFIRE)
			.name("§4[§6" + type.nm + "§4] §6Уровня: §4" + lvl).setAmount(lvl);
		final LinkedList<String> lr = new LinkedList<>();
		lr.add("§eЗдоровье: §c" + health + "§e/§c" + (type.hp * lvl));
		lr.add(" ");
		switch (type) {
		case DUST:
			lr.add("§eДобывает: §4" + (20 * lvl) + " 🔥 §eза §c" + type.cld + " сек");
			break;
		case NEXUS:
			lr.add("§6Точка §4спавна §6комманды!");
		case GOLD:
			lr.add("§eДобывает: §6" + (20 * lvl) + " ⛃ §eза §c" + type.cld + " сек");
			break;
		case UPGRADE:
			lr.add("§eКаждые §с" + type.cld + " сек§e, повышает:");
			switch (lvl) {
			case 3:
				lr.add("§e- §сУрон §eи §cОтдачу §eмобов");
			case 2:
				lr.add("§e- §сСкорость §eи §cЛовкость §eмобов");
			case 1:
				lr.add("§e- §сЗдоровье §eи §cЗащиту §eмобов");
			default:
				break;
			}
			break;
		default:
			lr.add("§eПризывает: " + tm.clr + type.getProd(lvl) + " §eкаждые §c" + type.cld + " сек");
			break;
		}
		return ib.lore(lr).build();
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof Build && ((Build) o).cLoc.equals(cLoc);
	}
	
	@Override
	public int hashCode() {
		return cLoc.hashCode();
	}
}
