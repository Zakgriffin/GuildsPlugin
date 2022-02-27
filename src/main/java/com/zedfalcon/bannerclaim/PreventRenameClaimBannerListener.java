package com.zedfalcon.bannerclaim;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PreventRenameClaimBannerListener implements Listener {
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.ANVIL && event.getSlot() == 2) {
            ItemStack stack = inventory.getItem(0);
            if(stack == null) return;
            ItemMeta meta = stack.getItemMeta();
            if(meta == null) return;
            String displayName = meta.getDisplayName();
            if (Arrays.stream(ClaimBannerTier.CLAIM_BANNER_TIERS).anyMatch(t -> t.displayName().equals(displayName))) {
                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot rename claim banners");
            }
        }
    }
}
