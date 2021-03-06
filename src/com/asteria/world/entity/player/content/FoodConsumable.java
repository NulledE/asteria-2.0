package com.asteria.world.entity.player.content;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import com.asteria.util.Utility;
import com.asteria.world.entity.Animation;
import com.asteria.world.entity.player.Player;
import com.asteria.world.entity.player.skill.Skill;
import com.asteria.world.entity.player.skill.SkillData;
import com.asteria.world.entity.player.skill.Skills;
import com.asteria.world.item.Item;

/**
 * An enumeration managing consumable food types.
 * 
 * @author Ryley Kimmel <ryley.kimmel@live.com>
 */
public enum FoodConsumable {

    SHRIMP(3, 315),
    LOBSTER(12, 379),
    MANTA_RAY(22, 391),
    MONKFISH(16, 7946),
    MACKREL(6, 355),
    SALMON(9, 329),
    SEA_TURTLE(22, 397),
    SHARK(20, 385),
    SWORDFISH(14, 373),
    TROUT(7, 333),
    TUNA(10, 361),
    TUNA_POTATO(22, 7060),
    ANCHOVIES(2, 319),
    CABBAGE(2, 1965),
    CRAYFISH(2, 13432),
    EQUA_LEAVES(2, 2128),
    ONION(2, 1957),
    BANANA(2, 1957),
    CHEESE(2, 1985),
    DWELLBERRIES(2, 2126),
    JANGERBERRIES(2, 247),
    LIME(2, 2120),
    LEMON(2, 2102),
    ORANGE(2, 2108),
    SPICY_TOMATO(2, 9994),
    SPINACH_ROLL(2, 1969),

    TOMATO(2, 1982),
    ROAST_BIRD_MEAT(2, 9980),
    COOKED_MEAT(2, 2142),
    UGTHANKI_MEAT(2, 1861),
    SARDINE(2, 325),

    BREAD(2, 2309),
    COOKED_RABBIT(2, 3228),
    FROGSPAWN_GUMBO(2, 10961),
    COOKED_CHICKEN(2, 2140),
    KARAMBWANJI(2, 3151),
    CHOCOLATE_BAR(2, 1973),

    SPICY_MINCED_MEAT(2, 9996),
    ROE(2, 11324),
    WHITE_TREE_FRUIT(2, 6469),
    HERRING(2, 347),
    BAKED_POTATO(2, 6701),

    RED_BANANA(2, 7572),
    SLICED_RED_BANANA(2, 7574),
    TCHIKI_MONKEY_NUTS(2, 7573),
    TCHIKI_NUT_PASTE(2, 7575),
    SPICY_SUACE(2, 7072),
    MINCED_MEAT(2, 7070),
    FROG_SPAWN(2, 5004),

    TOAD_CRUNCHIES(2, 9538),
    BAGUETTE(2, 6961),
    FILLETS(2, 10969),
    EEL_SUSHI(2, 10971),
    GRUBS_A_LA_MODE(2, 10966),

    MUSHROOMS(2, 10968),
    LOACH(2, 10970),
    ROST_FROG(2, 10967),
    GIANT_CARP(2, 337),
    GIANT_FROG_LEGS(2, 4517),

    COD(4, 339),
    PIKE(4, 351),
    BASS(9, 365),

    STRAWBERRY(Utility.exclusiveRandom(3), 5323),
    COOKED_SWEETCORN(Utility.exclusiveRandom(6), 5986),
    WATERMELON_SLICE(Utility.exclusiveRandom(5), 5984),

    CAKE(4, 1891, 1893, 1895),
    CHOCOLATE_CAKE(5, 1897, 1899, 1901),
    PLAIN_PIZZA(7, 2289, 2291),
    MEAT_PIZZA(8, 2293, 2295),
    ANCHOVY_PIZZA(9, 2297, 2299),
    PINEAPPLE_PIZZA(11, 2301, 2303),

    REDBERRY_PIE(2, 2325, 2333) {
        @Override
        public long getDelay() {
            return 600;
        }
    },

    APPLE_PIE(7, 2323, 2335) {
        @Override
        public long getDelay() {
            return 600;
        }
    },

    MEAT_PIE(6, 2327, 2331) {
        @Override
        public long getDelay() {
            return 600;
        }
    },

