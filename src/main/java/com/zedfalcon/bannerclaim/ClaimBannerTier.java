package com.zedfalcon.bannerclaim;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.List;

record ClaimBannerTier(
        String name,
        int claimWidth,
        int price,
        DyeColor dyeColor,
        ChatColor chatColor,
        Material highlightGlassPane
) {
    public static final ClaimBannerTier[] CLAIM_BANNER_TIERS = new ClaimBannerTier[]{
            new ClaimBannerTier("White", 16, 3, DyeColor.WHITE, ChatColor.WHITE, Material.WHITE_STAINED_GLASS_PANE),
            new ClaimBannerTier("Blue", 48, 25, DyeColor.LIGHT_BLUE, ChatColor.AQUA, Material.LIGHT_BLUE_STAINED_GLASS_PANE),
            new ClaimBannerTier("Purple", 80, 75, DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE, Material.MAGENTA_STAINED_GLASS_PANE),
            new ClaimBannerTier("Gold", 112, 150, DyeColor.YELLOW, ChatColor.YELLOW, Material.YELLOW_STAINED_GLASS_PANE),
    };

    public static ItemStack createClaimBanner(ClaimBannerTier tier) {
        ItemStack claimBanner = new ItemStack(Material.BLACK_BANNER);

        BannerMeta meta = (BannerMeta) claimBanner.getItemMeta();
        assert meta != null;
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern(tier.dyeColor, PatternType.RHOMBUS_MIDDLE));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.SKULL));
        patterns.add(new Pattern(tier.dyeColor, PatternType.BORDER));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.BORDER));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.STRAIGHT_CROSS));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.FLOWER));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_TOP));
        meta.setPatterns(patterns);

        meta.setLocalizedName(BannerClaim.CLAIM_BANNER_NAME_PREFIX + tier.name());
        meta.setDisplayName(tier.chatColor() + tier.name() + " Claim Banner");
        claimBanner.setItemMeta(meta);
        return claimBanner;
    }
}
