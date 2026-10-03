package dev.walu.metin.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.List;

public final class DecentBackend implements HologramBackend {

    @Override
    public void set(String id, Location base, List<String> lines) {
        Hologram hologram = DHAPI.getHologram(id);
        if (hologram == null) {
            DHAPI.createHologram(id, base.clone().add(0, (lines.size() - 1) * LINE, 0), false, lines);
        } else {
            DHAPI.setHologramLines(hologram, lines);
        }
    }

    @Override
    public void remove(String id) {
        Hologram hologram = DHAPI.getHologram(id);
        if (hologram != null) DHAPI.removeHologram(id);
    }
}
