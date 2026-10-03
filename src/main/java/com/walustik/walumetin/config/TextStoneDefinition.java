package com.walustik.walumetin.config;

import com.walustik.walumetin.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Metin taşı tanımı (record)
 * Config dosyasında tanımlanan her metin taşı türü bu sınıfın bir örneği olur.
 *
 * @param key Metin taşı türünün anahtarı (örn: "ornek_tas")
 * @param displayName Oyuncuya gösterilecek isim (renk kodları uygulanmış)
 * @param material Blok türü (örn: DIAMOND_ORE)
 * @param maxHp Başlangıç can noktaları
 * @param durationSeconds Yenileme süresi (saniye cinsinden)
 */
public record TextStoneDefinition(
        String key,
        String displayName,
        Material material,
        int maxHp,
        int durationSeconds
) {

    /**
     * Config bölümünden metin taşı tanımı oluşturur
     *
     * @param key Anahtarı
     * @param section Config bölümü
     * @return TextStoneDefinition nesnesi
     * @throws IllegalArgumentException Blok türü geçersizse
     */
    public static TextStoneDefinition fromSection(String key, ConfigurationSection section) {
        String rawName = section.getString("Name", "&7Metin Taşı");
        String typeName = section.getString("Type", "STONE");
        Material material = Material.matchMaterial(typeName);

        if (material == null) {
            throw new IllegalArgumentException(
                    "Geçersiz metin taşı türü: " + typeName + " (key: " + key + ")"
            );
        }

        int hp = Math.max(1, section.getInt("HP", 100));
        int durationSeconds = Math.max(1, section.getInt("Duration", 30));

        return new TextStoneDefinition(
                key,
                ColorUtil.colorize(rawName),
                material,
                hp,
                durationSeconds
        );
    }
}
