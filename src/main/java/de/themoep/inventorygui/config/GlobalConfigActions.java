package de.themoep.inventorygui.config;

import de.themoep.inventorygui.actions.ClickAction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global ClickActions used in {@link de.themoep.inventorygui.config.ConfigGui}.
 */
@NullMarked
public class GlobalConfigActions {

    private static final Map<String, ConfigGui.ConfigGuiClickAction> ACTIONS = new ConcurrentHashMap<>();

    static {
        ACTIONS.put("exit", (gui, click) -> {
            click.getWhoClicked().closeInventory();
            return ClickAction.Result.CANCEL_EVENT;
        });

        // Add page actions because these are handled separately.
        ACTIONS.put("first-page", (gui, click) -> ClickAction.Result.CANCEL_EVENT);
        ACTIONS.put("previous-page", (gui, click) -> ClickAction.Result.CANCEL_EVENT);
        ACTIONS.put("next-page", (gui, click) -> ClickAction.Result.CANCEL_EVENT);
        ACTIONS.put("last-page", (gui, click) -> ClickAction.Result.CANCEL_EVENT);
    }

    public static boolean register(String id, ConfigGui.ConfigGuiClickAction action, boolean force) {
        id = id.toLowerCase(Locale.ROOT);
        if (!force && ACTIONS.containsKey(id)) {
            return false;
        }
        ACTIONS.put(id, action);
        return true;
    }

    public static boolean register(String id, ConfigGui.ConfigGuiClickAction action) {
        return register(id, action, false);
    }

    public static boolean unregister(String id) {
        return ACTIONS.remove(id.toLowerCase(Locale.ROOT)) != null;
    }

    public static ConfigGui.@Nullable ConfigGuiClickAction get(String id) {
        return ACTIONS.get(id.toLowerCase(Locale.ROOT));
    }

    public static Map<String, ConfigGui.ConfigGuiClickAction> get() {
        return Map.copyOf(ACTIONS);
    }

}
