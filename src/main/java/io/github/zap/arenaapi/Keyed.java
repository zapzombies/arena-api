package io.github.zap.arenaapi;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Keyed {
    @NotNull String getKey();

    static @NotNull Keyed of(@NotNull String key) {
        return new Keyed() {
            @Override
            public @NotNull String getKey() {
                return key;
            }

            @Override
            public int hashCode() {
                return key.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if(obj instanceof Keyed keyed) {
                    return keyed.getKey().equals(key);
                }

                return false;
            }

            @Override
            public String toString() {
                return "Keyed{" + getKey() + "}";
            }
        };
    }
}
