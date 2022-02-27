package com.zedfalcon.bannerclaim;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;

public final class BannerClaim extends JavaPlugin {
    private static BannerClaim PLUGIN;
    private static RegionContainer REGION_CONTAINER;
    private static GuildManager GUILD_MANAGER;

    public static ClaimBannerTier claimBannerTierFromName(String name) {
        return Arrays.stream(ClaimBannerTier.CLAIM_BANNER_TIERS).filter(t -> t.name().equals(name)).findAny().orElse(null);
    }

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
        super.getServer().getPluginManager().registerEvents(new PlaceClaimBannerListener(), this);
        super.getServer().getPluginManager().registerEvents(new BreakClaimBannerListener(), this);
        super.getServer().getPluginManager().registerEvents(new PreventRenameClaimBannerListener(), this);

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
