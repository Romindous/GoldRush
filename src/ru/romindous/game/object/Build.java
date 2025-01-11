package ru.romindous.game.object;

import com.destroystokyo.paper.entity.ai.MobGoals;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.OConfig;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.StringUtil;
import ru.romindous.Main;
import ru.romindous.game.Arena;
import ru.romindous.type.BuildType;
import ru.romindous.type.RaceType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashSet;

public interface Build {

	OConfig saves = new OConfig(new File(Main.plug.getDataFolder() + "/builds.yml"), 0);

	int REF_DEL = 1;
	float SPIRE_LB = 0.25f;
	int ACT_DST_SQ = FastMath.square(60);
	MobGoals mgs = Bukkit.getMobGoals();
	String dft = "WHITE";

	Nexus tm();
	WXYZ cLoc();
	byte lvl();
	String prcHlth();
	BuildType type();
	RaceType race();
	HashSet<WXYZ> blks();
	boolean done();

	boolean damage(final int dmg);
	void remove(final boolean pay);
	boolean act();

	int hp();
	float ar();
	float spd();
	float dmg();
	float cd();
	float kb();

	int getGCost();
	int getDCost();

	Mob spawnMob(final Location loc, final EntityType etp, final String name, final Material hand,
		final Material ofhd, final Material helm, final Material chest, final Material legs, final Material boots);

	ItemStack getInfoItem();

	boolean has(final WXYZ loc);
	
	@Override
    boolean equals(final Object o);
	
	@Override
    int hashCode();

	@Slow(priority = 1)
    static @Nullable Build getByArea(final WXYZ loc) {
		for (final Arena ar : Main.active.values()) {
			return getByArea(ar, loc);
		}
		return null;
	}

	static @Nullable Build getByArea(final Arena ar, final WXYZ loc) {
		return ar.blocs.get(new WXYZ(loc.w, loc.x >> BuildType.dsLoc,
			loc.y >> BuildType.dsLoc, loc.z >> BuildType.dsLoc).getSLoc());
	}

	static void animateAct(final Location loc, final int pn, final double dY, final Sound snd, final float pt, final BlockData bd, final int tms) {
		new BukkitRunnable() {
			int i = 0;
			@Override
			public void run() {
				loc.setY(loc.getY() + dY);
				loc.getWorld().spawnParticle(Particle.BLOCK, loc, pn, pn >> 5, dY, pn >> 5, 0.2d, bd, false);
				loc.getWorld().playSound(loc, snd, 1f, pt);

				if ((i++) == tms) cancel();
			}
		}.runTaskTimer(Main.plug, 0, 2);
	}

	static String getRelInc(final float inc) {
		return StringUtil.toSigFigs((inc - 1d) * 100d, (byte) 2) + "%";
	}
}
