package de.themoep.inventorygui.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ConfigSupplier<T> {

    @Nullable Object getObject(String path);

    @Nullable String getString(String path);

    @NonNull List<String> getStringList(String path);

    @NonNull List<String> getKeys(boolean deep);

    @NonNull T getConfig();

    @Nullable ConfigSupplier<T> getSection(String path);

}
