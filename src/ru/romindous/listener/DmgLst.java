package ru.romindous.listener;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.jetbrains.annotations.Nullable;
import ru.romindous.Main;
import ru.romindous.game.Arena;
import ru.romindous.game.goal.MobGoal;
import ru.romindous.game.object.Build;
import ru.romindous.game.object.Nexus;
import ru.romindous.game.object.Rusher;
import ru.romindous.game.object.build.IBuild;
import ru.romindous.game.object.build.PBuild;

public class DmgLst implements Listener {
	
	@EventHandler
	public void onDmg(final EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			double od = e.getDamage();
			e.setCancelled(true);
			e.setDamage(0d);
			
			if (e instanceof final EntityDamageByEntityEvent ee) {
                final LivingEntity tgt = (LivingEntity) e.getEntity();
				final Rusher tgtRs = Rusher.getRusher(tgt);
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
				if (ee.getDamager() instanceof final Projectile prj) {
					if (prj.getShooter() instanceof LivingEntity) {
						switch (prj.getType()) {
							case SPECTRAL_ARROW:
								od = IBuild.SPIRE_DMG * (1f + Build.SPIRE_LB *
									((SpectralArrow) prj).getPierceLevel());
								break;
							case FIREBALL:
								od = PBuild.SPIRE_DMG;
								break;
                            default: break;
                        }
						dmgr = (LivingEntity) prj.getShooter();
					} else return;
				} else if (ee.getDamager() instanceof LivingEntity) {
					dmgr = (LivingEntity) ee.getDamager();
				} else return;

				final Rusher dmgrRs = Rusher.getRusher(dmgr);
				final MobGoal dmgrGl; final Nexus dmgrNx;
				if (dmgrRs != null) {
					dmgrGl = null;
					dmgrNx = dmgrRs.team();
				} else if (dmgr instanceof Mob) {
					dmgrGl = MobGoal.getMobGoal((Mob) dmgr);
					if (dmgrGl == null) return;
					dmgrNx = dmgrGl.team();
				} else return;
				if (dmgrNx == null || 
					dmgrNx.cc == tgtNx.cc) return;
				
				e.setCancelled(false);
				if (tgtRs == null) {
					damage(tgt, dmgr, (dmgrRs == null ? dmgrGl.atkDm() : od)
						* Math.max(0f, 1f - tgtGl.armor() * tgtNx.mobAr));
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
		if (tgt.getHealth() - dmg > 0d) {//alive
			tgt.playHurtAnimation(dmgr.getEyeLocation().getYaw());
			tgt.setHealth(tgt.getHealth() - dmg);
			if (tgt instanceof Mob) {
				final MobGoal gl = MobGoal.getMobGoal((Mob) tgt);
				if (gl != null) gl.setTgt(dmgr);
			}
		} else {//dead
			tgt.setHealth(0d);
		}
	}
	
	@EventHandler
	public void onPot(final PotionSplashEvent e) {
		if (e.getEntity().getShooter() instanceof final Mob mb) {
            final MobGoal mGl = MobGoal.getMobGoal(mb);
			if (mGl == null) return;
			final char cc = mGl.team().cc;
			final float dmg = mGl.atkDm();
			for (final LivingEntity le : e.getAffectedEntities()) {
				final Rusher ors = Rusher.getRusher(le);
				if (ors != null) {
					if (ors.team().cc == cc) {
						le.setHealth(Math.min(le.getAttribute(Attribute.GENERIC_MAX_HEALTH)
							.getBaseValue(), le.getHealth() + (dmg * mGl.team().mobDmg)));
					} else {
						mb.attack(le);
					}
				} else if (le instanceof Mob) {
					final Nexus onx = MobGoal.getMobTeam((Mob) le);
					if (onx == null) continue;
					if (onx.cc == cc) {
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
		final Rusher rs = Rusher.getRusher(e.getEntity());
		if (rs != null && rs.arena() != null) {
			e.setCancelled(true);
			rs.arena().killRs(rs);
		} else if (e.getEntity() instanceof final Mob mb) {
            e.getDrops().clear();
			e.setDroppedExp((int) mb.getAttribute(
				Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		} else return;
		
		final LivingEntity klr = getKiller(e.getEntity());
		if (klr == null) return;
		final Rusher krs = Rusher.getRusher(klr);
		final Arena ar;
		if (krs != null) {
			ar = krs.arena();
			if (ar != null) ar.addKill(krs, rs != null);
		} else if (klr instanceof Mob) {
			final Nexus nx = MobGoal.getMobTeam((Mob) klr);
			ar = nx == null ? null : nx.rs.arena();
			if (ar != null) ar.addKill(nx.rs, rs != null);
		}
	}

	private @Nullable LivingEntity getKiller(final LivingEntity ent) {
		if (ent.getLastDamageCause() instanceof final EntityDamageByEntityEvent ev) {
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
		final Projectile prj = e.getEntity();
		if (prj.getShooter() instanceof final LivingEntity le) {
			switch (e.getEntityType()) {
				case SNOWBALL:
					if (((ThrowableProjectile) prj).getItem().getType() != Material.SNOWBALL) {
						prj.getWorld().createExplosion(prj.getLocation(),
							(float) prj.getVelocity().lengthSquared(),
							false, false, le);
						prj.remove();
						return;
					}
					break;
				case ARROW, SPECTRAL_ARROW:
					if (e.getHitEntity() instanceof final LivingEntity tgt) {
						switch (tgt.getType()) {
							case ENDERMAN:
								tgt.damage(prj.getVelocity().lengthSquared() * 2d, prj);
								prj.remove();
								return;
							default:
								if (tgt.getEquipment() == null) break;
								if (tgt.getEquipment().getItemInOffHand().getType()
									== Material.SHIELD && Main.srnd.nextBoolean()) {
									e.setCancelled(true);
									prj.remove();
								}
								break;
						}
						return;
					}
					break;
				default: break;
			}
		}

		if (e.getHitBlock() != null) {
			prj.remove();
		}
	}
}
