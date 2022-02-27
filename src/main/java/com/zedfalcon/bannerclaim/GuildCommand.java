package com.zedfalcon.bannerclaim;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

record PlayerGuildInvite(UUID playerId, Guild guild) {
}

public class GuildCommand implements CommandExecutor {
    private final List<PlayerGuildInvite> playerGuildInvites;
    private final List<Guild> pendingGuildDisbanding;

    public GuildCommand() {
        this.playerGuildInvites = new ArrayList<>();
        this.pendingGuildDisbanding = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length == 0) {
            Guild guild = BannerClaim.getGuildManager().guildOfPlayer(player.getUniqueId());
            if (guild == null) {
                player.sendMessage(ChatColor.RED + "You are not part of a guild");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "You have a member of " + guild.getName());
            return true;
        }

        switch (args[0]) {
            case "create" -> {
                if (BannerClaim.getGuildManager().guildOfPlayer(player.getUniqueId()) != null) {
                    player.sendMessage(ChatColor.RED + "You are already a member of a guild");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "You need to enter a guild name");
                    return true;
                }
                String guildName = args[1];
                if (BannerClaim.getGuildManager().guildWithNameExists(guildName)) {
                    player.sendMessage(ChatColor.RED + "A guild by that name already exists");
                    return true;
                }
                List<UUID> members = new ArrayList<>();
                members.add(player.getUniqueId());
                Guild guild = new Guild(UUID.randomUUID(), guildName, members, new ArrayList<>());
                BannerClaim.getGuildManager().addGuild(guild);
                player.sendMessage(ChatColor.GREEN + "Guild '" + guildName + "' created");
                return true;
            }
            case "buyClaimBanner" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify a claim banner tier from these:");
                    for (ClaimBannerTier c : ClaimBannerTier.CLAIM_BANNER_TIERS) {
                        player.sendMessage("" + c.chatColor() + c.name() + " - " +
                                c.claimWidth() + "x" + c.claimWidth() +
                                " Claim - " + c.price() + " Diamonds");
                    }
                    return true;
                }

                String tierName = args[1].strip().toLowerCase();
                Optional<ClaimBannerTier> optionClaimBannerTier = Arrays.stream(ClaimBannerTier.CLAIM_BANNER_TIERS)
                        .filter(t -> t.name().toLowerCase().equals(tierName)).findAny();

                if (optionClaimBannerTier.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "That is not a valid claim banner tier");
                    return true;
                }
                ClaimBannerTier tier = optionClaimBannerTier.get();

                if(player.getGameMode() == GameMode.CREATIVE) {
                    ItemStack claimBanner = tier.createClaimBanner();
                    player.getInventory().addItem(claimBanner);
                    player.sendMessage(tier.chatColor() + tier.name() + ChatColor.GREEN + " claim banner given");
                    return true;
                }

                if (Helpers.countItems(player, Material.DIAMOND) < tier.price()) {
                    player.sendMessage(ChatColor.RED + "You do not have " + tier.price() + " diamonds");
                    return true;
                }

                Helpers.removeItemsByCount(player, Material.DIAMOND, tier.price());

                ItemStack claimBanner = tier.createClaimBanner();
                player.getInventory().addItem(claimBanner);

                player.sendMessage(tier.chatColor() + tier.name() + ChatColor.GREEN + " claim banner purchased");
                return true;
            }
            case "leave" -> {
                Guild guild = BannerClaim.getGuildManager().guildOfPlayer(player.getUniqueId());
                if (guild == null) {
                    player.sendMessage(ChatColor.RED + "You are not part of a guild");
                    return true;
                }
                if (pendingGuildDisbanding.contains(guild)) {
                    guild.removeMember(player.getUniqueId());
                    BannerClaim.getGuildManager().removeGuild(guild);
                    player.sendMessage(ChatColor.GREEN + "" + guild.getName() + " has been disbanded. All claim banners have been broken");
                    pendingGuildDisbanding.remove(guild);
                    return true;
                }
                if (guild.numberOfMembers() == 1) {
                    player.sendMessage(ChatColor.YELLOW + "You are the only member of " + guild.getName() +
                            ". Leaving will disband the guild and destroy all claim banners. Enter '/guild leave' again to confirm");
                    pendingGuildDisbanding.add(guild);
                    Bukkit.getScheduler().runTaskLater(BannerClaim.getPlugin(),
                            () -> pendingGuildDisbanding.remove(guild), 60 * 20);
                    return true;
                }
                guild.removeMember(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have left " + guild.getName());
                return true;
            }
            case "invite" -> {
                Guild guild = BannerClaim.getGuildManager().guildOfPlayer(player.getUniqueId());
                if (guild == null) {
                    player.sendMessage(ChatColor.RED + "You are not part of a guild");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "You need to specify a player to invite");
                    return true;
                }
                Player invitedPlayer = Bukkit.getPlayer(args[1]);
                if (invitedPlayer == null) {
                    player.sendMessage(ChatColor.RED + "That player is not online right now");
                    return true;
                }
                Guild invitedPlayerExistingGuild = BannerClaim.getGuildManager().guildOfPlayer(player.getUniqueId());
                if (invitedPlayerExistingGuild != null) {
                    player.sendMessage(ChatColor.RED + "That player already part of the guild " + invitedPlayerExistingGuild.getName());
                    return true;
                }
                invitedPlayer.sendMessage(ChatColor.GREEN + "You have been invited to join " + guild.getName() + ". Enter /guild join " + guild.getName() + " to accept");
                this.playerGuildInvites.add(new PlayerGuildInvite(invitedPlayer.getUniqueId(), guild));
                return true;
            }
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "You need to specify a guild to join");
                    return true;
                }
                Guild invitedGuild = BannerClaim.getGuildManager().getGuildByName(args[1]);
                if (invitedGuild == null) {
                    player.sendMessage(ChatColor.RED + "That guild does not exist");
                    return true;
                }

                for (int i = 0; i < this.playerGuildInvites.size(); i++) {
                    PlayerGuildInvite playerGuildInvite = this.playerGuildInvites.get(i);
                    if (playerGuildInvite.playerId().equals(player.getUniqueId()) && playerGuildInvite.guild() == invitedGuild) {
                        player.sendMessage(ChatColor.GREEN + "You joined " + invitedGuild.getName());
                        invitedGuild.addMember(player.getUniqueId());
                        playerGuildInvites.remove(playerGuildInvite);
                        pendingGuildDisbanding.remove(invitedGuild);
                        return true;
                    }
                }

                player.sendMessage(ChatColor.RED + "You have not been invited to " + invitedGuild.getName());
                return true;
            }
            case "list" -> {
                BannerClaim.getGuildManager().forEachGuild((guild) -> player.sendMessage(ChatColor.GOLD + guild.getName()));
                return true;
            }
            default -> {
                player.sendMessage(ChatColor.RED + "Incorrect command usage");
                return true;
            }
        }
    }
}
