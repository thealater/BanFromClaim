package no.vestlandetmc.BanFromClaim.handler;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class Permissions {

	private static final Map<String, Boolean> childrenAdmin = new HashMap<>();
	private static final Map<String, Boolean> childrenUse = new HashMap<>();

	public static final Permission BAN = new Permission("bfc.ban", "Give you access to /bfc command.", PermissionDefault.OP);
	public static final Permission UNBAN = new Permission("bfc.unban", "Give you access to /ubfc command.", PermissionDefault.OP);
	public static final Permission LIST = new Permission("bfc.list", "Give you access to /bfcl command.", PermissionDefault.OP);
	public static final Permission BYPASS = new Permission("bfc.bypass", "With bypass you can not be banned.", PermissionDefault.OP);
	public static final Permission SAFESPOT = new Permission("bfc.safespot", "Set new safespot.", PermissionDefault.OP);
	public static final Permission BANALL = new Permission("bfc.banall", "Give you access to /bfca command.", PermissionDefault.OP);
	public static final Permission KICK = new Permission("bfc.kick", "Give you access to /kfc command.", PermissionDefault.OP);

	public static final Permission USE = new Permission("bfc.use", "Give you access to user commands.", PermissionDefault.OP, childrenUse);
	public static final Permission ADMIN = new Permission("bfc.admin", "Give you access to everything.", PermissionDefault.OP, childrenAdmin);

	public static void register() {
		final PluginManager pm = Bukkit.getPluginManager();

		childrenAdmin.put("bfc.ban", true);
		childrenAdmin.put("bfc.unban", true);
		childrenAdmin.put("bfc.list", true);
		childrenAdmin.put("bfc.bypass", true);
		childrenAdmin.put("bfc.safespot", true);
		childrenAdmin.put("bfc.banall", true);
		childrenAdmin.put("bfc.kick", true);

		childrenUse.put("bfc.ban", true);
		childrenUse.put("bfc.unban", true);
		childrenUse.put("bfc.list", true);
		childrenUse.put("bfc.banall", true);
		childrenUse.put("bfc.kick", true);
		
		pm.addPermissions(List.of(
				BAN,
				UNBAN,
				LIST,
				BYPASS,
				SAFESPOT,
				BANALL,
				KICK,
				USE,
				ADMIN
		));
	}
}
