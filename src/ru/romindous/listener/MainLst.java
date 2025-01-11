package ru.romindous.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.Main;
import ru.romindous.game.Arena;
import ru.romindous.game.goal.MobGoal;
import ru.romindous.game.map.Setup;
import ru.romindous.game.object.Nexus;
import ru.romindous.game.object.PlRusher;
import ru.romindous.game.object.Rusher;

public class MainLst implements Listener {
	
	@EventHandler
	public void onLoad(final LocalDataLoadEvent e) {
		e.getPlayer().sendPlayerListHeader(TCUtil.form(TCUtil.N + "["
			+ TCUtil.P + "Золотая " + TCUtil.A + "Лихорадка" + TCUtil.N + "]"));
		Main.lobbyPl(e.getPlayer(), Rusher.getPlRusher(e.getPlayer()));

		final String wantArena = e.getOplayer().getDataString(Data.WANT_ARENA_JOIN);
		if (!wantArena.isEmpty()) {
			final Arena ta = Main.active.get(wantArena);
			final Arena ar;
			if (ta == null) {
				final Setup stp = Main.nonactive.get(wantArena);
				if (stp == null) return;
				ar = Main.createArena(stp);
			} else ar = ta;
			ar.join(PM.getOplayer(e.getPlayer(), PlRusher.class));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onWorld(final PlayerChangedWorldEvent e) {
		final Player p = e.getPlayer();
		PM.getOplayer(p).tag(true);
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			if (p.getEntityId() == pl.getEntityId()) continue;
			pl.hidePlayer(Main.plug, p);
			p.hidePlayer(Main.plug, pl);
		}

		for (final Player pl : p.getWorld().getPlayers()) {
			if (p.getEntityId() == pl.getEntityId()) continue;
			pl.showPlayer(Main.plug, p);
			p.showPlayer(Main.plug, pl);
		}
	}
	
	@EventHandler
	public void onLeave(final PlayerQuitEvent e) {
		final Rusher rs = Rusher.getPlRusher(e.getPlayer());
		if (rs != null && rs.arena() != null) {
			rs.arena().leave(rs);
		}
	}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final ChatPrepareEvent e) {
        final Player p = e.getPlayer();
        final PlRusher rs = Rusher.getPlRusher(p);
		e.showLocal(false);
    	if (rs.arena() == null) {
            final Component c = TCUtil.form(TCUtil.N + "(" + TCUtil.P + StringUtil.toSigFigs(
        		(double) ApiOstrov.getStat(p, Stat.GR_kill) / (double) ApiOstrov.getStat(p, Stat.GR_death), (byte) 2) + TCUtil.N + ") ");
            e.setSenderGameInfo(c);
            e.setViewerGameInfo(c);
    	} else {
			switch (rs.arena().gst) {
			case ОЖИДАНИЕ, ФИНИШ:
	            final Component c = TCUtil.form(TCUtil.N + "[" + TCUtil.P + rs.arena().name + TCUtil.N + "] ");
                e.setSenderGameInfo(c);
                e.setViewerGameInfo(c);
				break;
			default:
	    		e.sendProxy(false);
				break;
			}
		}
    }
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onAChat(final AsyncChatEvent e) {
		final Player snd = e.getPlayer();
        final PlRusher rs = Rusher.getPlRusher(snd);
		final String msg = TCUtil.deform(e.message());
		final Arena ar = rs.arena();
		//если на арене
		if (ar == null) {
			for (final Audience au : e.viewers()) {
				au.sendMessage(TCUtil.form(Main.PRFX.replace('[', '<').replace(']', '>') + snd.getName() + 
					TCUtil.N + " [" + TCUtil.A + "ЛОББИ" + TCUtil.N + "] §7§o≫ " + TCUtil.N + msg));
			}
		} else {
			switch (ar.gst) {
			case ОЖИДАНИЕ:
			case ФИНИШ:
				for (final Audience au : e.viewers()) {
					au.sendMessage(TCUtil.form(Main.PRFX.replace('[', '<').replace(']', '>') + snd.getName() + 
						TCUtil.N + " [" + TCUtil.P + ar.name + TCUtil.N + "] §7§o≫ " + TCUtil.N + msg));
				}
				break;
			case СТАРТ:
			case ИГРА:
				for (final Rusher ors : ar.pls) {
					ors.ifPlayer(p -> {
						p.sendMessage(TCUtil.form(TCUtil.N + "[Всем] " + (ors.team() == null ? TCUtil.P : ors.team().color()) +
								snd.getName() + " §7§o≫ " + TCUtil.N + msg.replaceFirst("!", "")));
						p.playSound(p.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1.4f);
					});
				}
				break;
			default:
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
			if (ent instanceof LivingEntity) {
				if (ent instanceof Mob &&
				MobGoal.getMobTeam((Mob) ent) == null)
					ent.remove();
				continue;
			}
			ent.remove();
		}
	}

	@EventHandler
	public void onEntAgro(final EntityTargetLivingEntityEvent e) {
		if (e.getEntity() instanceof final Mob mb && e.getTarget() != null) {
			final Nexus nx = MobGoal.getMobTeam(mb);
			if (nx != null) e.setCancelled(!nx.isEnemy(e.getTarget()));
		}
	}

	@EventHandler
	public void onTp(final EntityTeleportEvent e) {
		e.setCancelled(e.getEntityType() == EntityType.ENDERMAN);
	}
}
