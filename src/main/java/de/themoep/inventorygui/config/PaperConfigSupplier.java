package de.themoep.inventorygui.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record PaperConfigSupplier(@NonNull ConfigurationSection getConfig) implements ConfigSupplier<ConfigurationSection> {

    @Override
    public @Nullable Object getObject(String path) {
        return getConfig.get(path);
    }

    @Override
    public @Nullable String getString(String path) {
        return getConfig.getString(path);
    }

    @Override
    public @NonNull List<String> getStringList(String path) {
        return getConfig.getStringList(path);
    }

    @Override
    public @NonNull List<String> getKeys(boolean deep) {
        return List.copyOf(getConfig.getKeys(deep));
    }

    @Override
    public @Nullable ConfigSupplier<ConfigurationSection> getSection(String path) {
        ConfigurationSection section = getConfig.getConfigurationSection(path);
        return section == null ? null : new PaperConfigSupplier(section);
    }

}
