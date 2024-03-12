package ru.romindous.game.object;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.romindous.game.Arena;
import ru.romindous.type.DirType;
import ru.romindous.type.RaceType;
import ru.romindous.type.Upgrade;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface Rusher {

	double STR_MOD = 0.2d;

	String name();
	@Nullable Arena arena();
	void arena(final @Nullable Arena ar);
	
	int klls();
	void kllsI();
	void klls0();
	
	int dths();
	void dthsI();
	void dths0();
	
	int mkls();
	void mklsI();
	void mkls0();
	
	int brks();
	void brksI();
	void brks0();
	
	Nexus team();
	void team(final Nexus nx);
	
	WXYZ lastNearLoc();
	void nearLoc(final WXYZ loc);

	Boolean getUpg(final Upgrade upg);
	boolean addUpg(final Upgrade upg);
	boolean remUpg(final Upgrade upg);
	void clearUpgs();
	
	RaceType race();
	void race(final RaceType dr);
	
	DirType dir();
	void dir(final DirType dr);
	
	int exp();
	void exp0();
	void exp(final int dxp);

	int level();
	void level(final int dl);

	double mdDmg(final double dmg);
	
	LivingEntity getEntity();
	Player getPlayer();
	boolean ifPlayer(final Consumer<Player> pc);

	void taq(final String prefix, final String affix, final String suffix);

	ItemStack item(final EquipmentSlot slot);
	ItemStack item(final int slot);
	void item(final ItemStack it, final EquipmentSlot slot);
	void item(final ItemStack it, final int slot);
	Inventory inv();
	void clearInv();
	void dropIts(final Location loc);
	void color(final NamedTextColor color);

	void teleport(final LivingEntity le, final Location to);
	boolean isDead();
//	boolean isInPvP();
	
	static PlRusher getPlRusher(final HumanEntity pl) {
		/*final PlRusher sh = Main.rushs.get(nm);
		if (sh == null && crt) {
			final Player p = Bukkit.getPlayer(nm);
			if (p == null) return null;
			final PlRusher nvs = new PlRusher(p);
			Main.rushs.put(nm, nvs);
			return nvs;
		}*/
		return PM.getOplayer(pl, PlRusher.class);
	}
	
	static Rusher getRusher(final LivingEntity le) {
		return le instanceof HumanEntity ? getPlRusher((HumanEntity) le) : null;
	}
	
}
