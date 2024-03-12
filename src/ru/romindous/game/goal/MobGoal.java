package ru.romindous.game.goal;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import ru.romindous.Main;
import ru.romindous.game.object.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.MobGoals;

import ru.komiss77.modules.world.WXYZ;

import javax.annotation.Nullable;

public interface MobGoal extends Goal<Mob> {
	
	GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "grmob"));
	MobGoals mgs = Bukkit.getMobGoals();
	double MELEE_DST = 4d;
	//	public static final int CLOSE_RANGE = 100;
	int SHIFT = 2;
	int TICK_DEL = (1 << SHIFT) - 1;
	int FAR_RANGE = 16;
	int FAR_RANGE_SQ = FAR_RANGE * FAR_RANGE;
	int AGRO_RANGE = 10;
	int AGRO_RANGE_SQ = AGRO_RANGE * AGRO_RANGE;
	int AGRO_TICK = 32;
	
	static MobGoal getMobGoal(final Mob mb) {
		final Goal<Mob> gl = mgs.getGoal(mb, key);
		return gl instanceof MobGoal ? (MobGoal) gl : null;
	}
	
	static Nexus getMobTeam(final Mob mb) {
		final MobGoal gl = getMobGoal(mb);
		return gl == null ? null : gl.team();
	}
	
	Nexus team();
	
	int maxHp();
	float armor();
	float speed();
	float atkDm();
	int atkCd();
	float atkKb();

	void setTgt(final @Nullable LivingEntity tgt);
	void chngDst(@Nullable final WXYZ dst, final Location eyel);
	
}
