package no.vestlandetmc.BanFromClaim.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerRidePlayer {

	public static Player getPassenger(Player player) {
		for (Entity entity : player.getWorld().getNearbyEntities(player.getBoundingBox().expand(0.2, 4.0, 0.2))) {
			if (entity instanceof Player && !entity.getUniqueId().equals(player.getUniqueId())) {
				final Player target = (Player) entity;
				final int xTarget = target.getLocation().getBlockX();
				final int zTarget = target.getLocation().getBlockZ();
				final int yTarget = target.getLocation().getBlockY();
				final int xPlayer = player.getLocation().getBlockX();
				final int zPlayer = player.getLocation().getBlockZ();
				final int yPlayer = player.getLocation().getBlockY();
				if (xTarget == xPlayer && zTarget == zPlayer && (yTarget > yPlayer && yTarget < (yPlayer + 4))) {
					return target;
				}
			}
		}

		return null;
	}
}
