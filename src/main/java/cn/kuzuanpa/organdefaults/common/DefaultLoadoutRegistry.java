package cn.kuzuanpa.organdefaults.common;

import java.util.List;

public final class DefaultLoadoutRegistry {
    private static volatile List<DefaultLoadoutDefinition> LOADOUTS = List.of();

    private DefaultLoadoutRegistry() {
    }

    public static List<DefaultLoadoutDefinition> getLoadouts() {
        return LOADOUTS;
    }

    public static void setLoadouts(List<DefaultLoadoutDefinition> loadouts) {
        LOADOUTS = List.copyOf(loadouts);
    }
}
