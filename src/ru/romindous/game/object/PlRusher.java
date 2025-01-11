package ru.romindous.game.object;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.FastMath;
import ru.romindous.game.Arena;
import ru.romindous.type.DirType;
import ru.romindous.type.RaceType;
import ru.romindous.type.Upgrade;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.function.Consumer;

public class PlRusher extends Oplayer implements Rusher {
	
	private final PlayerInventory inv;

	public PlRusher(final HumanEntity p) {
		super(p);
		this.shc = nik.hashCode();
		this.inv = p.getInventory();
		this.upgs = new EnumMap<>(Upgrade.class);
    }
	
	public String name() {return nik;}
	
	public LivingEntity getEntity() {return getPlayer();}
	public Player getPlayer() {return (Player) inv.getHolder();}
	public boolean ifPlayer(final Consumer<Player> pc) {
		pc.accept(getPlayer()); return true;
	}

	public void taq(final String pfx, final String afx, final String sfx) {
		final Player p = getPlayer();
		tabPrefix(pfx, p);
		tabSuffix(sfx, p);
		beforeName(afx, p);
		tag(pfx, sfx);
	}

	private final int shc;
	public int hashCode() {return shc;}
	
	public boolean equals(final Object o) {
		return o instanceof Rusher && ((Rusher) o).name().equals(this.nik);
	}
	
	private @Nullable Arena ar;
	public @Nullable Arena arena() {return ar;}
	public void arena(final @Nullable Arena a) {ar = a;}
	
	private Nexus tm;
	public Nexus team() {return tm;}
	public void team(final Nexus n) {tm = n;}
	
	private WXYZ nlc;
	public WXYZ lastNearLoc() {return nlc.clone();}
	public void nearLoc(final WXYZ loc) {nlc = loc;}
	
	private int kls;
	public int klls() {return kls;}
	public void kllsI() {kls++;}
	public void klls0() {kls=0;}
	
	private int dts;
	public int dths() {return dts;}
	public void dthsI() {dts++;}
	public void dths0() {dts=0;}
	
	private int mks;
	public int mkls() {return mks;}
	public void mklsI() {mks++;}
	public void mkls0() {mks=0;}
	
	private int brks;
	public int brks() {return brks;}
	public void brksI() {brks++;}
	public void brks0() {brks=0;}

	public int exp() {return getPlayer().getTotalExperience();}
	public void exp0() {getPlayer().setTotalExperience(0);}
	public void exp(final int dxp) {
		final Player p = getPlayer();
		final int nxp = p.getTotalExperience() + dxp;
		if (nxp < 0) {exp0(); return;}

		p.setTotalExperience(nxp);
		final int l = (int) (Math.sqrt((float) nxp / 1.5f));
		p.setLevel(l);
		p.setExp((nxp / 1.5f - (l * l)) / ((l + 1) * (l + 1) - (l * l)));
	}

//	public int level() {return (int) (Math.sqrt(getPlayer().getTotalExperience() / 1.5d));}
	public int level() {return getPlayer().getLevel();}
	public void level(final int dl) {
		final Player p = getPlayer();
		final int lvl = p.getLevel();
		final int tol = lvl + dl;
		if (tol < 0) {
			level(-lvl);
			return;
		}
		final int txp = tol * tol * 3 / 2;
		p.setTotalExperience(Math.max((int) ((FastMath.square(tol + 1) * 1.5f - txp) * p.getExp()) + txp, 0));
		p.setLevel(tol);
	}

	public double mdDmg(final double dmg) {
		final PotionEffect se = tm.rs.getEntity().getPotionEffect(PotionEffectType.STRENGTH);
		return se == null ? dmg : dmg * ((se.getAmplifier() + 1) * STR_MOD + 1f);
	}

	private final EnumMap<Upgrade, Boolean> upgs;
	public Boolean getUpg(final Upgrade upg) {return upgs.get(upg);}
	public boolean addUpg(final Upgrade upg) {
		final Boolean bl = upgs.put(upg, true);
		if (bl == null) {
			if (upg.lst != null) upgs.replace(upg.lst, false);
			return true;
		}
		return !bl;
	}
	public boolean remUpg(final Upgrade upg) {
		if (upgs.remove(upg)) {
			if (upg.lst != null) upgs.replace(upg.lst, true);
			return true;
		}
		return false;
	}
	public void clearUpgs() {upgs.clear();}
	
	private RaceType rc;
	public RaceType race() {return rc;}
	public void race(final RaceType r) {rc = r;}
	
	private DirType dr;
	public DirType dir() {return dr;}
	public void dir(final DirType d) {
		dr = d;
		switch (d) {
		case DEFEND:
			nlc = tm.getCloseBld(new WXYZ(getPlayer().getLocation()), false);
		default:
			break;
		}
	}
	
	public Inventory inv() {return inv;}
	public ItemStack item(final EquipmentSlot slot) {
		return inv.getItem(slot);
	}
	public ItemStack item(final int slot) {
		return inv.getItem(slot);
	}
	public void item(final ItemStack it, final EquipmentSlot slot) {
		inv.setItem(slot, it);
	}
	public void item(final ItemStack it, final int slot) {
		inv.setItem(slot, it);
	}
	public void clearInv() {inv.clear();}
	public void dropIts(final Location loc) {}
	
	public void teleport(final LivingEntity le, final Location to) {le.teleport(to);}
	public boolean isDead() {return getPlayer().getGameMode() != GameMode.SURVIVAL;}
//	public boolean isInPvP() {return pvp_time > 0;}
	
}
