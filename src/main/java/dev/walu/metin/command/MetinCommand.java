package dev.walu.metin.command;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.manager.LanguageManager;
import dev.walu.metin.manager.StoneManager;
import dev.walu.metin.model.MetinStone;
import dev.walu.metin.model.MetinType;
import dev.walu.metin.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MetinCommand implements CommandExecutor, TabCompleter {

    private final WaluMetin plugin;

    public MetinCommand(WaluMetin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LanguageManager lang = plugin.language();
        StoneManager stones = plugin.stones();

        if (args.length == 0) {
            lang.lines("usage").forEach(sender::sendMessage);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                if (!sender.hasPermission("walumetin.admin")) {
                    lang.send(sender, "no-permission");
                    return true;
                }
                plugin.reloadAll();
                lang.send(sender, "reloaded");
            }
            case "admin" -> {
                if (!(sender instanceof Player p)) {
                    lang.send(sender, "player-only");
                    return true;
                }
                if (!p.hasPermission("walumetin.admin")) {
                    lang.send(p, "no-permission");
                    return true;
                }
                plugin.adminGui().openMain(p);
            }
            case "gui" -> {
                if (!(sender instanceof Player p)) {
                    lang.send(sender, "player-only");
                    return true;
                }
                if (!p.hasPermission("walumetin.gui")) {
                    lang.send(p, "no-permission");
                    return true;
                }
                MetinStone stone = args.length >= 2 ? byId(args[1]) : lookedAt(p);
                if (stone == null) {
                    lang.send(p, "stone-not-found");
                    return true;
                }
                plugin.gui().open(p, stone);
            }
            case "olustur", "create" -> {
                if (!(sender instanceof Player p)) {
                    lang.send(sender, "player-only");
                    return true;
                }
                if (!p.hasPermission("walumetin.admin")) {
                    lang.send(p, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    lang.lines("usage").forEach(sender::sendMessage);
                    return true;
                }
                MetinType type = stones.getType(args[1]);
                if (type == null) {
                    lang.send(p, "type-not-found", "%type%", args[1]);
                    return true;
                }

                Block spot = p.getLocation().getBlock();
                if (!spot.isPassable()) spot = spot.getRelative(BlockFace.UP);
                if (!spot.isPassable()) {
                    lang.send(p, "invalid-position");
                    return true;
                }
                if (stones.getAt(spot) != null) {
                    lang.send(p, "already-stone");
                    return true;
                }

                MetinStone created = stones.create(spot, type);
                lang.send(p, "stone-created", "%stone%", type.name(), "%id%", String.valueOf(created.id()));

                if (!type.isEntity() && spot.getRelative(BlockFace.UP).isPassable() && spot.getRelative(BlockFace.UP, 2).isPassable()) {
                    Location loc = p.getLocation();
                    loc.setY(spot.getY() + 1.0);
                    p.teleport(loc);
                }
            }
            case "sil", "delete", "remove" -> {
                if (!sender.hasPermission("walumetin.admin")) {
                    lang.send(sender, "no-permission");
                    return true;
                }
                MetinStone stone = null;
                if (args.length >= 2) {
                    stone = byId(args[1]);
                } else if (sender instanceof Player p) {
                    stone = lookedAt(p);
                } else {
                    lang.lines("usage").forEach(sender::sendMessage);
                    return true;
                }
                if (stone == null) {
                    lang.send(sender, "stone-not-found");
                    return true;
                }
                int id = stone.id();
                stones.remove(stone);
                lang.send(sender, "stone-removed", "%id%", String.valueOf(id));
            }
            case "info" -> {
                sender.sendMessage(ColorUtil.color("&eWaluMetin 3.0 | Dil: " + lang.active() + " | Taş sayısı: " + stones.all().size()));
            }
            default -> lang.lines("usage").forEach(sender::sendMessage);
        }
        return true;
    }

    private MetinStone byId(String raw) {
        try {
            return plugin.stones().get(Integer.parseInt(raw.replace("#", "")));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private MetinStone lookedAt(Player p) {
        StoneManager stones = plugin.stones();
        Entity ent = p.getTargetEntity(6);
        if (ent != null) {
            MetinStone s = stones.byEntity(ent);
            if (s != null) return s;
        }
        Block target = p.getTargetBlockExact(6);
        return target == null ? null : stones.getAt(target);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("walumetin.admin")) out.addAll(List.of("reload", "admin", "olustur", "sil", "info"));
            if (sender.hasPermission("walumetin.gui")) out.add("gui");
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if ((sub.equals("olustur") || sub.equals("create")) && sender.hasPermission("walumetin.admin")) {
                plugin.stones().types().forEach(t -> out.add(t.id()));
            } else if ((sub.equals("sil") || sub.equals("delete") || sub.equals("remove")) && sender.hasPermission("walumetin.admin")) {
                plugin.stones().all().forEach(s -> out.add(String.valueOf(s.id())));
            } else if (sub.equals("gui") && sender.hasPermission("walumetin.gui")) {
                plugin.stones().all().forEach(s -> out.add(String.valueOf(s.id())));
            }
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(prefix));
        return out;
    }
}
