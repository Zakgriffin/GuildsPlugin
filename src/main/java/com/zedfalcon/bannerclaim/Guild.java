package com.zedfalcon.bannerclaim;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.block.Block;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Guild {
    private final UUID id;
    private String name;
    private final List<UUID> members;
    private final List<Claim> claims;

    public Guild(UUID id, String name, List<UUID> members, List<Claim> claims) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.claims = claims;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMember(UUID memberToAdd) {
        this.members.add(memberToAdd);
        markUnsaved();
    }

    public void removeMember(UUID memberToRemove) {
        this.members.remove(memberToRemove);
        markUnsaved();
    }

    public void forEachMember(Consumer<UUID> consumer) {
        this.members.forEach(consumer);
    }

    public void forEachClaim(Consumer<Claim> consumer) {
        this.claims.forEach(consumer);
    }

    public boolean isMember(UUID playerUUID) {
        return this.members.contains(playerUUID);
    }

    public void addClaim(Claim claim) {
        this.claims.add(claim);
        for (UUID playerUUID : this.members) {
            claim.getRegion().getMembers().addPlayer(playerUUID);
        }
        RegionManager regions = BannerClaim.getRegionContainer().get(BukkitAdapter.adapt(claim.getBannerBlock().getWorld()));
        assert regions != null;
        regions.addRegion(claim.getRegion());
        markUnsaved();
    }

    private void removeClaim(Claim claim) {
        RegionManager regions = BannerClaim.getRegionContainer().get(BukkitAdapter.adapt(claim.getBannerBlock().getWorld()));
        assert regions != null;
        regions.removeRegion(claim.getRegion().getId());
        this.claims.remove(claim);
        markUnsaved();
    }

    public boolean removeClaimIfBannerBrokenAt(Block block) {
        for (Claim claim : claims) {
            if (claim.getBannerBlock().equals(block)) {
                this.removeClaim(claim);
                return true;
            }
        }
        return false;
    }

    public boolean ownsRegion(ProtectedRegion region) {
        return this.claims.stream().anyMatch(c -> c.getRegion() == region);
    }

    private void markUnsaved() {
        GuildFileStorage.saveGuild(this);
    }

    public int numberOfMembers() {
        return this.members.size();
    }

    public void delete() {
        for(int i = claims.size() - 1; i >= 0; i--) {
            this.claims.get(i).getBannerBlock().breakNaturally();
        }
    }
}
