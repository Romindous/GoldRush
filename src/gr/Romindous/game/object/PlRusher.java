package gr.Romindous.game.object;

import java.util.function.Consumer;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import gr.Romindous.Main;
import gr.Romindous.game.Arena;
import gr.Romindous.type.DirType;

public class PlRusher implements Rusher {
	
	private final String nm;
	private final PlayerInventory inv;

	public PlRusher(final Player p) {
		this.nm = p.getName();
		this.shc = nm.hashCode();
		this.inv = p.getInventory();
		
		Main.rushs.put(nm, this);
	}
	
	public String name() {return nm;}
	
	public LivingEntity getEntity() {return getPlayer();}
	public Player getPlayer() {return (Player) inv.getHolder();}
	public boolean ifPlayer(final Consumer<Player> pc) {
		pc.accept(getPlayer()); return true;
	}
	
	private final int shc;
	public int hashCode() {return shc;}
	
	public boolean equals(final Object o) {
		return o instanceof Rusher && ((Rusher) o).name().equals(this.nm);
	}
	
	private Arena ar;
	public Arena arena() {return ar;}
	public void arena(final Arena a) {ar = a;}
	
	private Nexus tm;
	public Nexus team() {return tm;}
	public void team(final Nexus n) {tm = n;}
	
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
	
	private DirType dr;
	public DirType dir() {return dr;}
	public void dir(final DirType d) {dr = d;}
	
	private int exp;
	public int exp() {return exp;}
	public void exp(final int xp) {exp = xp;}
	
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
}
