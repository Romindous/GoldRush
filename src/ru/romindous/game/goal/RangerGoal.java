package ru.romindous.game.goal;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import ru.komiss77.enums.GameState;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.version.Nms;
import ru.romindous.game.Arena;
import ru.romindous.game.object.Nexus;
import ru.romindous.type.DirType;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.EnumSet;

public class RangerGoal implements MobGoal {
	
	//private static final PotionData pdt = new PotionData(PotionType.SLOWNESS, true, true);

	private static final double PRJ_SPD = 1.6d;
	private static final int GFood = 3;
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

	private WeakReference<LivingEntity> tfr;
    private DirType dir;
    private WXYZ dLoc;
	private int dmgKd;
	private int tick;
    
    public RangerGoal(final Mob mob, final Nexus nx, final int maxHp, final float armor, 
    	final float speed, final float atkDm, final float atkCd, final float atkKb) {
		this.mob = mob; this.nx = nx; this.dLoc = null; this.pth = mob.getPathfinder();
		this.maxHp = maxHp; this.armor = armor; this.atkCd = (int) (20f / atkCd) >> SHIFT;
		this.atkKb = atkKb; this.speed = speed; this.atkDm = atkDm;
		this.tfr = new WeakReference<>(null);
		this.tick = (stk = stk == 12 ? 0 : stk + 1);
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
		tick++;
		if ((tick & TICK_DEL) == 0) {
			final Location eyel = mob.getEyeLocation();
			final LivingEntity tgt = tfr.get();
			final DirType dt = nx.rs.dir();
			dmgKd = Math.max(dmgKd - 1, 0);

			if (dir != dt) {
				dir = dt;
				chngDst(null, eyel);
			}

			switch (dir) {
				case ATTACK:
					//if (dLoc != null) dLoc = null;
					break;
				case DEFEND:
					if (dLoc == null) {
						final WXYZ dest = nx.rs.lastNearLoc();
						if (dest != null) chngDst(dest, eyel);
					}
					break;
				case FOLLOW:
					chngDst(new WXYZ(nx.rs.getEntity().getLocation(), false), eyel);
					break;
				case HOLDPS:
					if (dLoc == null) chngDst(new WXYZ(eyel, false), eyel);
					break;
			}

			if ((tick & 127) == 0) {
				if (nx.gold < GFood || nx.dust < DFood) {
					mob.setHealth(0d);
				} else nx.chgRecs(-GFood, -DFood);
			}

			final LivingEntity nwTgt;
			if (tgt == null || !tgt.isValid()) {//if target not exist
				if (dLoc != null && dLoc.distSq(eyel) > AGRO_RANGE_SQ) {//if close from dest
					final Pathfinder.PathResult pr = pth.findPath(dLoc.getCenterLoc());
					if (pr != null) pth.moveTo(pr, speed);
					return;
				}

				setTgt(nwTgt = findFreeTgt(new WXYZ(eyel, false)));

				if (nwTgt != null) {//set new tgt
					nwTgt.setPortalCooldown(AGRO_TICK);
					final Pathfinder.PathResult pr = pth.findPath(nwTgt.getLocation());
					if (pr != null) pth.moveTo(pr, speed);
				}
				Nms.setAggro(mob, false);
				return;
			}

			if (dLoc != null && dLoc.distSq(eyel) > FAR_RANGE_SQ) {//if far from dest
				final Pathfinder.PathResult pr = pth.findPath(dLoc.getCenterLoc());
				if (pr != null) pth.moveTo(pr, speed);
				return;
			}

			final Location tel = tgt.getEyeLocation();
			final Vector vc = tel.toVector().subtract(eyel.toVector());
			final float yaw = FastMath.getYaw(vc);
			mob.setRotation(yaw, 0);
			final double dst = vc.lengthSquared();
			if (dst > FAR_RANGE_SQ) {//check tgt dst
				setTgt(null);
				Nms.setAggro(mob, false);
				return;
			}

			if (dmgKd == 0) {
				if (pth.hasPath()) pth.stopPathfinding();
				dmgKd = atkCd;
				eyel.setYaw(yaw);
				mob.setRotation(eyel.getYaw(), eyel.getPitch());
				mob.swingMainHand();
				switch (mob.getType()) {
					case STRAY:
						mob.launchProjectile(Arrow.class, getArrowVc(vc, PRJ_SPD + atkKb));
						break;
					case PIGLIN:
						mob.launchProjectile(Arrow.class, getArrowVc(vc, PRJ_SPD + atkKb)).setFireTicks(1000);
						break;
                    case PILLAGER:
						mob.launchProjectile(Arrow.class, getArrowVc(vc, PRJ_SPD + atkKb)).setKnockbackStrength(1);
//						eyel.setYaw(eyel.getYaw() + 10f);
//						mob.launchProjectile(Arrow.class, getArrowVc(vc.clone()).multiply(atkKb));
//						eyel.setYaw(eyel.getYaw() - 20f);
//						mob.launchProjectile(Arrow.class, getArrowVc(vc.clone()).multiply(atkKb));
						break;
					default:
						break;
				}
				Nms.setAggro(mob, true);
				return;
			}

			if (dst < AGRO_RANGE_SQ) {//check close dst
				final Pathfinder.PathResult pr = pth.findPath(new Location(tel.getWorld(),
					eyel.getX() - vc.getX(), eyel.getY(), eyel.getZ() - vc.getZ()));
				if (pr != null) pth.moveTo(pr, speed);
				Nms.setAggro(mob, false);
				return;
			}

			Nms.setAggro(mob, true);
			final Pathfinder.PathResult pr = pth.findPath(tel);
			if (pr != null) pth.moveTo(pr, speed);
		}
	}

	public void setTgt(final @Nullable LivingEntity tgt) {
		tfr = new WeakReference<>(tgt);
		mob.setTarget(tgt);
	}

	@Override
	public void chngDst(@Nullable final WXYZ dst, final Location eyel) {
		if (dst == null) {
			dLoc = null;
			pth.stopPathfinding();
			return;
		}

		if (dLoc != null && dst.distSq(dLoc) < AGRO_RANGE_SQ) return;
		dLoc = dst;
	}

	private LivingEntity findFreeTgt(final WXYZ mbl) {
		return LocationUtil.getClsChEnt(mbl, AGRO_RANGE, LivingEntity.class, le -> {
			return le.getPortalCooldown() == 0 && mob.getEntityId() != le.getEntityId() && nx.isEnemy(le);
		});
	}

	private Vector getArrowVc(final Vector dst, final double spd) {
		final double DlnSq = dst.lengthSquared() * 0.01d / spd;
		if (dst.getY() > -DlnSq) dst.setY((dst.getY() + 1d) * DlnSq);
		return dst.normalize().multiply(spd);
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
