package com.zedfalcon.bannerclaim;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.block.Block;

public class Claim {
    private final ProtectedRegion region;
    private final Block bannerBlock;

    public Claim(ProtectedRegion region, Block bannerBlock) {
        this.region = region;
        this.bannerBlock = bannerBlock;
    }

    public ProtectedRegion getRegion() {
        return this.region;
    }
    public Block getBannerBlock() {
        return this.bannerBlock;
    }
}
