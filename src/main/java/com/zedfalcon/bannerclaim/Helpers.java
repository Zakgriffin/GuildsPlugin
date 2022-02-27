package com.zedfalcon.bannerclaim;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

record GuildClaimPair(Guild guild, Claim claim) {
}


public class Helpers {
    public static int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static void removeItemsByCount(Player player, Material material, int count) {
        int countToRemove = count;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == material) {
                int amountInStack = item.getAmount();
                if (amountInStack >= countToRemove) {
                    item.setAmount(amountInStack - countToRemove);
                    return;
                } else {
                    item.setAmount(0);
                    countToRemove -= amountInStack;
                }
            }
        }
    }

    public static void forChunksInRadius(Block block, int chunkRadius, Consumer<Chunk> f) {
        for (int chX = -chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = -chunkRadius; chZ <= chunkRadius; chZ++) {
                f.accept(block.getRelative(chX * 16, 0, chZ * 16).getChunk());
            }
        }
    }
}
