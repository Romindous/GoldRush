package gr.Romindous.listener;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import gr.Romindous.game.goal.MobGoal;
import gr.Romindous.game.object.Nexus;
import gr.Romindous.game.object.Rusher;

public class DmgLst implements Listener {
	
	@EventHandler
	public void onDmg(final EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			final double od = e.getDamage();
			e.setCancelled(true);
			e.setDamage(0d);
			
			if (e instanceof EntityDamageByEntityEvent) {
				final EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
				
				final LivingEntity tgt = (LivingEntity) e.getEntity();
				final Rusher tgtRs = Rusher.getRusher(tgt, false);
				final MobGoal tgtGl; final Nexus tgtNx;
				if (tgtRs != null) {
					tgtGl = null;
					tgtNx = tgtRs.team();
				} else if (tgt instanceof Mob) {
					tgtGl = MobGoal.getMobGoal((Mob) tgt);
					if (tgtGl == null) return;
					tgtNx = tgtGl.team();
				} else return;
				if (tgtNx == null) return;
				
				final LivingEntity dmgr;
				if (ee.getDamager() instanceof Projectile) {
					if (((Projectile) ee.getDamager()).getShooter() instanceof LivingEntity) {
						dmgr = (LivingEntity) ((Projectile) ee.getDamager()).getShooter();
					} else return;
				} else if (ee.getDamager() instanceof LivingEntity) {
					dmgr = (LivingEntity) ee.getDamager();
				} else return;

				final Rusher dmgrRs = Rusher.getRusher(dmgr, false);
				final MobGoal dmgrGl; final Nexus dmgrNx;
				if (dmgrRs != null) {
					dmgrGl = null;
					dmgrNx = dmgrRs.team();
				} else if (dmgr instanceof Mob) {
					dmgrGl = MobGoal.getMobGoal((Mob) dmgr);
					if (dmgrGl == null) return;
					dmgrNx = dmgrGl.team();
				} else return;
				if (dmgrNx == null) return;
				if (dmgrNx == null || 
					dmgrNx.clr == tgtNx.clr) return;
				
				e.setCancelled(false);
				if (tgtRs == null) {
					damage(tgt, dmgr, (dmgrRs == null ? dmgrGl.atkDm() : od) 
						* Math.max(0f, 1f - tgtGl.armor() * tgtNx.mobDfs));
				} else {
					e.setDamage(dmgrRs == null ? dmgrGl.atkDm() * 0.2f : od);
				}
			} else {
				switch (e.getCause()) {
				case CRAMMING, FALL, LIGHTNING:
					break;
				case VOID:
					e.getEntity().remove();
					break;
				default:
					e.setDamage(od);
					e.setCancelled(false);
					break;
				}
			}
		}
	}

	private void damage(final LivingEntity tgt, final LivingEntity dmgr, final double dmg) {
		if (tgt.getHealth() - dmg > 0.15d) {//alive
			tgt.playEffect(EntityEffect.HURT);
			tgt.setHealth(tgt.getHealth() - dmg);
			if (tgt instanceof Mob) ((Mob) tgt).setTarget(dmgr);
		} else {//dead
			tgt.setHealth(0d);
		}
	}
	
	@EventHandler
	public void onPot(final PotionSplashEvent e) {
		if (e.getEntity().getShooter() instanceof Mob) {
			final Mob mb = (Mob) e.getEntity().getShooter();
			final MobGoal mGl = MobGoal.getMobGoal(mb);
			if (mGl == null) return;
			final ChatColor cc = mGl.team().clr;
			final float dmg = mGl.atkDm();
			for (final LivingEntity le : e.getAffectedEntities()) {
				final Rusher ors = Rusher.getRusher(le, false);
				if (ors != null) {
					if (ors.team().clr == cc) {
						le.setHealth(Math.min(le.getAttribute(Attribute.GENERIC_MAX_HEALTH)
							.getBaseValue(), le.getHealth() + (dmg * mGl.team().mobDmg)));
					} else {
						mb.attack(le);
					}
				} else if (le instanceof Mob) {
					final Nexus onx = MobGoal.getMobTeam((Mob) le);
					if (onx == null) continue;
					if (onx.clr == cc) {
						le.setHealth(Math.min(le.getAttribute(Attribute.GENERIC_MAX_HEALTH)
							.getBaseValue(), le.getHealth() + (dmg * mGl.team().mobDmg)));
					} else {
						mb.attack(le);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDeath(final EntityDeathEvent e) {
		final Rusher rs = Rusher.getRusher(e.getEntity(), false);
		if (rs != null) {
			e.setCancelled(true);
			rs.arena().killRs(rs);
		} else if (e.getEntity() instanceof Mob) {
			final Mob mb = (Mob) e.getEntity();
			e.getDrops().clear();
			e.setDroppedExp((int) mb.getAttribute(
				Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		} else return;
		
		final LivingEntity klr = getKiller(e.getEntity());
		if (klr == null) return;
		final Rusher krs = Rusher.getRusher(klr, false);
		if (krs != null) {
			if (krs.arena() != null) krs.arena().addKill(krs, rs != null);
		} else if (klr instanceof Mob) {
			final Nexus nx = MobGoal.getMobTeam((Mob) klr);
			if (nx != null) nx.rs.arena().addKill(nx.rs, rs != null);
		}
	}

	private LivingEntity getKiller(final LivingEntity ent) {
		if (ent.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			final EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) ent.getLastDamageCause();
			if (ev.getDamager() instanceof Projectile && ((Projectile) ev.getDamager()).getShooter() instanceof LivingEntity) {
				return (LivingEntity) ((Projectile) ev.getDamager()).getShooter();
			} else if (ev.getDamager() instanceof LivingEntity) {
				return (LivingEntity) ev.getDamager();
			}
		}
		return null;
	}
	
	@EventHandler
	public void onHit(final ProjectileHitEvent e) {
		if (e.getHitBlock() != null) {
			e.getEntity().remove();
		}
	}
}
