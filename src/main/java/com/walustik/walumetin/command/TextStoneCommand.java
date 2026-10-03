package com.walustik.walumetin.command;

import com.walustik.walumetin.service.TextStoneService;
import com.walustik.walumetin.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /metintas komutunu yönetir
 * Metin taşı oluşturma komutları
 */
public class TextStoneCommand implements CommandExecutor {

    private final TextStoneService service;

    public TextStoneCommand(TextStoneService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sadece oyuncular komut kullanabilir
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    ColorUtil.colorize("&cBu komut sadece oyuncular tarafından kullanılabilir.")
            );
            return true;
        }

        // Yetki kontrolü
        if (!player.hasPermission("walumetin.admin")) {
            player.sendMessage(
                    ColorUtil.colorize("&cBu komutu kullanmaya yetkiniz yok.")
            );
            return true;
        }

        // Komut sayısı kontrolü
        if (args.length < 1) {
            player.sendMessage(
                    ColorUtil.colorize("&eKullanım: /metintas olustur <tasAdi> [x y z]")
            );
            return true;
        }

        String subCommand = args[0];

        // Alt komut kontrolü
        if (!subCommand.equalsIgnoreCase("olustur")) {
            player.sendMessage(
                    ColorUtil.colorize("&eKullanım: /metintas olustur <tasAdi> [x y z]")
            );
            return true;
        }

        // Metin taşı türü kontrolü
        if (args.length < 2) {
            player.sendMessage(
                    ColorUtil.colorize("&eKullanım: /metintas olustur <tasAdi> [x y z]")
            );
            return true;
        }

        String typeKey = args[1];

        boolean created;

        // Koordinatlarla oluşturma
        if (args.length >= 5) {
            try {
                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);
                Location loc = new Location(player.getWorld(), x, y, z);
                created = service.createAtLocation(loc, typeKey);
            } catch (NumberFormatException ex) {
                player.sendMessage(
                        ColorUtil.colorize("&cKoordinatlar sayısal olmalıdır.")
                );
                return true;
            }
        }
        // Baktığı blokla oluşturma
        else {
            created = service.createFromTarget(player, typeKey);
        }

        if (created) {
            player.sendMessage(
                    ColorUtil.colorize("&aMetin taşı oluşturuldu: &e" + typeKey)
            );
        } else {
            player.sendMessage(
                    ColorUtil.colorize(
                            "&cMetin taşı oluşturulamadı. Tür adı, konum veya blok "
                                    + "kısıtlaması kontrol ediniz."
                    )
            );
        }

        return true;
    }
}
