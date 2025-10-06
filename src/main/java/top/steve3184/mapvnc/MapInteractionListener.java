package top.steve3184.mapvnc;

import com.shinyhut.vernacular.client.VernacularClient;
import de.pianoman911.mapengine.api.clientside.IMapDisplay;
import de.pianoman911.mapengine.api.event.MapClickEvent;
import de.pianoman911.mapengine.api.util.MapClickType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;

public class MapInteractionListener implements Listener {

    private final MapVNC plugin;

    public MapInteractionListener(MapVNC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMapClick(MapClickEvent event) {
        IMapDisplay clickedDisplay = event.display();
        MapVNCDisplay vncDisplay = plugin.getDisplayLookup().get(clickedDisplay);
        Player player = event.player();

        if (vncDisplay == null || vncDisplay.getVncClient() == null || !vncDisplay.getVncClient().isRunning()) {
            return;
        }

        VernacularClient client = vncDisplay.getVncClient();
        BufferedImage sourceImage = vncDisplay.getSourceImage();

        if (sourceImage == null) {
            return;
        }
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();

        double mapPixelWidth = vncDisplay.getWidth() * 128.0;
        double mapPixelHeight = vncDisplay.getHeight() * 128.0;
        int vncX = (int) Math.floor((event.x() / mapPixelWidth) * sourceWidth);
        int vncY = (int) Math.floor((event.y() / mapPixelHeight) * sourceHeight);
        int button = (event.clickType() == MapClickType.RIGHT_CLICK) ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1;

        Map<UUID, Integer> draggingPlayers = plugin.getDraggingPlayers();
        UUID playerUUID = player.getUniqueId();

        if (player.isSneaking() && button == MouseEvent.BUTTON1) {
            client.moveMouse(vncX, vncY);
            if (!draggingPlayers.containsKey(playerUUID)) {
                draggingPlayers.put(playerUUID, vncDisplay.getId());
                client.updateMouseButton(MouseEvent.BUTTON1, true);
                player.sendActionBar(Component.text("DRAGGING: Release sneak to drop.", NamedTextColor.YELLOW));
            } else {
                player.sendActionBar(Component.text("DRAGGING: Release sneak to drop.", NamedTextColor.YELLOW));
            }
        } else {
            client.moveMouse(vncX, vncY);
            client.click(button);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();
        Map<UUID, Integer> draggingPlayers = plugin.getDraggingPlayers();
        UUID playerUUID = player.getUniqueId();

        if (draggingPlayers.containsKey(playerUUID)) {
            int displayId = draggingPlayers.remove(playerUUID);
            MapVNCDisplay vncDisplay = plugin.getActiveDisplays().get(displayId);

            if (vncDisplay != null && vncDisplay.getVncClient() != null && vncDisplay.getVncClient().isRunning()) {
                vncDisplay.getVncClient().updateMouseButton(MouseEvent.BUTTON1, false);
                player.sendActionBar(Component.text("Drag released.", NamedTextColor.GREEN));
            }
        }
    }
}