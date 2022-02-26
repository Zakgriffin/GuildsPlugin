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