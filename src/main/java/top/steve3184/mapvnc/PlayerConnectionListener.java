package top.steve3184.mapvnc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    private final MapVNC plugin;
    private final DisplayService displayService;

    public PlayerConnectionListener(MapVNC plugin, DisplayService displayService) {
        this.plugin = plugin;
        this.displayService = displayService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        for (MapVNCDisplay display : plugin.getActiveDisplays().values()) {
            synchronized (display.getViewerDisplays()) {
                PlayerDisplay playerDisplay = display.getViewerDisplays().remove(playerUUID);
                if (playerDisplay != null) {
                    plugin.getDisplayLookup().remove(playerDisplay.mapDisplay());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            for (MapVNCDisplay display : plugin.getActiveDisplays().values()) {
                if (display.getLocation().getWorld().equals(player.getWorld()) &&
                        display.getLocation().distanceSquared(player.getLocation()) <= 1024) {

                    synchronized (display.getViewerDisplays()) {
                        if (!display.getViewerDisplays().containsKey(player.getUniqueId())) {
                            PlayerDisplay newPlayerDisplay = displayService.createDisplayForPlayer(player, display);
                            display.getViewerDisplays().put(player.getUniqueId(), newPlayerDisplay);
                            plugin.getDisplayLookup().put(newPlayerDisplay.mapDisplay(), display);
                        }
                    }
                }
            }
        }, 1L);
    }
}