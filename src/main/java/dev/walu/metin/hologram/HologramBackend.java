package dev.walu.metin.hologram;

import org.bukkit.Location;

import java.util.List;

public interface HologramBackend {

    double LINE = 0.25;

    void set(String id, Location base, List<String> lines);

    void remove(String id);
}
