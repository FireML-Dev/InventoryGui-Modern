package de.themoep.inventorygui.config;

import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.inventorygui.actions.ClickAction;
import de.themoep.inventorygui.actions.CloseAction;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@NullMarked
public abstract class ConfigGui<T> {

    private final Map<String, ConfigGuiClickAction> actions = new HashMap<>();

    private final ConfigSupplier<T> configSupplier;
    private final Plugin plugin;
    protected final Player player;
    protected final Map<String, Supplier<Component>> replacements = new HashMap<>();

    private @Nullable CloseAction closeAction = null;

    public ConfigGui(HumanEntity human, Plugin plugin, ConfigSupplier<T> configSupplier) {
        if (!(human instanceof Player p)) {
            throw new UnsupportedOperationException("Cannot open ConfigGui for a non-player.");
        }
        this.plugin = plugin;
        this.player = p;
        this.configSupplier = configSupplier;

        actions.putAll(GlobalConfigActions.get());
        addClickActions(actions);
    }

    public void addReplacement(String variable, Component replacement) {
        this.replacements.put(variable, () -> replacement);
    }

    public void addReplacement(String variable, Supplier<Component> replacement) {
        this.replacements.put(variable, replacement);
    }

    public void addReplacements(Map<String, Component> replacements) {
        replacements.forEach(this::addReplacement);
    }

    public void setCloseAction(CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public InventoryGui getGui() {
        InventoryGui gui = new InventoryGui(
            plugin,
            getTitle(),
            getLayout()
        );
        loadFiller(gui);
        loadItems(gui);
        gui.setCloseAction(closeAction);
        configureGui(gui);

        return gui;
    }

    public void open() {
        getGui().show(this.player);
    }

    private void loadFiller(InventoryGui gui) {
        String fillerStr = configSupplier.getString("filler");
        if (fillerStr == null) {
            return;
        }
        Material material = Material.matchMaterial(fillerStr);
        if (material == null) {
            return;
        }
        ItemStack item = ItemStack.of(material);
        item.editMeta(meta -> meta.setHideTooltip(true));
        gui.setFiller(item);
    }

    private void loadItems(InventoryGui gui) {
        configSupplier.getKeys(false).forEach(key -> {
            ConfigSupplier<T> itemSection = configSupplier.getSection(key);
            if (itemSection == null) {
                return;
            }
            addGuiItem(gui, itemSection);
        });
    }

    protected void addGuiItem(InventoryGui gui, ConfigSupplier<T> section) {
        ConfigSupplier<T> itemSection = section.getSection("item");
        if (itemSection == null) {
            return;
        }
        ItemStack item = buildItem(itemSection.getConfig());
        if (item.isEmpty()) {
            return;
        }
        String charString = section.getString("character");
        if (charString == null || charString.isBlank()) {
            return;
        }
        char character = charString.charAt(0);
        // Check if this is a page button.
        GuiPageElement.PageAction pageAction = getPageAction(section.getString("page-action"));
        if (pageAction != null) {
            gui.addElement(fetchPageElement(character, item, section, pageAction));
            return;
        }

        // This is a regular button/item.
        StaticGuiElement actionElement = new StaticGuiElement(character, item, click -> {
            executeClickAction(section, click);
            executeClickCommands(section, click);
            return ClickAction.Result.CANCEL_EVENT;
        });
        gui.addElement(actionElement);
    }

    private GuiPageElement fetchPageElement(char character, ItemStack item, ConfigSupplier<T> section, GuiPageElement.PageAction pageAction) {
        ConfigSupplier<T> fallbackSection = section.getSection("fallback-item");
        if (fallbackSection == null) {
            return new GuiPageElement(character, item, pageAction);
        }
        ItemStack fallback = buildItem(fallbackSection.getConfig());
        return new GuiPageElement(character, item, fallback, pageAction);
    }

    private void executeClickAction(ConfigSupplier<T> section, GuiElement.Click click) {
        ConfigSupplier<T> actionSection = section.getSection("click-action");
        // If not a section, fall back to a string.
        if (actionSection == null) {
            ConfigGuiClickAction action = getAction(section.getString("click-action"));
            if (action != null) {
                action.onClick(this, click);
            }
            return;
        }
        // If a section, filter for the specific click type.
        ConfigGuiClickAction action = switch (click.getType()) {
            case LEFT -> getAction(actionSection.getString("left"));
            case RIGHT -> getAction(actionSection.getString("right"));
            case MIDDLE -> getAction(actionSection.getString("middle"));
            case DROP -> getAction(actionSection.getString("drop"));
            default -> null;
        };
        if (action != null) {
            action.onClick(this, click);
        }
    }

    private void executeClickCommands(ConfigSupplier<T> section, GuiElement.Click click) {
        HumanEntity player = click.getWhoClicked();
        for (String command : section.getStringList("click-commands")) {
            Bukkit.dispatchCommand(player, command);
        }
    }

    public abstract Component getTitle();

    public String[] getLayout() {
        return configSupplier.getStringList("layout").stream()
            .filter(Objects::nonNull)
            .limit(6)
            .toArray(String[]::new);
    }

    private GuiPageElement.@Nullable PageAction getPageAction(@Nullable String action) {
        if (action == null) {
            return null;
        }
        try {
            return GuiPageElement.PageAction.valueOf(action.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public abstract void configureGui(InventoryGui gui);

    protected abstract void addClickActions(Map<String, ConfigGuiClickAction> actionMap);

    protected abstract ItemStack buildItem(T config);

    private @Nullable ConfigGuiClickAction getAction(@Nullable String id) {
        if (id == null) {
            return null;
        }
        return actions.get(id);
    }

    public interface ConfigGuiClickAction {

        ClickAction.Result onClick(ConfigGui<?> gui, GuiElement.Click click);

    }

}
