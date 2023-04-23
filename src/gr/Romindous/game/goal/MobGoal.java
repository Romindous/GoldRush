package gr.Romindous.game.goal;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.MobGoals;

import gr.Romindous.Main;
import gr.Romindous.game.object.Nexus;

public interface MobGoal extends Goal<Mob> {
	
	public static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "grmob"));
	public static final MobGoals mgs = Bukkit.getMobGoals();
	public static final double MELEE_DST = 3d;
	public static final int CLOSE_RANGE = 100;
	public static final int FAR_RANGE = 400;
	public static final int AGRO_RANGE = 240;
	public static final int AGRO_TICK = 32;
	
	public static MobGoal getMobGoal(final Mob mb) {
		final Goal<Mob> gl = mgs.getGoal(mb, key);
		return gl != null && gl instanceof MobGoal ? (MobGoal) gl : null;
	}
	
	public static Nexus getMobTeam(final Mob mb) {
		final MobGoal gl = getMobGoal(mb);
		return gl == null ? null : gl.team();
	}
	
	public Nexus team();
	
	public int maxHp();
	public float armor();
	public float speed();
	public float atkDm();
	public int atkCd();
	public float atkKb();
	
	
	
}
