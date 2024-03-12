package ru.romindous.game.object.build;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.world.Schematic;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.romindous.game.Arena;
import ru.romindous.game.object.Build;
import ru.romindous.game.object.Nexus;
import ru.romindous.type.BuildType;
import ru.romindous.type.RaceType;

import java.util.HashSet;

public class Shop implements Build {

	protected final HashSet<WXYZ> blks;

	private final Arena ar;
	public final WXYZ cLoc;

	public Shop(final Arena ar, final WXYZ cLoc) {
		this.ar = ar;
		this.cLoc = cLoc;

		blks = new HashSet<>();
		final Schematic sch = BuildType.SHOP;
		final int dX = sch.getSizeX(), dY = sch.getSizeY(), dZ = sch.getSizeZ();
//		sch.paste(Bukkit.getConsoleSender(), cLoc, Schematic.Rotate.r0,false);
		final WXYZ pos = cLoc.clone().add(-(dX >> 1), 0, -(dZ >> 1));
		final World w = pos.w;
		final String wnm = w.getName();
		for (int x = 0; x < dX; x++) {
			for (int z = 0; z < dZ; z++) {
				for (int y = 0; y < dY; y++) {
					final XYZ lc = new XYZ(wnm, y, x, z);
					final Material mt = sch.getMaterial(lc);
					if (mt == null || mt.isAir()) continue;
					final BlockData bd = sch.getBlockData(lc);
					if (bd == null) w.setType(pos.x + x, pos.y + y, pos.z + z, mt);
					else w.setBlockData(pos.x + x, pos.y + y, pos.z + z, bd);
					blks.add(new WXYZ(w, pos.x + x, pos.y + y, pos.z + z));
				}
			}
		}

		final WXYZ top = new WXYZ(w, 1 + ((pos.x + dX) >> BuildType.dsLoc),
			1 + ((pos.y + dY) >> BuildType.dsLoc), 1 + ((pos.z + dZ) >> BuildType.dsLoc));
		for (int x = pos.x >> BuildType.dsLoc; x < top.x; x++) {
			for (int z = pos.z >> BuildType.dsLoc; z < top.z; z++) {
				for (int y = pos.y >> BuildType.dsLoc; y < top.y; y++) {
					ar.blocs.put(new WXYZ(w, x, y, z).getSLoc(), this);
				}
			}
		}
	}

	@Override
	public Nexus tm() {return null;}

	@Override
	public WXYZ cLoc() {return cLoc;}

	@Override
	public byte lvl() {return BuildType.maxLvl;}

	@Override
	public BuildType type() {return null;}

	@Override
	public RaceType race() {return null;}

	@Override
	public HashSet<WXYZ> blks() {return blks;}

	@Override
	public boolean done() {return true;}

	@Override
	public String prcHlth() {return "§a100%";}

	public boolean damage(final int dmg) {return false;}

	public void remove(final boolean pay) {
		for (final WXYZ p : blks) {
			final Location loc = p.getCenterLoc();
			final Block b = loc.getBlock();
			final BlockData bd = b.getBlockData();
			cLoc.w.spawnParticle(Particle.BLOCK_CRACK, loc, 20, 0.4d, 0.4d, 0.4d, 0.2d, bd, false);
			cLoc.w.playSound(loc, bd.getSoundGroup().getBreakSound(), 1f, 0.8f);
			b.setType(Material.AIR, false);
		}

		if (ar != null) {
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
	}

	public boolean act() {return false;}

	public int hp() {return 10;}

	public float ar() {return 0f;}

	public float spd() {return 1.00f;}

	public float dmg() {return 1.0f;}

	public float cd() {return 1.0f;}

	public float kb() {return 0.0f;}

	public int getGCost() {return 0;}

	public int getDCost() {return 0;}

	public Mob spawnMob(final Location loc, final EntityType etp, final String name, final Material hand, final Material ofhd,
			final Material helm, final Material chest, final Material legs, final Material boots) {return null;}

	public ItemStack getInfoItem() {
		return new ItemBuilder(Material.CAMPFIRE).build();
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
