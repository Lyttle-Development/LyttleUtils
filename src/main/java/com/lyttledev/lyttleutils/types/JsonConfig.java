package com.lyttledev.lyttleutils.types;

import com.google.gson.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JsonConfig utility for managing plugin configuration files using JSON (GSON).
 * Supports robust access to all possible JSON types, safe reading/writing, mapping to POJOs, and custom accessors.
 * Includes defensive checks, comments, and reload/save routines.
 *
 * @param <T> The POJO data class to map the config file to.
 */
public class JsonConfig<T> {
    private final String pluginFolderPath;
    private final String configPath;
    private final JavaPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Class<T> dataClass;

    private T dataCache = null;
    private JsonObject configCache = null;

    /**
     * Initialize JsonConfig with plugin, config file path, and POJO class.
     *
     * @param plugin     The JavaPlugin instance.
     * @param configPath The config file name or relative path.
     * @param dataClass  The class of the data object to map to/from JSON.
     */
    public JsonConfig(JavaPlugin plugin, String configPath, Class<T> dataClass) {
        this.pluginFolderPath = plugin.getDataFolder().getPath();
        this.configPath = configPath;
        this.plugin = plugin;
        this.dataClass = dataClass;
    }

    /**
     * Loads and returns the current POJO config, ensuring it's never null after call.
     * If loading fails, logs error and returns null.
     */
    public @Nullable T getData() {
        if (this.dataCache == null) {
            try {
                File file = new File(this.pluginFolderPath, this.configPath);
                if (!file.exists()) {
                    this.dataCache = dataClass.getDeclaredConstructor().newInstance();
                    saveData();
                } else {
                    try (FileReader reader = new FileReader(file)) {
                        this.dataCache = gson.fromJson(reader, dataClass);
                        if (this.dataCache == null) {
                            this.dataCache = dataClass.getDeclaredConstructor().newInstance();
                        }
                    }
                }
            } catch (Exception e) {
                this.plugin.getLogger().severe("Failed to load config (POJO) " + this.configPath + ": " + e.getMessage());
                this.dataCache = null;
            }
        }
        return this.dataCache;
    }

    /**
     * Loads and returns the current config as JsonObject, ensuring it's never null after call.
     * If loading fails, logs error and returns null.
     */
    private @Nullable JsonObject getConfig() {
        if (this.configCache == null) {
            try {
                File file = new File(this.pluginFolderPath, this.configPath);
                if (!file.exists()) {
                    this.configCache = new JsonObject();
                    saveConfig();
                } else {
                    try (FileReader reader = new FileReader(file)) {
                        JsonElement element = JsonParser.parseReader(reader);
                        this.configCache = (element != null && element.isJsonObject()) ? element.getAsJsonObject() : new JsonObject();
                    }
                }
            } catch (Exception e) {
                this.plugin.getLogger().severe("Failed to load config (JSON) " + this.configPath + ": " + e.getMessage());
                this.configCache = null;
            }
        }
        return this.configCache;
    }

    /**
     * Saves the current POJO data state to disk.
     */
    public void saveData() {
        try (FileWriter writer = new FileWriter(new File(this.pluginFolderPath, this.configPath))) {
            gson.toJson(this.getData(), writer);
        } catch (IOException e) {
            this.plugin.getLogger().warning("Failed to save POJO config: " + e.getMessage());
        }
        this.dataCache = null;
        this.configCache = null;
    }

    /**
     * Saves the current config (JsonObject) state to disk.
     */
    private void saveConfig() {
        try (FileWriter writer = new FileWriter(new File(this.pluginFolderPath, this.configPath))) {
            gson.toJson(this.configCache, writer);
        } catch (IOException e) {
            this.plugin.getLogger().warning("Failed to save JSON config: " + e.getMessage());
        }
        this.dataCache = null;
        this.configCache = null;
    }

    /**
     * Force reload from file.
     */
    public void reload() {
        this.dataCache = null;
        this.configCache = null;
        getData();
        getConfig();
    }

    // --------- Custom JSON accessors for dot notation & primitives ----------

    public @Nullable Object get(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return element != null ? gson.fromJson(element, Object.class) : null;
    }

