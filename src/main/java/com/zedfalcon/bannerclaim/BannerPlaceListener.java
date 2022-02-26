package com.zedfalcon.bannerclaim;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class BannerPlaceListener implements Listener {
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        ItemStack heldItemStack = e.getItemInHand();
        if (!Tag.BANNERS.isTagged(heldItemStack.getType())) return;
        ItemMeta itemMeta = heldItemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.getLocalizedName().contains(BannerClaim.CLAIM_BANNER_NAME_PREFIX)) return;

        e.setCancelled(true);

        Guild guild = BannerClaim.getGuildManager().guildOfPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You cannot claim since you are not part of a guild");
            return;
        }

        Optional<ClaimBannerTier> optionalTier = Arrays.stream(BannerClaim.CLAIM_BANNER_TIERS)
                .filter(c -> c.banner() == heldItemStack.getType()).findAny();

        if(optionalTier.isEmpty()) return;
        ClaimBannerTier tier = optionalTier.get();

        Block bannerBlock = e.getBlockPlaced();
        World world = bannerBlock.getWorld();
        int claimRadius = tier.claimWidth() / 2;
        BlockVector3 min = BlockVector3.at(bannerBlock.getX() - claimRadius, 0, bannerBlock.getZ() - claimRadius);
        BlockVector3 max = BlockVector3.at(bannerBlock.getX() + claimRadius, 256, bannerBlock.getZ() + claimRadius);
        ProtectedRegion claimRegion = new ProtectedCuboidRegion(UUID.randomUUID().toString(), min, max);

        RegionManager regions = BannerClaim.getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regions == null) return;
        for (ProtectedRegion overlappingRegion : regions.getApplicableRegions(claimRegion)) {
            if (!overlappingRegion.isMember(WorldGuardPlugin.inst().wrapPlayer(player))) {
                player.sendMessage(ChatColor.RED + "Another guild already has a claim within this space");
                return;
            }
        }

        Claim claim = new Claim(claimRegion, bannerBlock, tier);
        guild.addClaim(claim);

        player.sendMessage(ChatColor.GREEN + "" + tier.claimWidth() + "x" + tier.claimWidth() + " claim created for " + guild.getName());
        world.playSound(bannerBlock.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1f);
        new ClaimVisualization(player, bannerBlock, tier.highlightGlassPane(), claimRadius).visualizeClaimBounds();

        e.setCancelled(false);
    }
}
