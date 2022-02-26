package com.zedfalcon.bannerclaim;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BannerClaim extends JavaPlugin {
    private static BannerClaim PLUGIN;
    private static RegionContainer REGION_CONTAINER;
    private static GuildManager GUILD_MANAGER;
    public static final String CLAIM_BANNER_NAME_PREFIX = "claimBanner-";
    public static final ClaimBannerTier[] CLAIM_BANNER_TIERS = new ClaimBannerTier[]{
            new ClaimBannerTier("White", 16, 3, Material.WHITE_BANNER, ChatColor.WHITE, Material.WHITE_STAINED_GLASS_PANE),
            new ClaimBannerTier("Blue", 48, 25, Material.BLUE_BANNER, ChatColor.BLUE, Material.BLUE_STAINED_GLASS_PANE),
            new ClaimBannerTier("Purple", 80, 75, Material.PURPLE_BANNER, ChatColor.DARK_PURPLE, Material.PURPLE_STAINED_GLASS_PANE),
            new ClaimBannerTier("Gold", 112, 150, Material.ORANGE_BANNER, ChatColor.GOLD, Material.ORANGE_STAINED_GLASS_PANE),
    };

    public static BannerClaim getPlugin() {
        return BannerClaim.PLUGIN;
    }

    public static RegionContainer getRegionContainer() {
        return BannerClaim.REGION_CONTAINER;
    }

    public static GuildManager getGuildManager() {
        return BannerClaim.GUILD_MANAGER;
    }

    public WorldGuardPlugin worldGuardPlugin;
//    private File configFile;

    public BannerClaim() {
        BannerClaim.PLUGIN = this;
        BannerClaim.GUILD_MANAGER = new GuildManager();
    }

    @Override
    public void onEnable() {
        super.getServer().getPluginManager().registerEvents(new BannerPlaceListener(), this);
        super.getServer().getPluginManager().registerEvents(new BreakClaimBannerListener(), this);

        Objects.requireNonNull(super.getCommand("guild")).setExecutor(new GuildCommand());

        this.worldGuardPlugin = (WorldGuardPlugin) super.getServer().getPluginManager().getPlugin("WorldGuard");
        BannerClaim.REGION_CONTAINER = WorldGuard.getInstance().getPlatform().getRegionContainer();

        GuildFileStorage.loadAllGuilds();
    }

    @Override
    public void onDisable() {
        GuildFileStorage.saveAllGuilds();
    }
}
