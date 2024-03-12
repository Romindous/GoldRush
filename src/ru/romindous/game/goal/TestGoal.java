package ru.romindous.game.goal;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.modules.world.WXYZ;
import ru.romindous.Main;
import ru.romindous.game.object.Nexus;

import java.util.EnumSet;

public class TestGoal implements MobGoal {
	
    private final Mob mob;
    private final Player pl;
    private final Pathfinder pth;
	
	public Nexus team() {return null;}
	public int maxHp() {return 0;}
	public float armor() {return 0f;}
	public float speed() {return 0f;}
	public float atkDm() {return 0f;}
	public int atkCd() {return 0;}
	public float atkKb() {return 0f;}

    private final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Main.plug, "mob"));
    
    public TestGoal(final Mob mob, final Player pl) {
        this.mob = mob;
        this.pl = pl;
        this.pth = mob.getPathfinder();
    }

    public void setTgt(final @javax.annotation.Nullable LivingEntity tgt) {}

    @Override
    public void chngDst(@Nullable WXYZ dst, Location eyel) {}
 
    @Override
    public boolean shouldActivate() {
        return true;
    }
 
    @Override
    public boolean shouldStayActive() {
        return !mob.isDead();//shouldActivate();
    }
 
    @Override
    public void start() {
    }
 
    @Override
    public void stop() {
    }
    
    @Override
    public void tick() {
		final PathResult pr = pth.findPath(pl.getLocation());
		if (pr != null) pth.moveTo(pr, 0.22);
    }

	@Override
	public GoalKey<Mob> getKey() {
		return key;
	}

	@Override
	public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.TARGET, GoalType.LOOK);
	}
}