    public @Nullable String getString(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonPrimitive()) ? element.getAsString() : null;
    }

    public @Nullable Integer getInt(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonPrimitive()) ? element.getAsInt() : null;
    }

    public @Nullable Long getLong(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonPrimitive()) ? element.getAsLong() : null;
    }

    public @Nullable Double getDouble(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonPrimitive()) ? element.getAsDouble() : null;
    }

    public @Nullable Boolean getBoolean(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonPrimitive()) ? element.getAsBoolean() : null;
    }

    public @Nullable List<?> getList(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonArray()) ? gson.fromJson(element, List.class) : null;
    }

    public @Nullable List<String> getStringList(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        if (element != null && element.isJsonArray()) {
            List<String> list = new ArrayList<>();
            element.getAsJsonArray().forEach(e -> {
                if (e.isJsonPrimitive()) list.add(e.getAsString());
            });
            return list;
        }
        return null;
    }

    public @Nullable List<Integer> getIntegerList(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        if (element != null && element.isJsonArray()) {
            List<Integer> list = new ArrayList<>();
            element.getAsJsonArray().forEach(e -> {
                if (e.isJsonPrimitive()) list.add(e.getAsInt());
            });
            return list;
        }
        return null;
    }

    public @Nullable List<Double> getDoubleList(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        if (element != null && element.isJsonArray()) {
            List<Double> list = new ArrayList<>();
            element.getAsJsonArray().forEach(e -> {
                if (e.isJsonPrimitive()) list.add(e.getAsDouble());
            });
            return list;
        }
        return null;
    }

    public @Nullable List<Boolean> getBooleanList(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        if (element != null && element.isJsonArray()) {
            List<Boolean> list = new ArrayList<>();
            element.getAsJsonArray().forEach(e -> {
                if (e.isJsonPrimitive()) list.add(e.getAsBoolean());
            });
            return list;
        }
        return null;
    }

    public @Nullable Map<String, Object> getMap(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonObject()) ? gson.fromJson(element, Map.class) : null;
    }

    public @Nullable Set<String> getKeySet(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonObject()) ? element.getAsJsonObject().keySet() : null;
    }

    public @Nullable JsonObject getSection(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return null;
        JsonElement element = getElementByPath(cfg, path);
        return (element != null && element.isJsonObject()) ? element.getAsJsonObject() : null;
    }

    public void set(String path, @Nullable Object value) {
        JsonObject cfg = this.getConfig();
        setElementByPath(cfg, path, gson.toJsonTree(value));
        saveConfig();
    }

    public boolean remove(String path) {
        JsonObject cfg = this.getConfig();
        boolean removed = removeElementByPath(cfg, path);
        if (removed) saveConfig();
        return removed;
    }

    public boolean contains(String path) {
        JsonObject cfg = this.getConfig();
        return cfg != null && getElementByPath(cfg, path) != null;
    }

    public boolean containsLowercase(String path) {
        JsonObject cfg = this.getConfig();
        if (cfg == null) return false;
        for (String key : cfg.keySet()) {
            if (key.equalsIgnoreCase(path)) return true;
        }
        return false;
    }

    public String[] getKeys(String path) {
        Set<String> keys = getKeySet(path);
        return keys != null ? keys.toArray(new String[0]) : null;
    }

    public Object[] getAll(String path) {
        Map<String, Object> map = getMap(path);
        return map != null ? map.values().toArray(new Object[0]) : new Object[0];
    }

    public void clear() {
        if (new File(this.pluginFolderPath, this.configPath).delete()) {
            this.reload();
        }
    }

    // ---------- Path helpers for dot-notation JSON -------------

    private JsonElement getElementByPath(JsonObject obj, String path) {
        String[] parts = path.split("\\.");
        JsonElement current = obj;
        for (String part : parts) {
            if (current == null || !current.isJsonObject()) return null;
            JsonObject currentObj = current.getAsJsonObject();
            if (!currentObj.has(part)) return null;
            current = currentObj.get(part);
        }
        return current;
    }

    private void setElementByPath(JsonObject obj, String path, JsonElement value) {
        String[] parts = path.split("\\.");
        JsonObject current = obj;
        for (int i = 0; i < parts.length - 1; i++) {
            String key = parts[i];
            if (!current.has(key) || !current.get(key).isJsonObject()) {
                JsonObject child = new JsonObject();
                current.add(key, child);
            }
            current = current.getAsJsonObject(key);
        }
        current.add(parts[parts.length - 1], value);
    }

    private boolean removeElementByPath(JsonObject obj, String path) {
        String[] parts = path.split("\\.");
        JsonObject current = obj;
        for (int i = 0; i < parts.length - 1; i++) {
            String key = parts[i];
            if (!current.has(key) || !current.get(key).isJsonObject()) return false;
            current = current.getAsJsonObject(key);
        }
        return current.remove(parts[parts.length - 1]) != null;
    }
}