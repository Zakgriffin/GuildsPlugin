package com.zedfalcon.bannerclaim;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BreakClaimBannerListener implements Listener {
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getChangedType() != Material.AIR) return;
        Block block = event.getBlock();
        World world = block.getWorld();
        RegionManager regions = BannerClaim.getRegionContainer().get(BukkitAdapter.adapt(block.getWorld()));

        if (regions == null) return;

        BlockVector3 blockVector = BlockVector3.at(block.getX(), block.getY(), block.getZ());
        for (ProtectedRegion region : regions.getApplicableRegions(blockVector)) {
            Guild guild = BannerClaim.getGuildManager().guildOwningRegion(region);
            if (guild == null) continue;
            int claimWidth = region.getMaximumPoint().subtract(region.getMinimumPoint()).getX(); // TODO no ew
            int claimRadius = claimWidth / 2;
            Optional<ClaimBannerTier> tierOption = Arrays.stream(BannerClaim.CLAIM_BANNER_TIERS)
                    .filter(t -> t.claimWidth() == claimWidth).findFirst();
            if(tierOption.isEmpty()) continue;
            ClaimBannerTier tier = tierOption.get();

            boolean removed = guild.removeClaimIfBannerBrokenAt(block);
            if (removed) {
                // TODO OH NO NO NO NO NO NO
                Bukkit.getScheduler().runTaskLater(BannerClaim.getPlugin(), () -> {
                    Collection<Entity> nearbyItems = world.getNearbyEntities(block.getLocation(), 1, 1, 1, (e) -> e instanceof Item);
                    for(Entity entity : nearbyItems) {
                        if(entity.getName().contains("Banner")) {
                            entity.remove();
                        }
                    }
                    world.dropItemNaturally(block.getLocation(), new ItemStack(Helpers.createClaimBanner(tier)));
                }, 0);

                world.playSound(block.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1f);
                Helpers.forChunksInRadius(block, 1, (chunk) -> {
                    for (Entity entity : chunk.getEntities()) {
                        if (entity instanceof Player player) {
                            new ClaimVisualization(player, block, Material.BLACK_STAINED_GLASS_PANE, claimRadius).visualizeClaimBounds();
                        }
                    }
                });
            }
        }
    }
}
