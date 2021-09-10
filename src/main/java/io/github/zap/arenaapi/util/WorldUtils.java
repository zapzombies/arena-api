package io.github.zap.arenaapi.util;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class WorldUtils {
    public static @NotNull Block getBlockAt(@NotNull World world, @NotNull Vector vector) {
        return world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static void fillBounds(@NotNull World world, @NotNull BoundingBox bounds, @NotNull Material material) {
        int minX = NumberConversions.floor(bounds.getMinX());
        int minY = NumberConversions.floor(bounds.getMinY());
        int minZ = NumberConversions.floor(bounds.getMinZ());

        int maxX = NumberConversions.floor(bounds.getMaxX());
        int maxY = NumberConversions.floor(bounds.getMaxY());
        int maxZ = NumberConversions.floor(bounds.getMaxZ());

        for(int x = minX; x < maxX; x++) {
            for(int y = minY; y < maxY; y++) {
                for(int z = minZ; z < maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(material, false);
                }
            }
        }
    }

    public static void fillBounds(@NotNull World world, @NotNull MultiBoundingBox bounds, @NotNull Material material) {
        for(BoundingBox box : bounds) {
            fillBounds(world, box, material);
        }
    }

    public static @NotNull Location locationFrom(@NotNull World world, @NotNull Vector vector) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public static @NotNull Block blockRelative(@NotNull Block origin, @NotNull Vector offset) {
        return origin.getWorld().getBlockAt(origin.getLocation().add(offset));
    }
}
