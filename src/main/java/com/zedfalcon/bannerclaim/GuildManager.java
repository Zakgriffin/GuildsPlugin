package com.zedfalcon.bannerclaim;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class GuildManager {
    private final List<Guild> guilds;

    public GuildManager() {
        this.guilds = new ArrayList<>();
    }

    public void forEachGuild(Consumer<Guild> consumer) {this.guilds.forEach(consumer);}

    public void addGuild(Guild guildToAdd) {
        this.guilds.add(guildToAdd);
        GuildFileStorage.saveGuild(guildToAdd);
    }
    public void removeGuild(Guild guildToRemove) {
        guildToRemove.delete();
        this.guilds.remove(guildToRemove);
        GuildFileStorage.deleteGuild(guildToRemove);
    }

    public Guild guildOfPlayer(UUID playerUUID) {
        for (Guild guild : guilds) {
            if (guild.isMember(playerUUID)) return guild;
        }
        return null;
    }

    public Guild getGuildByName(String guildName) {
        return this.guilds.stream().filter((g) -> g.getName().equals(guildName)).findAny().orElse(null);
    }

    public boolean guildWithNameExists(String guildName) {
        return getGuildByName(guildName) != null;
    }

    public GuildClaimPair guildAndClaimFromRegion(ProtectedRegion region) {
        for(Guild guild : this.guilds) {
            Claim claim = guild.claimFromRegion(region);
            if(claim != null) {
                return new GuildClaimPair(guild, claim);
            }
        }
        return null;
    }
}
