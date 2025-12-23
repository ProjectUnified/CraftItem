package io.github.projectunified.craftitem.nbt;

import java.util.Objects;

/**
 * A class indicating the raw NBT data.
 */
public class NBTRaw {
    public final String value;

    /**
     * Create the instance
     *
     * @param value the raw data
     */
    public NBTRaw(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NBTRaw nbtRaw = (NBTRaw) o;
        return Objects.equals(value, nbtRaw.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
