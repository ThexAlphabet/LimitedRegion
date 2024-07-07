package dev.xalphabet.limitedregion.listeners;

import dev.xalphabet.limitedregion.LimitedRegion;
import dev.xalphabet.limitedregion.listeners.events.OnEnterRegionEvent;
import dev.xalphabet.listeners.events.OnLeaveRegionEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LimitedRegionListeners implements Listener {

    private final LimitedRegion plugin;
    private final Map<Player, Set<String>> playerRegions = new HashMap<>();
    private final Map<String, Integer> regionPlayerCount = new HashMap<>();
    private final Map<String, Integer> regionLimits = new HashMap<>();

    public LimitedRegionListeners(LimitedRegion plugin) {
        this.plugin = plugin;
        loadRegionLimits();
        initializeRegionCounts();
    }

    private void loadRegionLimits() {
        FileConfiguration config = plugin.getConfig();
        for (String regionName : config.getConfigurationSection("region_limits").getKeys(false)) {
            int limit = config.getInt("region_limits." + regionName);
            regionLimits.put(regionName, limit);
        }
    }

    private void initializeRegionCounts() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Set<String> regions = getRegionsAtLocation(player.getLocation(), player);
            for (String region : regions) {
                regionPlayerCount.put(region, regionPlayerCount.getOrDefault(region, 0) + 1);
            }
            playerRegions.put(player, regions);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Set<String> regions = getRegionsAtLocation(player.getLocation(), player);
        for (String region : regions) {
            regionPlayerCount.put(region, regionPlayerCount.getOrDefault(region, 0) + 1);
        }
        playerRegions.put(player, regions);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Set<String> regions = playerRegions.getOrDefault(player, new HashSet<>());
        for (String region : regions) {
            int currentCount = regionPlayerCount.getOrDefault(region, 0);
            if (currentCount > 0) {
                regionPlayerCount.put(region, currentCount - 1);
            }
        }
        playerRegions.remove(player);
    }

    @EventHandler
    public void onEnterRegion(OnEnterRegionEvent event) {
        Player player = event.getPlayer();
        String regionName = event.getRegionName();
        int currentCount = regionPlayerCount.getOrDefault(regionName, 0);
        int limit = regionLimits.getOrDefault(regionName, Integer.MAX_VALUE);

        regionPlayerCount.put(regionName, currentCount + 1);

        if (currentCount + 1 > limit && !player.hasPermission("limitedregion.bypass")) {

            regionPlayerCount.put(regionName, currentCount);

            Location previousLocation = player.getLocation().subtract(player.getLocation().getDirection().multiply(1.5));
            player.teleport(previousLocation);

            event.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void onLeaveRegion(OnLeaveRegionEvent event) {
        Player player = event.getPlayer();
        String regionName = event.getRegionName();
        int currentCount = regionPlayerCount.getOrDefault(regionName, 0);

        if (currentCount > 0) {
            regionPlayerCount.put(regionName, currentCount - 1);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        Set<String> fromRegions = getRegionsAtLocation(from, player);
        Set<String> toRegions = getRegionsAtLocation(to, player);

        Set<String> enteredRegions = new HashSet<>(toRegions);
        enteredRegions.removeAll(fromRegions);

        Set<String> exitedRegions = new HashSet<>(fromRegions);
        exitedRegions.removeAll(toRegions);

        for (String regionName : enteredRegions) {
            OnEnterRegionEvent enterEvent = new OnEnterRegionEvent(player, regionName);
            Bukkit.getServer().getPluginManager().callEvent(enterEvent);

            if (enterEvent.isCancelled()) {
                event.setCancelled(true);
                player.teleport(from); // Eject the player back to the previous location
                return;
            }
        }

        for (String regionName : exitedRegions) {
            OnLeaveRegionEvent leaveEvent = new OnLeaveRegionEvent(player, regionName);
            Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
        }

        // Update player's current regions
        playerRegions.put(player, toRegions);
    }

    private Set<String> getRegionsAtLocation(Location location, Player player) {
        Set<String> regions = new HashSet<>();
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(location.getWorld());

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (regionManager != null) {
            ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));

            for (ProtectedRegion region : applicableRegions) {
                regions.add(region.getId());
            }
        }

        return regions;
    }
}
