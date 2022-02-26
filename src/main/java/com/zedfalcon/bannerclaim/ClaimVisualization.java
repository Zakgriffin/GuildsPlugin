package com.zedfalcon.bannerclaim;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.entity.Player;

public class ClaimVisualization {
    private final Player player;
    private final int x;
    private final int y;
    private final int z;
    private final World world;
    private final Material highlightGlassPane;
    private final int claimRadius;
    private final int height = 20;
    private final static int DELAY = 100;

    public ClaimVisualization(Player player, Block center, Material highlightGlassPane, int claimRadius) {
        this.player = player;
        this.x = center.getX();
        this.y = center.getY();
        this.z = center.getZ();
        this.highlightGlassPane = highlightGlassPane;
        this.claimRadius = claimRadius;
        this.world = player.getWorld();
    }

    public void visualizeClaimBounds() {
        showClaimCorner(x + claimRadius, y, z + claimRadius, BlockFace.WEST, BlockFace.NORTH);
        showClaimCorner(x + claimRadius, y, z - claimRadius, BlockFace.WEST, BlockFace.SOUTH);
        showClaimCorner(x - claimRadius, y, z + claimRadius, BlockFace.EAST, BlockFace.NORTH);
        showClaimCorner(x - claimRadius, y, z - claimRadius, BlockFace.EAST, BlockFace.SOUTH);

        Bukkit.getScheduler().runTaskLater(BannerClaim.getPlugin(), () -> {
            hideClaimCorner(x + claimRadius, y, z + claimRadius);
            hideClaimCorner(x + claimRadius, y, z - claimRadius);
            hideClaimCorner(x - claimRadius, y, z + claimRadius);
            hideClaimCorner(x - claimRadius, y, z - claimRadius);
        }, DELAY);
    }


    private void hideClaimCorner(int x, int y, int z) {
        for (int dy = -height; dy < height; dy++) {
            player.sendBlockChange(new Location(world, x, y + dy, z), world.getBlockAt(x, y + dy, z).getBlockData());
        }
    }


    private void showClaimCorner(int x, int y, int z, BlockFace blockFaceX, BlockFace blockFaceZ) {
        GlassPane paneBlockData = ((GlassPane) highlightGlassPane.createBlockData());
        paneBlockData.setFace(blockFaceX, true);
        paneBlockData.setFace(blockFaceZ, true);
        for (int dy = -height; dy < height; dy++) {
            player.sendBlockChange(new Location(world, x, y + dy, z), paneBlockData);
        }
    }
}

//    showClaimCorner(player, world, x, y, z, 1, 1);
//        showClaimCorner(player, world, x, y, z, 1, -1);
//        showClaimCorner(player, world, x, y, z, -1, 1);
//        showClaimCorner(player, world, x, y, z, -1, -1);

//    private void showClaimCorner(Player player, World world, int x, int y, int z, int xSign, int zSign) {
//        int height = 10;
//        for (int dy = -height; dy < height; dy++) {
//            player.sendBlockChange(new Location(world, x + CLAIM_RADIUS * xSign, y + dy, z + CLAIM_RADIUS * zSign), Material.BLUE_STAINED_GLASS_PANE.createBlockData());
//            player.sendBlockChange(new Location(world, x + CLAIM_RADIUS * xSign - xSign, y + dy, z + CLAIM_RADIUS * zSign), Material.BLUE_STAINED_GLASS_PANE.createBlockData());
//            player.sendBlockChange(new Location(world, x + CLAIM_RADIUS * xSign, y + dy, z + CLAIM_RADIUS * zSign - zSign), Material.BLUE_STAINED_GLASS_PANE.createBlockData());
//        }
//    }
//    private void showClaimParticle(World world, int x, int y, int z) {
//        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1);
//        world.spawnParticle(Particle.REDSTONE, x, y, z, 50, 0, 5, 0, dust);
//    }