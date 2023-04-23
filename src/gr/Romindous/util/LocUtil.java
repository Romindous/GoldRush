package gr.Romindous.util;

import java.util.Collection;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import gr.Romindous.game.map.WXYZ;

public class LocUtil {

	public static WXYZ getNr2DLoc(final Location loc, final Collection<WXYZ> arr) {
		WXYZ lc = null;
		int dst = Integer.MAX_VALUE;
		final int X = loc.getBlockX(), Z = loc.getBlockZ();
		for (final WXYZ l : arr) {
			final int dx = l.x - X, dz = l.z - Z, dl = dx*dx + dz*dz;
			if (lc == null || dl < dst) {
				dst = dl;
				lc = l;
			}
		}
		return lc;
	}

	public static int forEntsIn2D(final WXYZ loc, final int dst, final Consumer<LivingEntity> cn) {
		int i = 0;
		final int dS = dst * dst;
		for (final LivingEntity le : loc.w.getLivingEntities()) {
			final Location elc = le.getLocation();
			final double dx = elc.getX() - loc.x, dz = elc.getZ() - loc.z;
			if (dx*dx + dz*dz < dS) {
				cn.accept(le); i++;
			}
		}
		return i;
	}
	
}
