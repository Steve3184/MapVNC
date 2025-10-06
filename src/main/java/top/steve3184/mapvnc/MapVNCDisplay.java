package top.steve3184.mapvnc;

import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapVNCDisplay {

    private final int id;
    private Location location;
    private int width;
    private int height;

    private VernacularClient vncClient;
    private String address;
    private int port;
    private String password;
    private ColorDepth colorDepth = ColorDepth.BPP_24_TRUE;

    private BukkitTask renderTask;
    private final Map<UUID, PlayerDisplay> viewerDisplays = new ConcurrentHashMap<>();

    private BlockVector cornerA;
    private BlockVector cornerB;
    private BlockFace facing;

    private volatile BufferedImage sourceImage = null;
    private volatile BufferedImage lastImage = null;

    public MapVNCDisplay(int id, Location location, int width, int height) {
        this.id = id;
        this.location = location;
        this.width = width;
        this.height = height;
    }

    public void cleanup() {
        if (renderTask != null && !renderTask.isCancelled()) {
            renderTask.cancel();
        }
        if (vncClient != null && vncClient.isRunning()) {
            vncClient.stop();
        }
        viewerDisplays.forEach((uuid, playerDisplay) -> playerDisplay.destroy(Bukkit.getPlayer(uuid)));
        viewerDisplays.clear();
        vncClient = null;
        sourceImage = null;
        lastImage = null;
    }

    public int getId() { return id; }
    public Location getLocation() { return location; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public VernacularClient getVncClient() { return vncClient; }
    public String getAddress() { return address; }
    public int getPort() { return port; }
    public String getPassword() { return password; }
    public ColorDepth getColorDepth() { return colorDepth; }
    public BukkitTask getRenderTask() { return renderTask; }
    public Map<UUID, PlayerDisplay> getViewerDisplays() { return viewerDisplays; }
    public BlockVector getCornerA() { return cornerA; }
    public BlockVector getCornerB() { return cornerB; }
    public BlockFace getFacing() { return facing; }
    public BufferedImage getSourceImage() { return sourceImage; }
    public BufferedImage getLastImage() { return lastImage; }

    public void setLocation(Location location) { this.location = location; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setVncClient(VernacularClient vncClient) { this.vncClient = vncClient; }
    public void setAddress(String address) { this.address = address; }
    public void setPort(int port) { this.port = port; }
    public void setPassword(String password) { this.password = password; }
    public void setColorDepth(ColorDepth colorDepth) { this.colorDepth = colorDepth; }
    public void setRenderTask(BukkitTask renderTask) { this.renderTask = renderTask; }
    public void setCornerA(BlockVector cornerA) { this.cornerA = cornerA; }
    public void setCornerB(BlockVector cornerB) { this.cornerB = cornerB; }
    public void setFacing(BlockFace facing) { this.facing = facing; }
    public void setSourceImage(BufferedImage sourceImage) { this.sourceImage = sourceImage; }
    public void setLastImage(BufferedImage lastImage) { this.lastImage = lastImage; }
}