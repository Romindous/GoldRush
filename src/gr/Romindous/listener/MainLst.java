package gr.Romindous.listener;

import java.util.Iterator;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import gr.Romindous.Main;
import gr.Romindous.game.Arena;
import gr.Romindous.game.goal.MobGoal;
import gr.Romindous.game.object.Rusher;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.deluxechat.events.DeluxeChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import ru.komiss77.Ostrov;

public class MainLst implements Listener {
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		Ostrov.sync(() -> Main.lobbyPl(e.getPlayer(), Rusher.getPlRusher(e.getPlayer().getName(), true)), 4);
	}
	
	@EventHandler
	public void onLeave(final PlayerQuitEvent e) {
		final Rusher rs = Rusher.getPlRusher(e.getPlayer().getName(), false);
		if (rs != null && rs.arena() != null) {
			rs.arena().leave(rs);
		}
		Main.rushs.remove(rs.name());
	}
	
	@EventHandler
	public void onChat(final AsyncChatEvent e) {
		final String msg = ((TextComponent) e.message()).content();
		if (msg.startsWith("/")) {
			return;
		}
		final Player snd = e.getPlayer();
		final Rusher pr = Rusher.getPlRusher(snd.getName(), true);
		final Arena ar = pr.arena();
		//если на арене
		if (ar == null) {
			return;
		} else {
			switch (ar.gst) {
			case ОЖИДАНИЕ, СТАРТ, ФИНИШ:
				final Iterator<Audience> pl = e.viewers().iterator();
				while (pl.hasNext()) {
					final Audience rec = pl.next();
					if (rec instanceof Player) {
						sendSpigotMsg(Main.prf().replace('[', '<').replace(']', '>') + "§6" + snd.getName() + " [§4" + ar.name + "§6] §7≫ " + msg, (Player) rec);
						if (eqlsCompStr(((Player) rec).getServer().motd(), snd.getServer().motd())) {
							pl.remove();
						}
					}
		        }
				return;
			default:
				for (final Rusher rs : ar.pls) {
					rs.ifPlayer(p -> {
						sendSpigotMsg("§7[Всем] " + pr.team().clr + 
								snd.getName() + " §7≫ " + msg.replaceFirst("!", ""), p);
					});
				}
				break;
			}
		}
        e.viewers().clear();
    }
	
	@EventHandler
	public void onFood(final FoodLevelChangeEvent e) {
		e.setFoodLevel(19);
	}
	
	@EventHandler
	public void onEntLoad(final EntitiesLoadEvent e) {
		for (final Entity ent : e.getEntities()) {
			if (ent instanceof Mob && MobGoal.getMobTeam((Mob) ent) == null) {
				ent.remove();
			}
		}
	}

	public static boolean eqlsCompStr(final Component c1, final Component c2) {
		return c1 instanceof TextComponent && c2 instanceof TextComponent && ((TextComponent) c1).content().equals(((TextComponent) c2).content());
	}
	
	@EventHandler
    public void Dchat(final DeluxeChatEvent e) {
        final Arena ar = Rusher.getPlRusher(e.getPlayer().getName(), true).arena();
        if (ar != null) {
			switch (ar.gst) {
			case ОЖИДАНИЕ, СТАРТ, ФИНИШ:
	            e.getDeluxeFormat().setPrefix(Main.prf() + "§7<§5" + ar.name + "§7> ");
			default:
	            e.setCancelled(true);
	            return;
			}
        }
    }
	
	@SuppressWarnings("deprecation")
	public static void sendSpigotMsg(final String msg, final Player p) {
		p.spigot().sendMessage(new net.md_5.bungee.api.chat.TextComponent(msg));
	}	
}
