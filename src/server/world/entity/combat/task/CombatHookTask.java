package server.world.entity.combat.task;

import server.core.worker.TaskFactory;
import server.core.worker.Worker;
import server.util.Misc;
import server.world.entity.Hit;
import server.world.entity.combat.CombatBuilder;
import server.world.entity.combat.CombatFactory;
import server.world.entity.combat.CombatHit;
import server.world.entity.combat.CombatType;
import server.world.entity.combat.prayer.CombatPrayer;
import server.world.entity.npc.Npc;
import server.world.entity.player.Player;
import server.world.map.Location;
import server.world.map.Position;

/**
 * A {@link Worker} implementation that dynamically handles every combat 'hook'
 * or turn during a combat session.
 * 
 * @author lare96
 */
public class CombatHookTask extends Worker {

    /** The builder assigned to this worker. */
    private CombatBuilder builder;

    /**
     * Create a new {@link CombatHookTask}.
     * 
     * @param builder
     *        the builder assigned to this worker.
     */
    public CombatHookTask(CombatBuilder builder) {
        super(1, false);
        this.builder = builder;
    }

    @Override
    public void fire() {

        /** Perform all cooldown functions here. */
        if (builder.isCooldownEffect()) {
            builder.decrementCooldown();

            if (builder.getCooldown() == 0) {
                builder.reset();
                this.cancel();
                return;
            }

            if (!builder.getEntity().isAutoRetaliate()) {
                return;
            }
        }

        /** If the current target is a player... */
        if (builder.getCurrentTarget().isPlayer()) {
            Player target = (Player) builder.getCurrentTarget();

            /**
             * If the target has teleported we reset the combat session for the
             * attacking entity.
             */
            if (target.getTeleportStage() > 0) {
                builder.reset();
                builder.getEntity().faceEntity(65535);
                builder.getEntity().getFollowWorker().cancel();
                builder.getEntity().setFollowing(false);
                builder.getEntity().setFollowingEntity(null);
                this.cancel();
                return;
            }

            if (builder.getEntity().isPlayer()) {
                Player player = (Player) builder.getEntity();

                /**
                 * If the attacking entity is a player then check if the target
                 * has ran out of the wilderness.
                 */
                if (!Location.inWilderness(target)) {
                    player.getPacketBuilder().sendMessage("Your target is not in the wilderness!");
                    builder.reset();
                    builder.getEntity().faceEntity(65535);
                    builder.getEntity().getFollowWorker().cancel();
                    builder.getEntity().setFollowing(false);
                    builder.getEntity().setFollowingEntity(null);
                    this.cancel();
                    return;
                }
            }
        }

        /** If the attacker is a player.... */
        if (builder.getEntity().isPlayer()) {
            Player player = (Player) builder.getEntity();

            /** Determine the combat strategy for this hook. */
            CombatFactory.determinePlayerStrategy(player);

            /**
             * Checks if you are trying to attack another entity while in
             * combat.
             */
            if (!Location.inMultiCombat(player) && player.getCombatBuilder().isBeingAttacked() && player.getCombatBuilder().getCurrentTarget() != player.getCombatBuilder().getLastAttacker()) {
                player.getPacketBuilder().sendMessage("You are already under attack!");
                builder.reset();
                builder.getEntity().faceEntity(65535);
                builder.getEntity().getFollowWorker().cancel();
                builder.getEntity().setFollowing(false);
                builder.getEntity().setFollowingEntity(null);
                this.cancel();
                return;
            }

            /**
             * Checks if you are trying to attack another entity who is in
             * combat.
             */
            if (!Location.inMultiCombat(builder.getCurrentTarget()) && builder.getCurrentTarget().getCombatBuilder().isBeingAttacked() && builder.getCurrentTarget().getCombatBuilder().getLastAttacker() != builder.getEntity()) {
                player.getPacketBuilder().sendMessage("They are already under attack!");
                builder.reset();
                builder.getEntity().faceEntity(65535);
                builder.getEntity().getFollowWorker().cancel();
                builder.getEntity().setFollowing(false);
                builder.getEntity().setFollowingEntity(null);
                this.cancel();
                return;
            }
        }

        /**
         * Checks if this attacker is an npc and if it needs to retreat back to
         * its home position.
         */
        if (builder.getEntity().isNpc()) {
            Npc npc = (Npc) builder.getEntity();

            if (npc.getCombatBuilder().getCurrentTarget().getCombatBuilder().isCooldownEffect() && !npc.getPosition().withinDistance(npc.getOriginalPosition(), 5) && npc.getDefinition().isRetreats() || !builder.getCurrentTarget().getCombatBuilder().isBeingAttacked() && !npc.getPosition().withinDistance(npc.getOriginalPosition(), 5) && npc.getDefinition().isRetreats()) {
                npc.getCombatBuilder().reset();
                npc.faceEntity(65535);
                npc.getFollowWorker().cancel();
                npc.setFollowing(false);
                npc.setFollowingEntity(null);
                npc.getMovementQueue().walk(npc.getOriginalPosition());
                this.cancel();
                return;
            }
        }

        /** If the attacker or target have unregistered then stop. */
        if (builder.getCurrentTarget().isUnregistered() || builder.getEntity().isUnregistered()) {
            builder.reset();
            this.cancel();
            return;
        }

        /** If this attacker has died then stop. */
        if (builder.getEntity().isHasDied()) {
            return;
        }

        /** If the target has died then stop. */
        if (builder.getCurrentTarget().isHasDied()) {
            builder.reset();
            builder.getEntity().faceEntity(65535);
            builder.getEntity().getFollowWorker().cancel();
            builder.getEntity().setFollowing(false);
            builder.getEntity().setFollowingEntity(null);
            this.cancel();
            return;
        }

        /** Decrement the attack timer. */
        builder.decrementAttackTimer();

        /**
         * The attack timer has reached 0 which means we are allowed to attack
         * now.
         */
        if (builder.getAttackTimer() == 0) {

            /** Check if the attacker is close enough to attack. */
            Position attackerPosition = builder.getEntity().getPosition().clone();
            Position victimPosition = builder.getCurrentTarget().getPosition().clone();

            if (!builder.getEntity().getMovementQueue().isLockMovement()) {
                if (!builder.getEntity().getMovementQueue().isRunToggled() && !attackerPosition.withinDistance(victimPosition, builder.getCurrentStrategy().getDistance(builder.getEntity())) || builder.getEntity().getMovementQueue().isRunToggled() && !attackerPosition.withinDistance(victimPosition, (builder.getCurrentStrategy().getDistance(builder.getEntity()) + 3))) {
                    return;
                }
            } else {
                if (!attackerPosition.withinDistance(victimPosition, builder.getCurrentStrategy().getDistance(builder.getEntity()))) {
                    return;
                }
            }

            /** Check if the attack can be made on this hook. */
            if (!builder.getCurrentStrategy().prepareAttack(builder.getEntity())) {
                builder.reset();
                builder.getEntity().faceEntity(65535);
                builder.getEntity().getFollowWorker().cancel();
                builder.getEntity().setFollowing(false);
                builder.getEntity().setFollowingEntity(null);
                return;
            }

            /** To keep track of the damage dealt. */
            int totalDamage = 0;

            /** Calculate the hit for this hook. */
            CombatHit combatHit = builder.getCurrentStrategy().attack(builder.getEntity(), builder.getCurrentTarget());

            if (combatHit != null && combatHit.getHits() != null) {

                /**
                 * If the attacker is an npc and protection prayers are active
                 * reduce the damage to 0 when the combat type corresponds to
                 * the correct protection prayer.
                 */
                if (builder.getCurrentTarget().isPlayer() && builder.getEntity().isNpc()) {
                    Player player = (Player) builder.getCurrentTarget();

                    if (combatHit.getHitType() == CombatType.MELEE && CombatPrayer.isPrayerActivated(player, CombatPrayer.PROTECT_FROM_MELEE)) {
                        for (int i = 0; i < combatHit.getHits().length; i++) {
                            combatHit.getHits()[i] = new Hit(0);
                        }
                    } else if (combatHit.getHitType() == CombatType.MAGIC && CombatPrayer.isPrayerActivated(player, CombatPrayer.PROTECT_FROM_MAGIC)) {
                        for (int i = 0; i < combatHit.getHits().length; i++) {
                            combatHit.getHits()[i] = new Hit(0);
                        }
                    } else if (combatHit.getHitType() == CombatType.RANGE && CombatPrayer.isPrayerActivated(player, CombatPrayer.PROTECT_FROM_MISSILES)) {
                        for (int i = 0; i < combatHit.getHits().length; i++) {
                            combatHit.getHits()[i] = new Hit(0);
                        }
                    }

                    /**
                     * If the attacker and target are players and protection
                     * prayers are active reduce the damage to 0 at random when
                     * the combat type corresponds to the correct protection
                     * prayer.
                     */
                } else if (builder.getCurrentTarget().isPlayer() && builder.getEntity().isPlayer()) {
                    Player player = (Player) builder.getEntity();
                    Player target = (Player) builder.getCurrentTarget();

                    /**
                     * If the player is wearing full veracs they will hit
                     * through prayer no matter what.
                     */
                    if (!CombatFactory.isWearingFullVeracs(player)) {
                        if (combatHit.getHitType() == CombatType.MELEE && CombatPrayer.isPrayerActivated(target, CombatPrayer.PROTECT_FROM_MELEE)) {
                            if (Misc.getRandom().nextInt(4) == 0) {
                                for (int i = 0; i < combatHit.getHits().length; i++) {
                                    combatHit.getHits()[i] = new Hit(0);
                                }
                            }
                        } else if (combatHit.getHitType() == CombatType.MAGIC && CombatPrayer.isPrayerActivated(target, CombatPrayer.PROTECT_FROM_MAGIC)) {
                            if (Misc.getRandom().nextInt(4) == 0) {
                                for (int i = 0; i < combatHit.getHits().length; i++) {
                                    combatHit.getHits()[i] = new Hit(0);
                                }
                            }
                        } else if (combatHit.getHitType() == CombatType.RANGE && CombatPrayer.isPrayerActivated(target, CombatPrayer.PROTECT_FROM_MISSILES)) {
                            if (Misc.getRandom().nextInt(4) == 0) {
                                for (int i = 0; i < combatHit.getHits().length; i++) {
                                    combatHit.getHits()[i] = new Hit(0);
                                }
                            }
                        }
                    }
                }

                /** Calculate the total damage that will be dealt to the target. */
                if (combatHit.getHits().length == 1) {
                    totalDamage += combatHit.getHits()[0].getDamage();
                } else if (combatHit.getHits().length == 2) {
                    totalDamage += combatHit.getHits()[0].getDamage();
                    totalDamage += combatHit.getHits()[1].getDamage();
                } else if (combatHit.getHits().length == 3) {
                    totalDamage += combatHit.getHits()[0].getDamage();
                    totalDamage += combatHit.getHits()[1].getDamage();
                    totalDamage += combatHit.getHits()[2].getDamage();
                } else if (combatHit.getHits().length == 4) {
                    builder.getCurrentTarget().dealQuadrupleDamage(combatHit.getHits()[0], combatHit.getHits()[1], combatHit.getHits()[2], combatHit.getHits()[3]);
                    totalDamage += combatHit.getHits()[0].getDamage();
                    totalDamage += combatHit.getHits()[1].getDamage();
                    totalDamage += combatHit.getHits()[2].getDamage();
                    totalDamage += combatHit.getHits()[3].getDamage();
                }

                /** Schedule a task based on the combat type. */
                if (combatHit.getHitType() == CombatType.MELEE) {
                    TaskFactory.getFactory().submit(new CombatHitTask(builder.getEntity(), builder.getCurrentTarget(), combatHit.getHits(), combatHit.getHitType(), totalDamage, 1, true));
                } else if (combatHit.getHitType() == CombatType.RANGE) {
                    TaskFactory.getFactory().submit(new CombatHitTask(builder.getEntity(), builder.getCurrentTarget(), combatHit.getHits(), combatHit.getHitType(), totalDamage, 2, false));
                } else if (combatHit.getHitType() == CombatType.MAGIC) {
                    TaskFactory.getFactory().submit(new CombatHitTask(builder.getEntity(), builder.getCurrentTarget(), combatHit.getHits(), combatHit.getHitType(), totalDamage, 3, false));
                }
            }

            /** Reset this combat hook and prepare for the next hook. */
            builder.getCurrentTarget().getCombatBuilder().setLastAttacker(builder.getEntity());
            builder.getEntity().getCombatBuilder().setAttackTimer(builder.getCurrentStrategy().attackTimer(builder.getEntity()));
            builder.getCurrentTarget().getLastCombat().reset();
            builder.getEntity().getLastFight().reset();
            builder.getCurrentTarget().getCombatBuilder().resetCooldown();
            builder.getEntity().facePosition(builder.getCurrentTarget().getPosition());
        }
    }
}
