package top.steve3184.mapvnc;

import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final MapVNC plugin;
    private final DisplayService displayService;

    public CommandHandler(MapVNC plugin, DisplayService displayService) {
        this.plugin = plugin;
        this.displayService = displayService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("create") && !(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by a player.", NamedTextColor.RED));
            return true;
        }

        switch (subCommand) {
            case "create" -> handleCreateCommand((Player) sender, args);
            case "remove" -> handleRemoveCommand(sender, args);
            case "list" -> handleListCommand(sender);
            case "set" -> handleSetCommand(sender, args);
            case "connect" -> handleConnectCommand(sender, args);
            case "disconnect" -> handleDisconnectCommand(sender, args);
            case "input" -> handleInputCommand(sender, args);
            case "key" -> handleKeyCommand(sender, args);
            default -> sendHelpMessage(sender);
        }
        return true;
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("mapvnc.command.create")) {
            player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (args.length < 6) {
            player.sendMessage(Component.text("Usage: /mvnc create <x> <y> <z> <width> <height>", NamedTextColor.RED));
            return;
        }

        try {
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);
            int width = Integer.parseInt(args[4]);
            int height = Integer.parseInt(args[5]);
            if (width <= 0 || height <= 0) {
                player.sendMessage(Component.text("Width and height must be positive.", NamedTextColor.RED));
                return;
            }

            int id = plugin.getNextId().getAndIncrement();
            Location location = new Location(player.getWorld(), x, y, z);
            displayService.createDisplay(player, id, location, width, height);
            player.sendMessage(Component.text("Successfully created VNC display #", NamedTextColor.GREEN)
                    .append(Component.text(id, NamedTextColor.WHITE)));

        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid number format for coordinates, width, or height.", NamedTextColor.RED));
        }
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mapvnc.command.remove")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mvnc remove <id>", NamedTextColor.RED));
            return;
        }
        MapVNCDisplay display = getTargetDisplay(args[1], sender);
        if (display == null) return;

        if (displayService.removeDisplay(display.getId())) {
            sender.sendMessage(Component.text("Successfully removed VNC display #" + display.getId() + ".", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("VNC display with ID #" + display.getId() + " not found.", NamedTextColor.RED));
        }
    }

    private void handleListCommand(CommandSender sender) {
        if (!sender.hasPermission("mapvnc.command.list")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (plugin.getActiveDisplays().isEmpty()) {
            sender.sendMessage(Component.text("No active VNC displays found.", NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text("--- Active VNC Displays ---", NamedTextColor.GOLD));
        plugin.getActiveDisplays().forEach((id, display) -> {
            Location loc = display.getLocation();
            String status = display.getVncClient() != null && display.getVncClient().isRunning() ? "CONNECTED" : "DISCONNECTED";
            sender.sendMessage(
                    Component.text("#" + id + ": ", NamedTextColor.YELLOW)
                            .append(Component.text(String.format("%s:%d", display.getAddress(), display.getPort()), NamedTextColor.WHITE))
                            .append(Component.text(" [" + status + "]", "CONNECTED".equals(status) ? NamedTextColor.GREEN : NamedTextColor.RED))
                            .append(Component.text(String.format("\n    at (%d, %d, %d) Size: %dx%d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), display.getWidth(), display.getHeight()), NamedTextColor.GRAY))
            );
        });
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mapvnc.command.set")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sendHelpMessage(sender);
            return;
        }

        MapVNCDisplay display = getTargetDisplay(args[1], sender);
        if (display == null) return;

        String property = args[2].toLowerCase();
        switch (property) {
            case "address":
                if (args.length != 5) {
                    sender.sendMessage(Component.text("Usage: /mvnc set <id> address <ip> <port>", NamedTextColor.RED));
                    return;
                }
                try {
                    String ip = args[3];
                    int port = Integer.parseInt(args[4]);
                    display.setAddress(ip);
                    display.setPort(port);
                    sender.sendMessage(Component.text("Set address for display #" + display.getId() + " to " + ip + ":" + port, NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Invalid port number.", NamedTextColor.RED));
                }
                break;
            case "password":
                String password = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : null;
                display.setPassword(password);
                if (password != null) {
                    sender.sendMessage(Component.text("Password set for display #" + display.getId(), NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Password cleared for display #" + display.getId(), NamedTextColor.GREEN));
                }
                break;
            case "colordepth":
                if (args.length != 4) {
                    sender.sendMessage(Component.text("Usage: /mvnc set <id> colordepth <8|16|24>", NamedTextColor.RED));
                    return;
                }
                ColorDepth depth;
                switch(args[3]) {
                    case "8": depth = ColorDepth.BPP_8_INDEXED; break;
                    case "16": depth = ColorDepth.BPP_16_TRUE; break;
                    case "24": depth = ColorDepth.BPP_24_TRUE; break;
                    default:
                        sender.sendMessage(Component.text("Invalid color depth. Use 8, 16, or 24.", NamedTextColor.RED));
                        return;
                }
                display.setColorDepth(depth);
                sender.sendMessage(Component.text("Set color depth for display #" + display.getId() + " to " + args[3] + "-bit", NamedTextColor.GREEN));
                break;
            case "pos":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("You must be a player to set position based on perspective.", NamedTextColor.RED));
                    return;
                }
                if (args.length != 6) {
                    sender.sendMessage(Component.text("Usage: /mvnc set <id> pos <x> <y> <z>", NamedTextColor.RED));
                    return;
                }
                try {
                    double x = Double.parseDouble(args[3]);
                    double y = Double.parseDouble(args[4]);
                    double z = Double.parseDouble(args[5]);
                    Location newLocation = new Location(((Player) sender).getWorld(), x, y, z);
                    displayService.moveDisplay((Player) sender, display, newLocation);
                    sender.sendMessage(Component.text("Moved display #" + display.getId() + " successfully.", NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Invalid coordinates provided.", NamedTextColor.RED));
                }
                break;
            case "size":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("You must be a player to set size based on perspective.", NamedTextColor.RED));
                    return;
                }
                if (args.length != 5) {
                    sender.sendMessage(Component.text("Usage: /mvnc set <id> size <width> <height>", NamedTextColor.RED));
                    return;
                }
                try {
                    int newWidth = Integer.parseInt(args[3]);
                    int newHeight = Integer.parseInt(args[4]);
                    if (newWidth <= 0 || newHeight <= 0) {
                        sender.sendMessage(Component.text("Width and height must be positive.", NamedTextColor.RED));
                        return;
                    }
                    displayService.resizeDisplay((Player) sender, display, newWidth, newHeight);
                    sender.sendMessage(Component.text("Resized display #" + display.getId() + " successfully.", NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Invalid width or height.", NamedTextColor.RED));
                }
                break;
            default:
                sender.sendMessage(Component.text("Unknown property. Use: address, password, colordepth, pos, size.", NamedTextColor.RED));
                break;
        }
        displayService.drawInfoScreen(display);
    }

    private void handleConnectCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mapvnc.command.connect")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mvnc connect <id>", NamedTextColor.RED));
            return;
        }
        MapVNCDisplay display = getTargetDisplay(args[1], sender);
        if (display == null) return;

        if (display.getAddress() == null || display.getPort() == 0) {
            sender.sendMessage(Component.text("Address and port must be set before connecting.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Connecting display #" + display.getId() + "...", NamedTextColor.YELLOW));
        displayService.connect(display, (error) -> {
            sender.sendMessage(Component.text("Failed to connect display #" + display.getId() + ": " + error.getMessage(), NamedTextColor.RED));
        });
    }

    private void handleDisconnectCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mapvnc.command.disconnect")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mvnc disconnect <id>", NamedTextColor.RED));
            return;
        }
        MapVNCDisplay display = getTargetDisplay(args[1], sender);
        if (display == null) return;

        displayService.disconnect(display);
        sender.sendMessage(Component.text("Disconnected display #" + display.getId(), NamedTextColor.GREEN));
    }
    private void handleInputCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mapvnc.command.input")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /mvnc input <id> <text...>", NamedTextColor.RED));
            return;
        }
        MapVNCDisplay display = getTargetDisplay(args[1], sender);
        if (display == null) return;
        VernacularClient client = display.getVncClient();
        if (client == null || !client.isRunning()) {
            sender.sendMessage(Component.text("Display #" + display.getId() + " is not connected.", NamedTextColor.RED));
            return;
        }
        String textToInput = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        client.type(textToInput);
        sender.sendMessage(Component.text("Sent input to display #" + display.getId(), NamedTextColor.GREEN));
    }

    private void handleKeyCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mapvnc.command.key")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(Component.text("Usage: /mvnc key <id> <keyName>", NamedTextColor.RED));
            return;
        }
        MapVNCDisplay display = getTargetDisplay(args[1], sender);
        if (display == null) return;
        VernacularClient client = display.getVncClient();
        if (client == null || !client.isRunning()) {
            sender.sendMessage(Component.text("Display #" + display.getId() + " is not connected.", NamedTextColor.RED));
            return;
        }
        Integer keyCode = VNCKeyMap.getKeyCode(args[2]);
        if (keyCode == null) {
            sender.sendMessage(Component.text("Unknown key: " + args[2], NamedTextColor.RED));
            return;
        }
        client.updateKey(keyCode, true);
        client.updateKey(keyCode, false);
        sender.sendMessage(Component.text("Sent key '" + args[2].toUpperCase() + "' to display #" + display.getId(), NamedTextColor.GREEN));
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("--- MapVNC Help ---", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/mvnc create <x> <y> <z> <w> <h>", NamedTextColor.AQUA).append(Component.text(" - Create a VNC display.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/mvnc remove <id|-1>", NamedTextColor.AQUA).append(Component.text(" - Remove a display (-1 for nearest).", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/mvnc list", NamedTextColor.AQUA).append(Component.text(" - List all displays.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/mvnc set <id|-1> <prop> [val...]", NamedTextColor.AQUA).append(Component.text(" - Set properties (-1 for nearest).", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/mvnc input <id|-1> <text...>", NamedTextColor.AQUA).append(Component.text(" - Type text into a display (-1 for nearest).", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/mvnc key <id|-1> <key>", NamedTextColor.AQUA).append(Component.text(" - Press a key on a display (-1 for nearest).", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/mvnc connect <id|-1>", NamedTextColor.AQUA).append(Component.text(" - Connect to the VNC server (-1 for nearest).", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/mvnc disconnect <id|-1>", NamedTextColor.AQUA).append(Component.text(" - Disconnect from the server (-1 for nearest).", NamedTextColor.WHITE)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0],
                    Arrays.asList("create", "remove", "list", "set", "connect", "disconnect", "input", "key"), completions);
        }

        String subCommand = args[0].toLowerCase();
        if (args.length == 2) {
            if (Arrays.asList("remove", "set", "connect", "disconnect", "input", "key").contains(subCommand)) {
                List<String> ids = plugin.getActiveDisplays().keySet().stream().map(String::valueOf).collect(Collectors.toList());
                ids.add("-1");
                return StringUtil.copyPartialMatches(args[1], ids, completions);
            }
        }

        Block targetBlock = player.getTargetBlockExact(10);

        if ("create".equals(subCommand)) {
            if (targetBlock != null) {
                if (args.length == 2) completions.add(String.valueOf(targetBlock.getX()));
                if (args.length == 3) completions.add(String.valueOf(targetBlock.getY()));
                if (args.length == 4) completions.add(String.valueOf(targetBlock.getZ()));
            }
            if (args.length == 5) completions.add("<width>");
            if (args.length == 6) completions.add("<height>");
            return completions;
        }

        if ("set".equals(subCommand)) {
            if (args.length == 3) {
                return StringUtil.copyPartialMatches(args[2], Arrays.asList("address", "password", "colordepth", "pos", "size"), completions);
            }
            String property = args[2].toLowerCase();
            switch (property) {
                case "address":
                    if (args.length == 4) completions.add("<ip>");
                    if (args.length == 5) completions.add("<port>");
                    break;
                case "password":
                    if (args.length == 4) completions.add("[password]");
                    break;
                case "pos":
                    if (targetBlock != null) {
                        if (args.length == 4) completions.add(String.valueOf(targetBlock.getX()));
                        if (args.length == 5) completions.add(String.valueOf(targetBlock.getY()));
                        if (args.length == 6) completions.add(String.valueOf(targetBlock.getZ()));
                    }
                    break;
                case "size":
                    if (args.length == 4) completions.add("<width>");
                    if (args.length == 5) completions.add("<height>");
                    break;
                case "colordepth":
                    if (args.length == 4) return StringUtil.copyPartialMatches(args[3], Arrays.asList("8", "16", "24"), completions);
                    break;
            }
            return completions;
        }

        if ("key".equals(subCommand) && args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], VNCKeyMap.getKeyNames(), completions);
        }

        return Collections.emptyList();
    }

    private MapVNCDisplay getTargetDisplay(String idString, CommandSender sender) {
        if ("-1".equals(idString)) {
            Location executionLocation = null;
            if (sender instanceof Player player) {
                executionLocation = player.getLocation();
            } else if (sender instanceof BlockCommandSender blockSender) {
                executionLocation = blockSender.getBlock().getLocation();
            }

            if (executionLocation == null) {
                sender.sendMessage(Component.text("Cannot use 'nearest' (-1) from the console as there is no location.", NamedTextColor.RED));
                return null;
            }

            MapVNCDisplay nearestDisplay = null;
            double minDistanceSq = Double.MAX_VALUE;
            final double maxDistance = 16.0;
            final double maxDistanceSq = maxDistance * maxDistance;

            for (MapVNCDisplay display : plugin.getActiveDisplays().values()) {
                if (display.getLocation().getWorld() == null || !display.getLocation().getWorld().equals(executionLocation.getWorld())) {
                    continue;
                }
                double distanceSq = display.getLocation().distanceSquared(executionLocation);
                if (distanceSq <= maxDistanceSq && distanceSq < minDistanceSq) {
                    minDistanceSq = distanceSq;
                    nearestDisplay = display;
                }
            }

            if (nearestDisplay == null) {
                sender.sendMessage(Component.text("No VNC display found within 16 blocks.", NamedTextColor.RED));
                return null;
            }

            sender.sendMessage(Component.text("Operating on nearest display: #" + nearestDisplay.getId(), NamedTextColor.GRAY));
            return nearestDisplay;
        }

        try {
            int id = Integer.parseInt(idString);
            MapVNCDisplay display = plugin.getActiveDisplays().get(id);
            if (display == null) {
                sender.sendMessage(Component.text("Map display with ID #" + id + " not found.", NamedTextColor.RED));
                return null;
            }
            return display;
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid ID format. It must be a number.", NamedTextColor.RED));
            return null;
        }
    }
}