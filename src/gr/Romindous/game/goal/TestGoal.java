package gr.Romindous.game.goal;

import java.util.EnumSet;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import gr.Romindous.Main;
import gr.Romindous.game.object.Nexus;

public class TestGoal implements MobGoal {
	
    private final Mob mob;
    private final Player pl;
	
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
    }
 
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
    	mob.getPathfinder().moveTo(pl, 0.22);
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
