package com.zedfalcon.bannerclaim;

import org.bukkit.ChatColor;
import org.bukkit.Material;

record ClaimBannerTier(
        String name,
        int claimWidth,
        int price,
        Material banner,
        ChatColor chatColor,
        Material highlightGlassPane
) {
}
