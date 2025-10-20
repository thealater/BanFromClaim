package no.vestlandetmc.BanFromClaim.listener;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.utils.LocationFinder;
import no.vestlandetmc.BanFromClaim.utils.PlayerRidePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class RegionListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final ClaimData claimData = new ClaimData();
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();

		if (locFrom.getBlock().equals(locTo.getBlock())) {
			return;
		}

		final Player player = e.getPlayer();
		final RegionHook regionHook = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = regionHook.getRegionID(locTo);

		if (regionID == null) return;

		// Early exit: bypass players never processed further
		if (canBypass(player)) return;

		// Check ban conditions first (cheap YAML lookups)
		final boolean banAll = claimData.isAllBanned(regionID);
		boolean selfBanned = false;
		if (!banAll) {
			selfBanned = playerBanned(player, regionID);
		}
		if (!(banAll || selfBanned)) {
			return; // nothing to do if no ban condition
		}

		final boolean hasTrust = regionHook.hasTrust(player, regionID);
		if (hasTrust) return;

		// Defer expensive owner + combat checks until here
		final UUID ownerUUID = regionHook.getOwnerID(regionID);
		boolean hasAttacked = false;
		if (ownerUUID != null && CombatMode.attackerContains(player.getUniqueId())) {
			hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);
		}
		if (hasAttacked) return;

		// Passenger check only if claim has ban condition
		final Player passenger = getPassengerAbove(player);
		if (passenger != null && !canBypass(passenger) && !regionHook.hasTrust(passenger, regionID)
				&& (banAll || playerBanned(passenger, regionID) || selfBanned)) {
			passenger.teleport(player.getLocation().add(0, 4, 0));
		}

		final String regionIdFrom = regionHook.getRegionID(locFrom);
		if (regionIdFrom != null && regionIdFrom.equals(regionID)) {
			if (selfBanned || banAll) {
				final int sizeRadius = regionHook.sizeRadius(regionID);
				final Location greaterBoundaryCorner = regionHook.getGreaterBoundaryCorner(regionID);
				final Location lesserBoundaryCorner = regionHook.getLesserBoundaryCorner(regionID);

				final LocationFinder lf = new LocationFinder(greaterBoundaryCorner, lesserBoundaryCorner, player.getWorld().getUID(), sizeRadius);
				Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
					if (randomCircumferenceRadiusLoc == null) {
						if (Config.SAFE_LOCATION == null) {
							player.teleport(player.getWorld().getSpawnLocation());
						} else {
							player.teleport(Config.SAFE_LOCATION);
						}
					} else {
						player.teleport(randomCircumferenceRadiusLoc);
					}
				}));
			} else {
				final Location tpLoc = player.getLocation().add(e.getFrom().toVector().subtract(e.getTo().toVector()).normalize().multiply(3));
				if (tpLoc.getBlock().getType().equals(Material.AIR)) {
					player.teleport(tpLoc);
				} else {
					final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
					player.teleport(safeLoc);
				}

				new ParticleHandler(locTo).drawCircle(1, e.getTo().getBlockX() == e.getFrom().getBlockX());
			}
		} else {
			final Location tpLoc = player.getLocation().add(e.getFrom().toVector().subtract(e.getTo().toVector()).normalize().multiply(3));
			if (tpLoc.getBlock().getType().equals(Material.AIR)) {
				player.teleport(tpLoc);
			} else {
				final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
				player.teleport(safeLoc);
			}

			new ParticleHandler(locTo).drawCircle(1, e.getTo().getBlockX() == e.getFrom().getBlockX());
		}

		if (!MessageHandler.spamMessageClaim.contains(player.getUniqueId())) {
			MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
			MessageHandler.spamMessageClaim.add(player.getUniqueId());
			Bukkit.getScheduler().runTaskLater(BfcPlugin.getPlugin(), () -> MessageHandler.spamMessageClaim.remove(player.getUniqueId()), 5L * 20L);
		}
	}

	private Player getPassengerAbove(Player player) {
		for (Entity entity : player.getWorld().getNearbyEntities(player.getBoundingBox().expand(0.2, 4.0, 0.2))) {
			if (entity instanceof Player && !entity.getUniqueId().equals(player.getUniqueId())) {
				final Player p = (Player) entity;
				final int xTarget = p.getLocation().getBlockX();
				final int zTarget = p.getLocation().getBlockZ();
				final int yTarget = p.getLocation().getBlockY();
				final int xPlayer = player.getLocation().getBlockX();
				final int zPlayer = player.getLocation().getBlockZ();
				final int yPlayer = player.getLocation().getBlockY();
				if (xTarget == xPlayer && zTarget == zPlayer && (yTarget > yPlayer && yTarget < (yPlayer + 4))) {
					return p;
				}
			}
		}
		return null;
	}

	private boolean canBypass(Player player) {
		return player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR);
	}

	private boolean playerBanned(Player player, String claimID) {
		final ClaimData claimData = new ClaimData();
		if (!claimData.checkClaim(claimID)) return false;
		final java.util.List<String> banned = claimData.bannedPlayers(claimID);
		return banned != null && banned.contains(player.getUniqueId().toString());
	}
}