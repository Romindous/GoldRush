package ru.romindous;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.romindous.game.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import ru.romindous.game.map.Setup;
import ru.romindous.game.object.Rusher;
import ru.romindous.invent.SetupInv;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.inventory.SmartInventory;

public class GRCmd implements CommandExecutor, TabCompleter {
	
	@Override
	public List<String> onTabComplete(final CommandSender send, final Command cmd, final String al, final String[] args) {
		final List<String> sgg = new ArrayList<>();
		if (send instanceof Player) {
			switch (args.length) {
			case 1:
				sgg.add("join");
				sgg.add("leave");
				sgg.add("help");
				if (ApiOstrov.isLocalBuilder(send, false)) {
					sgg.add("edit");
					sgg.add("setlobby");
				}
				break;
			case 2:
				switch (args[0]) {
				case "edit":
					if (!ApiOstrov.isLocalBuilder(send, false)) break;
				case "join", "leave":
					for (final Setup st : Main.nonactive.values()) {
						sgg.add(st.nm);
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
		return sgg;
	}
	
	@Override
	public boolean onCommand(final CommandSender send, final Command cmd, final String label, final String[] args) {
		if (label.equalsIgnoreCase("gr") && send instanceof final Player p) {
            if (args.length < 1) {
				p.performCommand("gr help");
			}
			final Rusher rs;
			final Arena ar;
			switch (args[0]) {
			case "edit":
				if (args.length < 2 || !ApiOstrov.isLocalBuilder(send, true)) {
					p.sendMessage(Main.PRFX + "§cНеправельный синтакс комманды, все комманды - §4/gr help");
					return false;
				}
				
				final SetupInv mm = SetupInv.edits.get(p.getUniqueId());
				if (mm == null) {
					SmartInventory.builder().size(3, 9)
                    .id("Map "+p.getName()).title("§6Редактор Карты §4" + args[1])
                    .provider(new SetupInv(args[1]))
                    .build().open(p);
				} else if (mm.stp.nm.equals(args[1])) {
					SmartInventory.builder().size(3, 9)
                    .id("Map "+p.getName()).title("§6Редактор Карты §4" + args[1])
                    .provider(mm == null ? new SetupInv(args[1]) : mm)
                    .build().open(p);
				} else {
					p.sendMessage(Main.PRFX + "§cВы уже редактируете карту §4" + mm.stp.nm);
					return false;
				}
				break;
			case "setlobby":
		        final File af = new File(Main.plug.getDataFolder() + File.separator + "arenas.yml");
		        final YamlConfiguration ars = YamlConfiguration.loadConfiguration(af);
		        Main.lobby = new WXYZ(p.getLocation(), false);
		        ars.set("lobby", new XYZ(p.getLocation()).toString());
				p.sendMessage(Main.PRFX + "§6Точка лобби сохранена на " + 
					"(§4" + Main.lobby.x + "§6, §4" + Main.lobby.y + "§6, §4" + Main.lobby.z + "§6)!");
		        try {ars.save(af);} catch (IOException e) {e.printStackTrace();}
				break;
			case "join":
				rs = Rusher.getPlRusher(p);
				if (rs.arena() != null) {
					p.sendMessage(Main.PRFX + "§cВы уже на карте, используйте §4/gr leave§c для выхода!");
					return false;
				}
				if (args.length > 1) {
					ar = Main.active.get(args[1]);
					if (ar == null) {
						final Setup stp = Main.nonactive.get(args[1]);
						if (stp == null) {
							p.sendMessage(Main.PRFX + "§cТакая карта еще не создана!");
							return false;
						}
						final Arena nar = Main.createArena(stp);
						Main.active.put(args[1], nar);
						partyJoinMap(rs, p, nar);
						//p.sendMessage(Main.PRFX + "§c1!");
					} else {
						partyJoinMap(rs, p, ar);
					}
				} else {
					ar = biggestArena();
					if (ar == null) {
						if (Main.nonactive.size() > 0) {
							final Arena nar = Main.createArena(Main.rndElmt(Main.nonactive.values().toArray(new Setup[0])));
							Main.active.put(nar.name, nar);
							partyJoinMap(rs, p, nar);
						} else {
							p.sendMessage(Main.PRFX + "§cНи одной карты еще не создано!");
							return false;
						}
					} else {
						partyJoinMap(rs, p, ar);
					}
				}
				break;
			case "leave":
				rs = Rusher.getPlRusher(p);
				ar = rs.arena();
				if (ar == null) {
					p.sendMessage(Main.PRFX + "§cВы не находитесь в игре!");
					return false;
				}
				ar.leave(rs);
				break;
			case "help":
				if (ApiOstrov.isLocalBuilder(p, false)) {
					p.sendMessage("""
						§4-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
						§6Помощь по коммандам:
						§6/gr join (название) §7- присоединится к игре
						§6/gr leave §7- выход из игры
						§6/gr help §7- этот текст
						§6/gr edit (название) §7- редактирование карты
						§6/gr setlobby §7- установка лобби
						§6/gr reload §7- перезагрузка конфигов
						§4-=-=-=-=-=-=-=-=-=-=-=-=-=-=-""");
					return true;
				}
				p.sendMessage("""
					§4-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
					§6Помощь по коммандам:
					§6/gr join (название) §7- присоединится к игре
					§6/gr leave §7- выход из игры
					§6/gr help §7- этот текст
					§4-=-=-=-=-=-=-=-=-=-=-=-=-=-=-""");
				break;
			default:
				break;
			}
		}
		return true;
	}

	public static void partyJoinMap(final Rusher sh, final Player p, final Arena ar) {
		if (ApiOstrov.hasParty(p) && ApiOstrov.isPartyLeader(p)) {
			for (final String s : ApiOstrov.getPartyPlayers(p)) {
				final Player pl = Bukkit.getPlayer(s);
				if (pl != null && pl.getEntityId() != p.getEntityId()) {
					pl.sendMessage(Main.PRFX + "Лидер вашей компании зашел на карту §d" + ar.name + "§7!");
					partyJoinMap(Rusher.getPlRusher(pl), pl, ar);
				}
			}
		}
		ar.join(sh);
		p.playSound(p.getLocation(), Sound.BLOCK_COMPOSTER_EMPTY, 1f, 0.6f);
	}

	//арена на которой больше всего игроков
	public static Arena biggestArena() {
		Arena ret = null;
		
		for (final Arena ar : Main.active.values()) {
			ret = ret == null ? ar : (ar.pls.size() > ret.pls.size() ? ar : ret);
		}
		
		return ret;
	}
}
