package codes.biscuit.chunkbuster.hooks;

import codes.biscuit.chunkbuster.ChunkBuster;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.EnumMap;
import java.util.Map;

public class HookUtils {

    private Map<HookType, Object> enabledHooks = new EnumMap<>(HookType.class);
    private ChunkBuster main;


    /**
     * This will automatically create all the hooks and add all the enabled hooks
     * to the map above.
     */
    public HookUtils(ChunkBuster main) {
        this.main = main;
        PluginManager pm = main.getServer().getPluginManager();
        if (pm.getPlugin("MassiveCore") != null &&
                pm.getPlugin("Factions") != null && // Many people have Massivecore on their server when they don't need it, so check dependencies.
                pm.getPlugin("Factions").getDescription().getDepend().contains("MassiveCore")) {
            main.getLogger().info("Hooked into MassiveCore Factions");
            enabledHooks.put(HookType.MCOREFACTIONS, new MCoreFactionsHook(main));
        } else if (pm.getPlugin("Factions") != null) {
            main.getLogger().info("Hooked into FactionsUUID/SavageFactions");
            enabledHooks.put(HookType.FACTIONSUUID, new FactionsUUIDHook(main));
            try {
                Class.forName("com.massivecraft.factions.perms.Role"); //old is com.massivecraft.factions.struct.Role
                main.getLogger().info("Hooked into the new FactionsUUID/SavageFactions role system.");
                enabledHooks.put(HookType.FACTIONSUUID_ROLE, new FactionsUUID_New());
            } catch (ClassNotFoundException e) {
                System.out.println(e);
            }
        }
        if (pm.getPlugin("WorldGuard") != null) {
            String pluginVersion = main.getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
             if (pluginVersion.startsWith("6")) {
                enabledHooks.put(HookType.WORLDGUARD, new WorldGuard_6());
                main.getLogger().info("Hooked into WorldGuard 6");
            }
        }
    }

    /**
     * Check if a player has a faction if the factions hook is enabled
     */
    public boolean hasFaction(Player p) {
        if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.MCOREFACTIONS)) {
            MCoreFactionsHook mCoreFactionsHook = (MCoreFactionsHook)enabledHooks.get(HookType.MCOREFACTIONS);
            return mCoreFactionsHook.hasFaction(p);
        } else if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.FACTIONSUUID)) {
            FactionsUUIDHook factionsUUIDHook = (FactionsUUIDHook)enabledHooks.get(HookType.FACTIONSUUID);
            return factionsUUIDHook.hasFaction(p);
        } else {
            return true;
        }
    }

    /**
     * Check if a location is wilderness if the factions hook is enabled
     */
    public boolean isWilderness(Location loc) {
        if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.MCOREFACTIONS)) {
            MCoreFactionsHook mCoreFactionsHook = (MCoreFactionsHook)enabledHooks.get(HookType.MCOREFACTIONS);
            return mCoreFactionsHook.isWilderness(loc);
        } else if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.FACTIONSUUID)) {
            FactionsUUIDHook factionsUUIDHook = (FactionsUUIDHook)enabledHooks.get(HookType.FACTIONSUUID);
            return factionsUUIDHook.isWilderness(loc);
        } else {
            return false;
        }
    }

    /**
     * Whether the player can place here, checking all appropriate hooks
     */
    public boolean compareLocToPlayer(Location loc, Player p) {
        boolean canBuild = true;
        if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.MCOREFACTIONS)) {
            MCoreFactionsHook mCoreFactionsHook = (MCoreFactionsHook)enabledHooks.get(HookType.MCOREFACTIONS);
            if (!mCoreFactionsHook.compareLocPlayerFaction(loc, p)) canBuild = false;
        } else if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.FACTIONSUUID)) {
            FactionsUUIDHook factionsUUIDHook = (FactionsUUIDHook)enabledHooks.get(HookType.FACTIONSUUID);
            if (!factionsUUIDHook.compareLocPlayerFaction(loc, p)) canBuild = false;
        }
        if (main.getConfigValues().worldguardHookEnabled() && enabledHooks.containsKey(HookType.WORLDGUARD)) {
            IWorldGuardHook worldGuardHook = (IWorldGuardHook)enabledHooks.get(HookType.WORLDGUARD);
            if (!worldGuardHook.checkLocationBreakFlag(loc.getChunk(), p)) canBuild = false;
        }
        return canBuild;
    }

    /**
     * Check if a player has the minimum role to place chunkbusters if the factions hook is enabled
     */
    public boolean checkRole(Player p) {
        if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.MCOREFACTIONS)) {
            MCoreFactionsHook mCoreFactionsHook = (MCoreFactionsHook)enabledHooks.get(HookType.MCOREFACTIONS);
            return mCoreFactionsHook.checkRole(p, main.getConfigValues().getMinimumRole());
        } else if (main.getConfigValues().factionsHookEnabled() && enabledHooks.containsKey(HookType.FACTIONSUUID_ROLE)) {
            IFactionsUUIDHook factionsUUIDHook = (IFactionsUUIDHook)enabledHooks.get(HookType.FACTIONSUUID_ROLE);
            return factionsUUIDHook.checkRole(p, main.getConfigValues().getMinimumRole());
        } else {
            return true;
        }
    }

    /**
     * Log a block as removed in CoreProtect
     */

    public enum HookType {
        MCOREFACTIONS,
        FACTIONSUUID,
        FACTIONSUUID_ROLE,
        WORLDGUARD,
        COREPROTECT,
        TOWNY
    }

}
