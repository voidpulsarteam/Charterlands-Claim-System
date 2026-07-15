package dev.voidpulsar.lc_claim_economy.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ChunkPosKey {
    private static final String SEPARATOR = "#";

    private ChunkPosKey() {
    }

    /**
     * Best-effort encoder for foreign chunk position objects (e.g. FTB
     * ChunkDimPos) without hard-linking to their classes.
     */
    public static String encode(Object foreignPos) {
        if (foreignPos == null) {
            throw new IllegalArgumentException("Chunk position object is null");
        }

        try {
            Object dimensionObj = foreignPos.getClass().getMethod("dimension").invoke(foreignPos);
            Object locationObj = dimensionObj.getClass().getMethod("location").invoke(dimensionObj);
            int x = ((Number) foreignPos.getClass().getMethod("x").invoke(foreignPos)).intValue();
            int z = ((Number) foreignPos.getClass().getMethod("z").invoke(foreignPos)).intValue();
            return encode((ResourceLocation) locationObj, x, z);
        } catch (ReflectiveOperationException | ClassCastException error) {
            throw new IllegalArgumentException("Unsupported chunk position object: " + foreignPos.getClass().getName(), error);
        }
    }

    public static String encode(ResourceLocation dimension, int x, int z) {
        return dimension + SEPARATOR + x + SEPARATOR + z;
    }

    public static ResourceLocation dimension(String key) {
        return ResourceLocation.parse(key.substring(0, key.indexOf(SEPARATOR)));
    }

    public static int x(String key) {
        int first = key.indexOf(SEPARATOR);
        int second = key.indexOf(SEPARATOR, first + 1);
        return Integer.parseInt(key.substring(first + 1, second));
    }

    public static int z(String key) {
        int second = key.indexOf(SEPARATOR, key.indexOf(SEPARATOR) + 1);
        return Integer.parseInt(key.substring(second + 1));
    }

    public static ResourceKey<Level> toDimensionKey(String key) {
        return ResourceKey.create(Registries.DIMENSION, dimension(key));
    }
}
