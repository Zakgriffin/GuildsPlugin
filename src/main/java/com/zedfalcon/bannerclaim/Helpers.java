package com.zedfalcon.bannerclaim;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;
import java.util.function.Consumer;

record GuildClaimPair(Guild guild, Claim claim) {}


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

    public static Set<Block> getAdjacentBlocks(Block block) {
        return Set.of(
                block.getRelative(BlockFace.UP),
                block.getRelative(BlockFace.DOWN),
                block.getRelative(BlockFace.WEST),
                block.getRelative(BlockFace.EAST),
                block.getRelative(BlockFace.NORTH),
                block.getRelative(BlockFace.SOUTH)
        );
    }

//    public static boolean isBlackBanner(Material material) {
//        return material == Material.BLACK_BANNER || material == Material.BLACK_WALL_BANNER;
//    }

    public static void forChunksInRadius(Block block, int chunkRadius, Consumer<Chunk> f) {
        for (int chX = -chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = -chunkRadius; chZ <= chunkRadius; chZ++) {
                f.accept(block.getRelative(chX * 16, 0, chZ * 16).getChunk());
            }
        }
    }

    public static ItemStack createClaimBanner(ClaimBannerTier tier) {
        ItemStack claimBanner = new ItemStack(tier.banner());
        ItemMeta meta = claimBanner.getItemMeta();
        assert meta != null;
        meta.setLocalizedName(BannerClaim.CLAIM_BANNER_NAME_PREFIX + tier.name());
        meta.setDisplayName(tier.chatColor() + tier.name() + " Claim Banner");
        claimBanner.setItemMeta(meta);
        return claimBanner;
    }
}
