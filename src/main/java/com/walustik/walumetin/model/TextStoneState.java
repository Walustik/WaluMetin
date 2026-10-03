package com.walustik.walumetin.model;

import com.walustik.walumetin.config.TextStoneDefinition;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

/**
 * Metin taşının o anki durumunu tutar
 * HP, yenileme durumu, hologram, vb.
 */
public class TextStoneState {

    private final Location location;
    private final TextStoneDefinition definition;
    private int hp;
    private boolean respawning;
    private long respawnAtMillis;

    private ArmorStand hologram;

    public TextStoneState(Location location, TextStoneDefinition definition) {
        this.location = location.clone();
        this.definition = definition;
        this.hp = definition.maxHp();
    }

    public Location getLocation() {
        return location.clone();
    }

    public TextStoneDefinition getDefinition() {
        return definition;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public boolean isRespawning() {
        return respawning;
    }

    public void setRespawning(boolean respawning) {
        this.respawning = respawning;
    }

    public long getRespawnAtMillis() {
        return respawnAtMillis;
    }

    public void setRespawnAtMillis(long respawnAtMillis) {
        this.respawnAtMillis = respawnAtMillis;
    }

    public ArmorStand getHologram() {
        return hologram;
    }

    public void setHologram(ArmorStand hologram) {
        this.hologram = hologram;
    }

    public int getMaxHp() {
        return definition.maxHp();
    }

    /**
     * Metin taşına hasar verir
     *
     * @param amount Hasar miktarı
     */
    public void damage(int amount) {
        this.hp = Math.max(0, this.hp - amount);
    }

    /**
     * Metin taşının kırıldığını kontrol eder
     *
     * @return Kırıldıysa true
     */
    public boolean isDestroyed() {
        return hp <= 0;
    }

    /**
     * Konum bazlı bir anahtar oluşturur (harita için)
     *
     * @return Konum anahtarı
     */
    public String locationKey() {
        Location loc = getLocation();
        return loc.getWorld().getName()
                + ":" + loc.getBlockX()
                + ":" + loc.getBlockY()
                + ":" + loc.getBlockZ();
    }
}
