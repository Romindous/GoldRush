package gr.Romindous.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import gr.Romindous.game.object.Rusher;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam.a;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;

public class TitleUtil {

	private static final Method getWrld = mkGet(".CraftWorld");
	private static final Method getPl = mkGet(".entity.CraftPlayer");
	private static final Method getLE = mkGet(".entity.CraftLivingEntity");
	private static Method mkGet(final String pth) {
		try {
			return Class.forName(Bukkit.getServer().getClass().getPackage().getName() + pth).getDeclaredMethod("getHandle");
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void sendNmTg(final Rusher rs, final String prf, final String sfx, final EnumChatFormat clr) {
		final EntityLiving ep = getNMSLE(rs.getEntity());
		if (ep == null) return;
		final Scoreboard sb = ep.cD().aF();
		final ScoreboardTeam st = sb.g(rs.name());
		st.b(IChatBaseComponent.a(prf));
		st.c(IChatBaseComponent.a(sfx));
		st.a(clr);
		final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);
		final PacketPlayOutScoreboardTeam crt = PacketPlayOutScoreboardTeam.a(st, true);
		final PacketPlayOutScoreboardTeam add = PacketPlayOutScoreboardTeam.a(st, rs.name(), a.a);
		final PacketPlayOutScoreboardTeam mod = PacketPlayOutScoreboardTeam.a(st, false);
		sb.d(st);
		for (final World w : Bukkit.getWorlds()) {
			for (final EntityHuman e : getNMSWrld(w).w()) {
				((EntityPlayer) e).b.a(pt);
				((EntityPlayer) e).b.a(crt);
				((EntityPlayer) e).b.a(add);
				((EntityPlayer) e).b.a(mod);
			}
		}
	}
    
    public static WorldServer getNMSWrld(final World w) {
		try {
			return (WorldServer) getWrld.invoke(w);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}
    
    public static EntityLiving getNMSLE(final LivingEntity le) {
		try {
			return (EntityLiving) getLE.invoke(le);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}
    
    public static EntityPlayer getNMSPl(final Player p) {
		try {
			return (EntityPlayer) getPl.invoke(p);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
  	}
}
