package com.zedfalcon.bannerclaim;

import com.google.gson.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class GuildFileStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String GUILD_STORAGE_DIR = "guilds";
    private static final Plugin PLUGIN = BannerClaim.getPlugin();
    private static final String GUILD_FILE_PREFIX = "guild-";

    private static File guildsDir;

    public static void loadAllGuilds() {
        guildsDir = new File(PLUGIN.getDataFolder(), GUILD_STORAGE_DIR);
        if (!guildsDir.exists()) {
            if(!guildsDir.mkdir()) {
                Bukkit.getLogger().log(Level.WARNING, "failed to create guilds directory");
            }
        }

        File[] files = guildsDir.listFiles((dir, name) -> name.startsWith(GUILD_FILE_PREFIX));
        assert files != null;
        for (File file : files) {
            if (!file.getName().startsWith(GUILD_FILE_PREFIX)) continue;

            try {
                FileReader reader = new FileReader(file);
                JsonObject guildObj = GSON.fromJson(reader, JsonObject.class);
                reader.close();
                Guild guild = guildFromJson(guildObj);
                BannerClaim.getGuildManager().addGuild(guild);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Guild guildFromJson(JsonObject guildObj) {
        // id
        UUID id = UUID.fromString(guildObj.get("id").getAsString());

        // name
        String name = guildObj.get("name").getAsString();

        // members
        List<UUID> members = new ArrayList<>();
        for (JsonElement memberEl : guildObj.getAsJsonArray("members")) {
            UUID memberID = UUID.fromString(memberEl.getAsString());
            members.add(memberID);
        }

        // claims
        List<Claim> claims = new ArrayList<>();
        for (JsonElement claimEl : guildObj.getAsJsonArray("claims")) {
            JsonObject claimObj = claimEl.getAsJsonObject();

            // bannerLocation
            JsonObject bannerLocationObj = claimObj.getAsJsonObject("bannerLocation");
            int x = bannerLocationObj.get("x").getAsInt();
            int y = bannerLocationObj.get("y").getAsInt();
            int z = bannerLocationObj.get("z").getAsInt();
            UUID worldId = UUID.fromString(bannerLocationObj.get("world").getAsString());
            World world = PLUGIN.getServer().getWorld(worldId);
            assert world != null;
            Block bannerLocation = world.getBlockAt(x, y, z);

            // region
            String regionID = claimObj.get("region").getAsString();
            RegionManager regions = BannerClaim.getRegionContainer().get(BukkitAdapter.adapt(world));
            assert regions != null;
            ProtectedRegion region = regions.getRegion(regionID);

            // tier
            String tierName = claimObj.get("tier").getAsString();
            ClaimBannerTier tier = BannerClaim.claimBannerTierFromName(tierName);

            claims.add(new Claim(region, bannerLocation, tier));
        }

        return new Guild(id, name, members, claims);
    }

    public static void saveAllGuilds() {
        BannerClaim.getGuildManager().forEachGuild(GuildFileStorage::saveGuild);
    }

    public static void saveGuild(Guild guild) {
        try {
            String id = guild.getId().toString();
            File file = new File(guildsDir, GUILD_FILE_PREFIX + id + ".json");
            if (!file.exists()) {
                if(!file.createNewFile()) {
                    Bukkit.getLogger().log(Level.WARNING, "failed to create a guild file");
                }
            }

            FileWriter writer = new FileWriter(file);
            JsonObject guildObj = guildToJson(guild);
            GSON.toJson(guildObj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteGuild(Guild guild) {
        String id = guild.getId().toString();
        File file = new File(guildsDir, GUILD_FILE_PREFIX + id + ".json");
        if (file.exists()) {
            if(!file.delete()) {
                Bukkit.getLogger().log(Level.WARNING, "failed to delete a guild file");
            }
        }
    }

    private static JsonObject guildToJson(Guild guild) {
        JsonObject guildObj = new JsonObject();

        // id
        guildObj.add("id", new JsonPrimitive(guild.getId().toString()));

        // name
        guildObj.add("name", new JsonPrimitive(guild.getName()));

        // members
        JsonArray membersArr = new JsonArray();
        guild.forEachMember((member) -> membersArr.add(member.toString()));
        guildObj.add("members", membersArr);

        // claims
        JsonArray claimsArr = new JsonArray();
        guild.forEachClaim((claim) -> {
            JsonObject claimObj = new JsonObject();

            // bannerLocation
            JsonObject bannerLocationObj = new JsonObject();
            Block bannerLocation = claim.getBannerBlock();
            World world = bannerLocation.getWorld();
            bannerLocationObj.add("x", new JsonPrimitive(bannerLocation.getX()));
            bannerLocationObj.add("y", new JsonPrimitive(bannerLocation.getY()));
            bannerLocationObj.add("z", new JsonPrimitive(bannerLocation.getZ()));
            bannerLocationObj.add("world", new JsonPrimitive(world.getUID().toString()));
            claimObj.add("bannerLocation", bannerLocationObj);

            // region
            claimObj.add("region", new JsonPrimitive(claim.getRegion().getId()));

            // tier
            claimObj.add("tier", new JsonPrimitive(claim.tier().name()));

            claimsArr.add(claimObj);
        });
        guildObj.add("claims", claimsArr);

        return guildObj;
    }
}