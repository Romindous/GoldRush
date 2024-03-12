package ru.romindous.game.map;

import java.io.File;
import java.io.IOException;

import ru.romindous.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.komiss77.modules.world.XYZ;

public class Setup {
	
	public final String nm;
	public final byte min;
	public final XYZ lobby;
	public final XYZ[] bases;
	public final XYZ[] shops;
	public final boolean bots;
	public final boolean fin;
	
	public Setup(final ConfigurationSection ar) {
		nm = ar.getName();
		min = (byte) ar.getInt("min");
		bots = ar.getBoolean("bots");
		lobby = XYZ.fromString(ar.getString("lobby"));
		if (ar.contains("base")) {
			final String[] gXs = ar.getString("base.x").split(":");
			final String[] gYs = ar.getString("base.y").split(":");
			final String[] gZs = ar.getString("base.z").split(":");
			bases = new XYZ[gXs.length];
			for (int i = gXs.length - 1; i >= 0; i--) {
				bases[i] = new XYZ(lobby.worldName, Integer.parseInt(gXs[i]), Integer.parseInt(gYs[i]), Integer.parseInt(gZs[i]));
			}
		} else bases = null;
		if (ar.contains("shop")) {
			final String[] sXs = ar.getString("shop.x").split(":");
			final String[] sYs = ar.getString("shop.y").split(":");
			final String[] sZs = ar.getString("shop.z").split(":");
			shops = new XYZ[sXs.length];
			for (int i = sXs.length - 1; i >= 0; i--) {
				shops[i] = new XYZ(lobby.worldName, Integer.parseInt(sXs[i]), Integer.parseInt(sYs[i]), Integer.parseInt(sZs[i]));
			}
		} else shops = null;
		fin = ar.getBoolean("fin");
	}
	
	public void save() {
		if (!fin) return;
		final File fl = new File(Main.plug.getDataFolder() + File.separator + "arenas.yml");
		final YamlConfiguration conf = YamlConfiguration.loadConfiguration(fl);
		conf.set("arenas." + nm, null);
		final ConfigurationSection ar = conf.createSection("arenas." + nm);
		ar.set("min", min);
		ar.set("bots", bots);
		ar.set("fin", true);
		ar.set("lobby", lobby.toString());
		
		final StringBuilder gXs = new StringBuilder();
		final StringBuilder gYs = new StringBuilder();
		final StringBuilder gZs = new StringBuilder();
		for (final XYZ l : bases) {
			gXs.append(":").append(l.x);
			gYs.append(":").append(l.y);
			gZs.append(":").append(l.z);
		}
		ar.set("base.x", gXs.substring(1));
		ar.set("base.y", gYs.substring(1));
		ar.set("base.z", gZs.substring(1));

		final StringBuilder sXs = new StringBuilder();
		final StringBuilder sYs = new StringBuilder();
		final StringBuilder sZs = new StringBuilder();
		for (final XYZ l : shops) {
			sXs.append(":").append(l.x);
			sYs.append(":").append(l.y);
			sZs.append(":").append(l.z);
		}
		ar.set("shop.x", sXs.substring(1));
		ar.set("shop.y", sYs.substring(1));
		ar.set("shop.z", sZs.substring(1));

		try {
			conf.save(fl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Setup(final String name) {
		nm = name; min = 2; lobby = null; bases = null;
		shops = null; bots = false; fin = false;
	}
	
	public Setup(final String name, final byte min,
		 final XYZ lobby, final XYZ[] bases, final XYZ[] shops,
			 final boolean bots, final boolean fin) {
		this.nm = name; this.min = min; this.lobby = lobby;
		this.bases = bases; this.shops = shops;
		this.bots = bots; this.fin = fin;
	}
	
	public boolean isReady() {
		if (nm == null || bases == null || lobby == null || min < 1) return false;
		return bases.length > 1;
	}

	public void delete(final boolean hard) {
		Main.nonactive.remove(nm);
		if (hard) {
			final File fl = new File(Main.plug.getDataFolder() + File.separator + "arenas.yml");
			final YamlConfiguration cnf = YamlConfiguration.loadConfiguration(fl);
			cnf.set("arenas." + nm, null);
			try {
				cnf.save(fl);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
