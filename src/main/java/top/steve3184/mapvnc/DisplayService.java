package top.steve3184.mapvnc;

import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import de.pianoman911.mapengine.api.MapEngineApi;
import de.pianoman911.mapengine.api.clientside.IMapDisplay;
import de.pianoman911.mapengine.api.drawing.IDrawingSpace;
import de.pianoman911.mapengine.api.util.Converter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DisplayService {

    private final MapVNC plugin;
    private final MapEngineApi mapEngine;
    private final Font infoFont = new Font("SansSerif", Font.PLAIN, 10);

    public DisplayService(MapVNC plugin, MapEngineApi mapEngine) {
        this.plugin = plugin;
        this.mapEngine = mapEngine;
    }

    public void createDisplay(Player creator, int id, Location location, int width, int height) {
        MapVNCDisplay display = new MapVNCDisplay(id, location, width, height);
        calculateAndStoreGeometry(creator, display);
        plugin.getActiveDisplays().put(id, display);
        PlayerDisplay creatorDisplay = createDisplayForPlayer(creator, display);
        display.getViewerDisplays().put(creator.getUniqueId(), creatorDisplay);
        plugin.getDisplayLookup().put(creatorDisplay.mapDisplay(), display);
        startRenderLoop(display);
        drawInfoScreen(display);
    }

    public boolean removeDisplay(int id) {
        MapVNCDisplay display = plugin.getActiveDisplays().remove(id);
        if (display == null) {
            return false;
        }
        synchronized (display.getViewerDisplays()) {
            display.getViewerDisplays().values().forEach(playerDisplay ->
                    plugin.getDisplayLookup().remove(playerDisplay.mapDisplay())
            );
        }
        display.cleanup();
        return true;
    }

    public void moveDisplay(Player perspectivePlayer, MapVNCDisplay display, Location newLocation) {
        synchronized (display.getViewerDisplays()) {
            cleanupPlayerVisuals(display);

            display.setLocation(newLocation);
            calculateAndStoreGeometry(perspectivePlayer, display);

            respawnVisualsForNearbyPlayers(display);
        }
    }

    public void resizeDisplay(Player perspectivePlayer, MapVNCDisplay display, int newWidth, int newHeight) {
        synchronized (display.getViewerDisplays()) {
            cleanupPlayerVisuals(display);

            display.setWidth(newWidth);
            display.setHeight(newHeight);
            calculateAndStoreGeometry(perspectivePlayer, display);

            BufferedImage source = display.getSourceImage();
            if (source != null) {
                int targetWidth = newWidth * 128;
                int targetHeight = newHeight * 128;
                BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaledImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
                g.dispose();
                display.setLastImage(scaledImage);
            }

            respawnVisualsForNearbyPlayers(display);
        }
    }

    public void connect(MapVNCDisplay display, Consumer<Exception> errorHandler) {
        if (display.getVncClient() != null && display.getVncClient().isRunning()) return;

        VernacularConfig config = new VernacularConfig();
        config.setColorDepth(display.getColorDepth());
        config.setErrorListener(e -> Bukkit.getScheduler().runTask(plugin, () -> errorHandler.accept(e)));
        if (display.getPassword() != null) {
            config.setPasswordSupplier(display::getPassword);
        }
        config.setScreenUpdateListener(image -> {
            int sourceWidth = image.getWidth(null);
            int sourceHeight = image.getHeight(null);
            if (sourceWidth <= 0 || sourceHeight <= 0) return;

            BufferedImage currentSourceImage = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D gSource = currentSourceImage.createGraphics();
            gSource.drawImage(image, 0, 0, null);
            gSource.dispose();
            display.setSourceImage(currentSourceImage);

            int targetWidth = display.getWidth() * 128;
            int targetHeight = display.getHeight() * 128;
            BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D gScaled = scaledImage.createGraphics();
            gScaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gScaled.drawImage(currentSourceImage, 0, 0, targetWidth, targetHeight, null);
            gScaled.dispose();

            display.setLastImage(scaledImage);
        });

        VernacularClient client = new VernacularClient(config);
        display.setVncClient(client);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                client.start(display.getAddress(), display.getPort());
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> errorHandler.accept(e));
            }
        });
    }

    public void disconnect(MapVNCDisplay display) {
        if (display.getVncClient() != null && display.getVncClient().isRunning()) {
            display.getVncClient().stop();
        }
        display.setVncClient(null);
        display.setSourceImage(null);
        display.setLastImage(null);
        drawInfoScreen(display);
    }

    public void drawInfoScreen(MapVNCDisplay display) {
        synchronized (display.getViewerDisplays()) {
            for (PlayerDisplay playerDisplay : display.getViewerDisplays().values()) {
                IDrawingSpace drawingSpace = playerDisplay.drawingSpace();
                if (drawingSpace != null) {
                    int width = display.getWidth() * 128;
                    int height = display.getHeight() * 128;

                    drawingSpace.rect(0, 0, width, height, new Color(45, 45, 45).getRGB());

                    String ip = display.getAddress() != null ? display.getAddress() + ":" + display.getPort() : "Not set";
                    String password = display.getPassword() != null ? "*".repeat(display.getPassword().length()) : "None";
                    String colorDepth = display.getColorDepth().toString();

                    drawingSpace.text("ID: " + display.getId(), infoFont, 5, 15, Color.WHITE.getRGB());
                    drawingSpace.text("Address: " + ip, infoFont, 5, 30, Color.WHITE.getRGB());
                    drawingSpace.text("Password: " + password, infoFont, 5, 45, Color.WHITE.getRGB());
                    drawingSpace.text("Color Depth: " + colorDepth, infoFont, 5, 60, Color.WHITE.getRGB());

                    drawingSpace.flush();
                }
            }
        }
    }

    private void cleanupPlayerVisuals(MapVNCDisplay display) {
        display.getViewerDisplays().forEach((uuid, playerDisplay) -> {
            plugin.getDisplayLookup().remove(playerDisplay.mapDisplay());
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer != null) playerDisplay.destroy(viewer);
        });
        display.getViewerDisplays().clear();
    }

    private void respawnVisualsForNearbyPlayers(MapVNCDisplay display) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(display.getLocation().getWorld()) &&
                    player.getLocation().distanceSquared(display.getLocation()) <= 1024) {

                PlayerDisplay newPlayerDisplay = createDisplayForPlayer(player, display);
                display.getViewerDisplays().put(player.getUniqueId(), newPlayerDisplay);
                plugin.getDisplayLookup().put(newPlayerDisplay.mapDisplay(), display);
            }
        }
        if (display.getVncClient() == null || !display.getVncClient().isRunning()) {
            drawInfoScreen(display);
        } else if (display.getLastImage() != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                BufferedImage image = display.getLastImage();
                if (image != null) {
                    synchronized(display.getViewerDisplays()) {
                        display.getViewerDisplays().values().forEach(pd -> {
                            pd.drawingSpace().image(image, 0, 0);
                            pd.drawingSpace().flush();
                        });
                    }
                }
            });
        }
    }

    public BukkitTask startProximityChecker() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getActiveDisplays().isEmpty()) return;

                Map<World, List<MapVNCDisplay>> displaysByWorld = plugin.getActiveDisplays().values().stream()
                        .collect(Collectors.groupingBy(display -> display.getLocation().getWorld()));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    World playerWorld = player.getWorld();
                    List<MapVNCDisplay> worldDisplays = displaysByWorld.get(playerWorld);
                    if (worldDisplays == null) continue;

                    for (MapVNCDisplay display : worldDisplays) {
                        synchronized (display.getViewerDisplays()) {
                            UUID playerUUID = player.getUniqueId();
                            boolean isViewing = display.getViewerDisplays().containsKey(playerUUID);
                            Location displayCenter = display.getLocation();

                            if (player.getLocation().distanceSquared(displayCenter) <= 1024) {
                                if (!isViewing) {
                                    PlayerDisplay newPlayerDisplay = createDisplayForPlayer(player, display);
                                    display.getViewerDisplays().put(playerUUID, newPlayerDisplay);
                                    plugin.getDisplayLookup().put(newPlayerDisplay.mapDisplay(), display);
                                    if (display.getVncClient() == null || !display.getVncClient().isRunning()) {
                                        drawInfoScreen(display);
                                    }
                                }
                            } else {
                                if (isViewing) {
                                    PlayerDisplay oldPlayerDisplay = display.getViewerDisplays().remove(playerUUID);
                                    if (oldPlayerDisplay != null) {
                                        plugin.getDisplayLookup().remove(oldPlayerDisplay.mapDisplay());
                                        oldPlayerDisplay.destroy(player);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 20L);
    }

    private void startRenderLoop(MapVNCDisplay display) {
        BukkitRunnable renderLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getActiveDisplays().get(display.getId()) == null) {
                    this.cancel();
                    return;
                }

                if (display.getVncClient() != null && display.getVncClient().isRunning()) {
                    BufferedImage image = display.getLastImage();
                    if (image != null) {
                        synchronized (display.getViewerDisplays()) {
                            for (PlayerDisplay playerDisplay : display.getViewerDisplays().values()) {
                                IDrawingSpace drawingSpace = playerDisplay.drawingSpace();
                                if (drawingSpace != null) {
                                    drawingSpace.image(image, 0, 0);
                                    drawingSpace.flush();
                                }
                            }
                        }
                    }
                }
            }
        };
        display.setRenderTask(renderLoop.runTaskTimerAsynchronously(plugin, 0L, 3L));
    }

    public void cleanupAllDisplays() {
        plugin.getActiveDisplays().values().forEach(MapVNCDisplay::cleanup);
        plugin.getActiveDisplays().clear();
        plugin.getDisplayLookup().clear();
    }

    public void calculateAndStoreGeometry(Player viewer, MapVNCDisplay displayInfo) {
        Vector direction = viewer.getLocation().getDirection().setY(0).normalize();
        Vector rightVec = direction.getCrossProduct(new Vector(0, 1, 0)).normalize();
        BlockVector rightStep = new BlockVector(Math.round(rightVec.getX()), 0, Math.round(rightVec.getZ()));
        BlockVector downStep = new BlockVector(0, -1, 0);
        BlockVector cornerA = displayInfo.getLocation().toVector().toBlockVector();
        Vector offset = rightStep.clone().multiply(displayInfo.getWidth() - 1)
                .add(downStep.clone().multiply(displayInfo.getHeight() - 1));
        BlockVector cornerB = cornerA.clone().add(offset).toBlockVector();
        displayInfo.setCornerA(cornerA);
        displayInfo.setCornerB(cornerB);
        displayInfo.setFacing(viewer.getFacing().getOppositeFace());
    }

    public PlayerDisplay createDisplayForPlayer(Player player, MapVNCDisplay sourceDisplay) {
        IMapDisplay display = mapEngine.displayProvider().createBasic(sourceDisplay.getCornerA(), sourceDisplay.getCornerB(), sourceDisplay.getFacing());
        IDrawingSpace drawingSpace = mapEngine.pipeline().createDrawingSpace(display);
        drawingSpace.ctx().receivers().add(player);
        drawingSpace.ctx().buffering(true);
        drawingSpace.ctx().converter(Converter.FLOYD_STEINBERG);

        display.spawn(player, 0);
        return new PlayerDisplay(display, drawingSpace);
    }
}