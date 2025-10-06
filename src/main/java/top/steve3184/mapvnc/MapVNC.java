package top.steve3184.mapvnc;

import de.pianoman911.mapengine.api.MapEngineApi;
import de.pianoman911.mapengine.api.clientside.IMapDisplay;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class MapVNC extends JavaPlugin {

    private DisplayService displayService;
    private BukkitTask proximityCheckTask;

    private final Map<Integer, MapVNCDisplay> activeDisplays = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Map<IMapDisplay, MapVNCDisplay> displayLookup = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> draggingPlayers = new ConcurrentHashMap<>();


    @Override
    public void onEnable() {
        MapEngineApi mapEngine = Bukkit.getServicesManager().load(MapEngineApi.class);
        if (mapEngine == null) {
            getLogger().severe("MapEngine API not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.displayService = new DisplayService(this, mapEngine);
        CommandHandler commandHandler = new CommandHandler(this, displayService);
        PlayerConnectionListener connectionListener = new PlayerConnectionListener(this, displayService);
        getServer().getPluginManager().registerEvents(new MapInteractionListener(this), this);

        Objects.requireNonNull(getCommand("mapvnc")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("mapvnc")).setTabCompleter(commandHandler);
        getServer().getPluginManager().registerEvents(connectionListener, this);

        this.proximityCheckTask = displayService.startProximityChecker();
        getLogger().info("MapVNC has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MapVNC... Cleaning up displays...");
        if (this.proximityCheckTask != null && !this.proximityCheckTask.isCancelled()) {
            this.proximityCheckTask.cancel();
        }
        if (displayService != null) {
            displayService.cleanupAllDisplays();
        }
        getLogger().info("MapVNC has been disabled and all displays have been cleaned up.");
    }

    public Map<Integer, MapVNCDisplay> getActiveDisplays() {
        return activeDisplays;
    }

    public AtomicInteger getNextId() {
        return nextId;
    }

    public Map<IMapDisplay, MapVNCDisplay> getDisplayLookup() {
        return displayLookup;
    }

    public Map<UUID, Integer> getDraggingPlayers() {
        return draggingPlayers;
    }
}