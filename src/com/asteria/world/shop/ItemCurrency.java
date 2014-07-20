package com.asteria.world.shop;

import com.asteria.world.entity.player.Player;
import com.asteria.world.item.Item;

/**
 * A {@link GenericCurrency} implementation that provides further functionality
 * for any currency that can be represented by an {@link Item}.
 * 
 * @author lare96
 */
public abstract class ItemCurrency implements GenericCurrency {

    @Override
    public void give(Player player, int amount) {
        player.getInventory().deleteItem(new Item(item(), amount));
    }

    @Override
    public void recieve(Player player, int amount) {
        player.getInventory().addItem(new Item(item(), amount));
    }

    @Override
    public int getAmount(Player player) {
        return player.getInventory().getContainer().getCount(item());
    }

    @Override
    public boolean inventoryFull(Player player) {
        return player.getInventory().getContainer().contains(item());
    }

    /**
     * The item representing this currency implementation.
     * 
     * @return the item representing this currency implementation.
     */
    public abstract int item();
}
