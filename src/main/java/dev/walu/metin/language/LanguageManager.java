package dev.walu.metin.language;

import dev.walu.metin.WaluMetin;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LanguageManager {

    private static final List<String> SUPPORTED = List.of("tr", "en", "de", "es", "fr", "ar");

    private final WaluMetin plugin;
    private YamlConfiguration messages = new YamlConfiguration();
    private YamlConfiguration fallback = new YamlConfiguration();
    private String active = "tr";

    public LanguageManager(WaluMetin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File dir = new File(plugin.getDataFolder(), "lang");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("lang/ klasörü oluşturulamadı.");
        }

        for (String code : SUPPORTED) {
            File file = new File(dir, code + ".yml");
            if (!file.exists()) {
                plugin.saveResource("lang/" + code + ".yml", false);
            }
        }

        String wanted = plugin.getConfig().getString("Language", "tr").toLowerCase(Locale.ROOT);
        if (!SUPPORTED.contains(wanted)) {
            wanted = "en";
        }
        active = wanted;

        messages = YamlConfiguration.loadConfiguration(new File(dir, active + ".yml"));
        fallback = YamlConfiguration.loadConfiguration(new File(dir, "en.yml"));
    }

    public String active() { return active; }

    public String raw(String key) {
        String value = messages.getString(key);
        if (value == null) value = fallback.getString(key);
        return value == null ? key : value;
    }

    public String msg(String key, String... replacements) {
        String value = raw(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            value = value.replace(replacements[i], replacements[i + 1]);
        }
        return dev.walu.metin.util.ColorUtil.color(value);
    }

    public List<String> lines(String key, String... replacements) {
        List<String> raw = messages.getStringList(key);
        if (raw.isEmpty()) raw = fallback.getStringList(key);
        List<String> out = new ArrayList<>();
        for (String value : raw) {
            String rendered = value;
            for (int i = 0; i + 1 < replacements.length; i += 2) rendered = rendered.replace(replacements[i], replacements[i + 1]);
            out.add(dev.walu.metin.util.ColorUtil.color(rendered));
        }
        return out;
    }

    public void send(CommandSender sender, String key, String... replacements) {
        sender.sendMessage(msg(key, replacements));
    }

    public String stateName(dev.walu.metin.model.MetinStone.State state) {
        return msg("state-" + state.name().toLowerCase(Locale.ROOT));
    }
}
