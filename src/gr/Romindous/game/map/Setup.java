package gr.Romindous.game.map;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import gr.Romindous.Main;
import ru.komiss77.modules.world.XYZ;

public class Setup {
	
	public final String nm;
	public final byte min;
	public final XYZ lobby;
	public final XYZ[] bases;
	public final boolean bots;
	public final boolean fin;
	
	public Setup(final ConfigurationSection ar) {
		nm = ar.getName();
		min = (byte) ar.getInt("min");
		bots = ar.getBoolean("bots");
		lobby = XYZ.fromString(ar.getString("lobby"));
		final String[] gXs = ar.getString("base.x").split(":");
		final String[] gYs = ar.getString("base.y").split(":");
		final String[] gZs = ar.getString("base.z").split(":");
		bases = new XYZ[gXs.length];
		for (int i = gXs.length - 1; i >= 0; i--) {
			bases[i] = new XYZ(lobby.worldName, Integer.parseInt(gXs[i]), Integer.parseInt(gYs[i]), Integer.parseInt(gZs[i]));
		}
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
		ar.set("fin", fin);
		ar.set("lobby", lobby.toString());
		
		final StringBuffer gXs = new StringBuffer();
		final StringBuffer gYs = new StringBuffer();
		final StringBuffer gZs = new StringBuffer();
		for (final XYZ l : bases) {
			gXs.append(":" + l.x);
			gYs.append(":" + l.y);
			gZs.append(":" + l.z);
		}
		ar.set("base.x", gXs.substring(1));
		ar.set("base.y", gYs.substring(1));
		ar.set("base.z", gZs.substring(1));
		
		try {
			conf.save(fl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Setup(final String name) {
		nm = name; min = 2; lobby = null;
		bases = null; bots = false; fin = false;
	}
	
	public Setup(final String name, final byte min, final XYZ lobby, 
		final XYZ[] bases, final boolean bots, final boolean fin) {
		this.nm = name; this.min = min; this.lobby = lobby;
		this.bases = bases; this.bots = bots; this.fin = fin;
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
