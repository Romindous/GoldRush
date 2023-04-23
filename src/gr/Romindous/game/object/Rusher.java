package gr.Romindous.game.object;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import gr.Romindous.Main;
import gr.Romindous.game.Arena;
import gr.Romindous.type.DirType;

public interface Rusher {

	public String name();
	public Arena arena();
	public void arena(final Arena ar);
	
	public int klls();
	public void kllsI();
	public void klls0();
	
	public int dths();
	public void dthsI();
	public void dths0();
	
	public int mkls();
	public void mklsI();
	public void mkls0();
	
	public int brks();
	public void brksI();
	public void brks0();
	
	public Nexus team();
	public void team(final Nexus nx);
	
	public DirType dir();
	public void dir(final DirType dr);
	
	public int exp();
	public void exp(final int xp);
	
	public LivingEntity getEntity();
	public Player getPlayer();
	public boolean ifPlayer(final Consumer<Player> pc);

	public ItemStack item(final EquipmentSlot slot);
	public ItemStack item(final int slot);
	public void item(final ItemStack it, final EquipmentSlot slot);
	public void item(final ItemStack it, final int slot);
	public Inventory inv();
	public void clearInv();
	public void dropIts(final Location loc);

	public void teleport(final LivingEntity le, final Location to);
	public boolean isDead();
	
	public static PlRusher getPlRusher(final String nm, final boolean crt) {
		final PlRusher sh = Main.rushs.get(nm);
		if (sh == null && crt) {
			final Player p = Bukkit.getPlayer(nm);
			if (p == null) return null;
			final PlRusher nvs = new PlRusher(p);
			Main.rushs.put(nm, nvs);
			return nvs;
		}
		return sh;
	}
	
	public static Rusher getRusher(final LivingEntity le, final boolean crt) {
		if (le.getType() == EntityType.PLAYER) {
			return getPlRusher(le.getName(), crt);
		} else {
			/*final BtShooter bh = BotManager.npcs.get(le.getEntityId());
			if (bh == null) {
				return crt ? new BtShooter(null, le.getWorld()) : null;
			}
			return bh;*/
			return null;
		}
	}
	
}
