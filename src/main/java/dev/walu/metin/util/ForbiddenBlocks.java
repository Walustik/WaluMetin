package dev.walu.metin.util;

import org.bukkit.Material;

import java.util.Set;

public final class ForbiddenBlocks {

    private static final Set<String> NAMES = Set.of(
            "BEDROCK", "BARRIER", "SPAWNER", "TRIAL_SPAWNER", "VAULT", "TNT",
            "CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "BARREL", "FURNACE", "BLAST_FURNACE",
            "SMOKER", "HOPPER", "DROPPER", "DISPENSER", "CRAFTER", "BREWING_STAND", "LECTERN",
            "JUKEBOX", "DECORATED_POT", "CHISELED_BOOKSHELF", "BEACON", "END_PORTAL_FRAME",
            "END_PORTAL", "NETHER_PORTAL", "STRUCTURE_BLOCK", "JIGSAW", "LIGHT", "MOVING_PISTON",
            "PISTON_HEAD"
    );

    private ForbiddenBlocks() {}

    public static boolean isAllowed(Material material) {
        if (material == null) return false;
        if (material == Material.END_CRYSTAL) return true;
        if (!material.isBlock() || material.isAir()) return false;
        if (!material.isSolid()) return false;
        if (material.hasGravity()) return false;
        String name = material.name();
        if (NAMES.contains(name)) return false;
        return !(name.endsWith("SHULKER_BOX") || name.endsWith("_BED") || name.endsWith("_DOOR")
                || name.endsWith("COMMAND_BLOCK") || name.endsWith("_SIGN") || name.endsWith("_BANNER"));
    }
}
