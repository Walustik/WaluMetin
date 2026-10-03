package com.walustik.walumetin.config;

import com.walustik.walumetin.WaluMetinPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Config dosyasını yönetir ve metin taşı tanımlarını yükler
 */
public class TextStoneConfigManager {

    private final WaluMetinPlugin plugin;
    private final Map<String, TextStoneDefinition> definitions = new LinkedHashMap<>();

    public TextStoneConfigManager(WaluMetinPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Config dosyasını yeniden yükler ve metin taşı tanımlarını günceller
     */
    public void reload() {
        plugin.reloadConfig();
        definitions.clear();

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection root = config.getConfigurationSection("MetinTaslari");

        if (root == null) {
            plugin.getLogger().warning(
                    "MetinTaslari bölümü bulunamadı. config.yml kontrol ediniz."
            );
            return;
        }

        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);

            if (section == null) {
                continue;
            }

            try {
                TextStoneDefinition definition = TextStoneDefinition.fromSection(key, section);
                definitions.put(key.toLowerCase(), definition);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning(ex.getMessage());
            }
        }

        plugin.getLogger().info(
                "Yüklü metin taşı sayısı: " + definitions.size()
        );
    }

    /**
     * Anahtarına göre metin taşı tanımını döndürür
     *
     * @param key Metin taşı türü anahtarı
     * @return TextStoneDefinition veya null
     */
    public TextStoneDefinition getDefinition(String key) {
        if (key == null) {
            return null;
        }
        return definitions.get(key.toLowerCase());
    }

    /**
     * Tüm metin taşı tanımlarının bir kopyasını döndürür
     *
     * @return Tanımlar haritası (okunamaz)
     */
    public Map<String, TextStoneDefinition> getDefinitions() {
        return Map.copyOf(definitions);
    }
}
