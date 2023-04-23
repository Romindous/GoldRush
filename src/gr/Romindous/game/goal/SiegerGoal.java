package gr.Romindous.game.goal;

import java.util.EnumSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import gr.Romindous.Main;
import gr.Romindous.game.Arena;
import gr.Romindous.game.map.WXYZ;
import gr.Romindous.game.object.Nexus;
import gr.Romindous.game.object.Rusher;
import gr.Romindous.type.DirType;
import gr.Romindous.util.LocUtil;
import ru.komiss77.enums.GameState;
import ru.komiss77.version.VM;

public class SiegerGoal implements MobGoal {
	
	private static final int GFood = 8;
	private static final int DFood = 2;
	private static int stk = 0;
	
    private final Mob mob;
    private final Pathfinder pth;
    
	private final Nexus nx;
	private final int maxHp;
	private final float armor;
	private final float speed;
	private final float atkDm;
	private final int atkCd;
	private final float atkKb;
	
	public Nexus team() {return nx;}
	public int maxHp() {return maxHp;}
	public float armor() {return armor;}
	public float speed() {return speed;}
	public float atkDm() {return atkDm;}
	public int atkCd() {return atkCd;}
	public float atkKb() {return atkKb;}

    private DirType dir;
    private WXYZ dLoc;
	private int dmgKd;
	private int tick;
    
    public SiegerGoal(final Mob mob, final Nexus nx, final int maxHp, final float armor, 
    	final float speed, final float atkDm, final float atkCd, final float atkKb) {
        this.mob = mob; this.nx = nx; this.dLoc = null; this.pth = mob.getPathfinder();
        this.maxHp = (int) (maxHp * nx.mobHp); this.armor = armor * nx.mobDfs;
        this.atkCd = (int) (1f / (atkCd * nx.mobAs)); this.atkKb = atkKb * nx.mobKb;
        this.speed = speed * nx.mobSpd; this.atkDm = atkDm * nx.mobDmg;
        this.tick = (stk == 12 ? stk = 0 : stk++);
        this.dir = DirType.FOLLOW;
        
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.maxHp);
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
    	final Arena ar = nx.rs.arena();
    	if (ar == null || ar.gst != GameState.ИГРА || mob.isDead() || !mob.isValid() || mob.getHealth() < 0.1d) {
    		mob.remove();
    		return;
    	}
    	if (((tick++) & 3) == 0) {
    		final boolean sometimes = (tick & 255) == 0;
    		final Location eyel = mob.getEyeLocation();
    		final DirType dt = nx.rs.dir();
    		final LivingEntity tgt = mob.getTarget();
    		final boolean dfDir = dir != dt;
    		dmgKd = Math.max(dmgKd - 1, 0);
    		if (dfDir) {
    			dir = dt;
    			dLoc = null;
				if (pth.hasPath()) pth.stopPathfinding();
    		}
    		switch (dir) {
			case ATTACK:
				//if (dLoc != null) dLoc = null;
				break;
			case DEFEND:
				if (dLoc == null || sometimes) {
					dLoc = nx.getCloseBld(new WXYZ(eyel, false));
				}
				break;
			case FOLLOW:
				final WXYZ rsLc = new WXYZ(nx.rs.getEntity().getLocation(), false);
				dLoc = rsLc.getDistance(eyel) < FAR_RANGE ? rsLc.add(Main.rngFrom(2, 0), 0, Main.rngFrom(2, 0)) : null;
				break;
			case HOLDPS:
				if (dLoc == null) {
					dLoc = new WXYZ(eyel, false);
				}
				break;
			}
    		
    		if (((tick - 1) & 127) == 0) {
    			if (nx.gold < GFood || nx.dust < DFood) {
    				mob.setHealth(0d);
    			} else nx.chgRecs(-GFood, -DFood);
    		}
    		
    		if (VM.getNmsServer().getFastMat(eyel.getWorld(), eyel.getBlockX(), 
    			eyel.getBlockY() - 1, eyel.getBlockZ()) == Material.WATER) {
    			mob.setVelocity(mob.getVelocity().setY(0.1d));
    		}
    		
    		final LivingEntity nwTgt = tgt == null || !tgt.isValid() ? 
				findFreeTgt(new WXYZ(eyel, false)) : tgt;
    		mob.setTarget(nwTgt);
    		
    		if (dLoc != null && (dfDir || nwTgt == null || dLoc.getDistance(eyel) > AGRO_RANGE)) {
    			pth.moveTo(dLoc.getCenterLoc(), speed * nx.mobSpd);
				return;
    		}
    		
			if (nwTgt != null) {
				final Location tlc = nwTgt.getEyeLocation();
				final double dst = tlc.distanceSquared(eyel);
				if (dst > AGRO_RANGE) {
					mob.setTarget(null);
					return;
				}
				
				if (dst < mob.getWidth() * 0.4f + MELEE_DST) {
					if (pth.hasPath()) pth.stopPathfinding();
					if (dmgKd == 0) {
						dmgKd = (int) (atkCd * nx.mobAs);
						if (mob.getType() == EntityType.CREEPER) {
							((Creeper) mob).setIgnited(true);
						} else {
							mob.swingMainHand();
							LocUtil.forEntsIn2D(new WXYZ(tlc, false), 3, le -> {
								mob.attack(le);
								le.setVelocity(eyel.getDirection().setY(0.1d).multiply(atkKb * nx.mobKb));
							});
						}
					}
					return;
				}
				pth.moveTo(tlc, speed * nx.mobSpd);
			}
    	}
    }
    
	private LivingEntity findFreeTgt(final WXYZ mbl) {
		final ChatColor cc = nx.clr;
		for (final LivingEntity le : mbl.w.getLivingEntities()) {
			if (le.getPortalCooldown() == 0 && mob.getEntityId() != le.getEntityId() 
				&& mbl.getDistance(le.getEyeLocation()) < AGRO_RANGE) {
				final Rusher rs = Rusher.getRusher(le, false);
				final Nexus nx;
				if (rs != null) {
					nx = rs.team();
				} else if (le instanceof Mob) {
					nx = MobGoal.getMobTeam((Mob) le);
				} else continue;
				
				if (nx != null && nx.clr != cc) {
					le.setPortalCooldown(AGRO_TICK);
					return le;
				}
			}
		}
		return null;
	}

	@Override
	public GoalKey<Mob> getKey() {
		return MobGoal.key;
	}

	@Override
	public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.TARGET, GoalType.LOOK);
	}
}
