package com.zedfalcon.bannerclaim;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.block.Block;

public record Claim(ProtectedRegion region, Block bannerBlock, ClaimBannerTier tier) {
    public ProtectedRegion getRegion() {
        return this.region;
    }

    public Block getBannerBlock() {
        return this.bannerBlock;
    }
}
