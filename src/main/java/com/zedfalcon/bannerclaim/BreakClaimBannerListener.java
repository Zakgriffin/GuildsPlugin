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

import java.util.Collection;

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
            GuildClaimPair guildClaimPair = BannerClaim.getGuildManager().guildAndClaimFromRegion(region);
            if (guildClaimPair == null) continue;
            Guild guild = guildClaimPair.guild();
            Claim claim = guildClaimPair.claim();

            if (!claim.bannerBlock().equals(block)) continue;

            guild.removeClaim(claim);

            world.playSound(block.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1f);
            Helpers.forChunksInRadius(block, 1, (chunk) -> {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Player player) {
                        new ClaimVisualization(player, block, Material.BLACK_STAINED_GLASS_PANE, claim.tier().claimWidth() / 2).visualizeClaimBounds();
                    }
                }
            });
        }
    }
}