    /**
     * @see <a href=http://2007.runescape.wikia.com/wiki/Kebab>Kebab effects</a>
     */
    KEBAB(-1, 1971) {

        /**
         * (non-Javadoc)
         * 
         * This method will always return {@code false} to avoid the task which
         * notifies of health regain.
         * 
         * @see com.asteria.world.entity.player.content.FoodConsumable#fireAction(com.asteria.world.entity.player.Player)
         */
        @Override
        public void foodEffect(Player player) {
            Skill skill = player.getSkills()[Skills.HITPOINTS];
            int realLevel = skill.getLevelForExperience();

            if (Utility.random(100F) >= 61.24F) {
                /* 10% of total hit points */
                int healAmount = Math.round((10 * 100F) / realLevel);
                skill.increaseLevel(healAmount, realLevel);
                player.getPacketBuilder().sendMessage(
                    "It restores some life points.");
                return;
            }

            if (Utility.random(100F) >= 21.12F) {
                skill.increaseLevel(Utility.inclusiveRandom(10, 20), realLevel);
                player.getPacketBuilder().sendMessage(
                    "That was a good kebab. You feel a lot better.");
                return;
            }

            if (Utility.random(100F) >= 8.71F) {
                player.getPacketBuilder().sendMessage(
                    "The kebab didn't seem to do a lot.");
                return;
            }

            if (Utility.random(100F) >= 3.65F) {
                skill.increaseLevel(30, realLevel);
                player.getSkills()[Skills.ATTACK].increaseLevel(Utility.exclusiveRandom(3));
                player.getSkills()[Skills.STRENGTH].increaseLevel(Utility.exclusiveRandom(3));
                player.getSkills()[Skills.DEFENCE].increaseLevel(Utility.exclusiveRandom(3));
                player.getPacketBuilder().sendMessage(
                    "Wow, that was an amazing kebab! You feel really invigorated.");
                return;
            }

            if (Utility.random(100F) >= 3.28F) {
                player.getSkills()[Skills.ATTACK].decreaseLevel(Utility.exclusiveRandom(3));
                player.getSkills()[Skills.STRENGTH].decreaseLevel(Utility.exclusiveRandom(3));
                player.getSkills()[Skills.DEFENCE].decreaseLevel(Utility.exclusiveRandom(3));
                player.getPacketBuilder().sendMessage(
                    "That tasted a bit dodgy. You feel a bit ill.");
                return;
            }

            if (Utility.random(100F) >= 2.00F) {
                /* Any random skill that is not hit points */
                int id = Utility.inclusiveRandomExcludes(0,
                    player.getSkills().length, Skills.HITPOINTS);
                Skill randomSkill = player.getSkills()[id];

                randomSkill.decreaseLevel(Utility.exclusiveRandom(3));
                player.getPacketBuilder().sendMessage(
                    "Eating the kebab has damaged your " + SkillData.getSkill(
                        id).name().toLowerCase().replaceAll("_", " ") + " stat.");
                return;
            }
        }
    };

    /**
     * The amount of hit points this food heals.
     */
    private final int healAmount;

    /**
     * The ids which represent this food type.
     */
    private final int[] ids;

    /**
     * A set of consumable foods.
     */
    private static final Set<FoodConsumable> ALL_FOOD = EnumSet.allOf(FoodConsumable.class);

    /**
     * Constructs a new {@link FoodConsumable} with the specified heal amount
     * and ids.
     * 
     * @param healAmount
     *            The amount of hit points this food heals.
     * @param ids
     *            The ids of this food type.
     */
    private FoodConsumable(int healAmount, int... ids) {
        this.ids = ids;
        this.healAmount = healAmount;
    }

    /**
     * Returns the amount of hit points this food heals.
     */
    public final int getHealAmount() {
        return healAmount;
    }

    /**
     * Returns the ids of this food type.
     */
    public final int[] getIds() {
        return ids;
    }

    @Override
    public final String toString() {
        return name().toLowerCase().replace("_", " ");
    }

    /**
     * Returns the delay before consuming another food type.
     * 
     * <p>
     * This method may be oveerridden to provide a different functionality for
     * foods that have a different delay.
     * </p>
     */
    public long getDelay() {
        return 600 * 3; // default 3 ticks
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asteria.util.Utility.GenericAction#fireAction(java.lang.Object)
     * 
     *      <p>
     *      This method may be overridden to provide a different functionality
     *      for such foods as kebabs.
     *      </p>
     */
    public void foodEffect(Player player) {
        Skill skill = player.getSkills()[Skills.HITPOINTS];
        int realLevel = skill.getLevelForExperience();
        if (skill.getLevel() >= realLevel) {
            return;
        }

        skill.increaseLevel(getHealAmount(), realLevel);
        player.getPacketBuilder().sendMessage("It healed some health.");
    }

    /**
     * Returns the chatbox message printed when a food is consumed.
     * 
     * <p>
     * This method may be overridden to provide a different functionality for
     * foods which have a different chatbox message.
     * </p>
     */
    public String getMessage() {
        return (ids.length > 1 ? "You eat a slice of the " : "You eat the ") + toString() + ".";
    }

    /**
     * Returns a new Item object if the consumed item is to be replaced.
     * 
     * @param item
     *            The item.
     * @return The new item.
     */
    private static Optional<Item> getReplacementItem(Item item) {
        Optional<FoodConsumable> food = forId(item.getId());
        if (food.isPresent()) {
            int length = food.get().getIds().length;
            for (int index = 0; index < length; index++) {
                if (food.get().getIds()[index] == item.getId() && index + 1 < length) {
                    return Optional.of(new Item(food.get().getIds()[index + 1]));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the food consumable for the specified id.
     * 
     * @param id
     *            The id.
     * @return The food consumable, or null if it does not exist.
     */
    private static Optional<FoodConsumable> forId(int id) {
        for (FoodConsumable food : ALL_FOOD) {
            for (int foodId : food.getIds()) {
                if (id == foodId) {
                    return Optional.of(food);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to consume the specified item in the specified slot for the
     * specified player.
     * 
     * @param player
     *            The player.
     * @param item
     *            The item.
     * @param slot
     *            The slot.
     * @return {@code true} if the item was successfully consumed, otherwise
     *         {@code false}.
     */
    public static boolean consume(Player player, Item item, int slot) {
        Optional<FoodConsumable> food = forId(item.getId());
        if (!food.isPresent()) {
            return false;
        }
        if (player.isDead()) {
            return false;
        }
        // TODO: Check duel rule for no food
        if (player.getEatingTimer().elapsed() < food.get().getDelay()) {
            return false;
        }

        player.animation(new Animation(829));
        player.getEatingTimer().reset();
        player.getPotionTimer().reset();

        player.getInventory().remove(item, slot);

        Optional<Item> replacement = getReplacementItem(item);
        if (replacement.isPresent()) {
            player.getInventory().set(slot, replacement.get());
            player.getInventory().refresh();
        }

        player.getPacketBuilder().sendMessage(food.get().getMessage());
        food.get().foodEffect(player);
        Skills.refresh(player, Skills.HITPOINTS);
        return true;
    }
}