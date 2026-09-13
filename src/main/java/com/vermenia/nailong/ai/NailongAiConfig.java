package com.vermenia.nailong.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.vermenia.nailong.NailongMod;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public final class NailongAiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("nailong").resolve("ai_config.json");

    private static NailongAiConfig instance;

    // 配置字段
    private String apiEndpoint = "https://api.openai.com/v1/chat/completions";
    private String apiKey = "";
    private String model = "gpt-3.5-turbo";

    private NailongAiConfig() {
    }

    public static NailongAiConfig load() {
        if (instance != null) {
            return instance;
        }

        try {
            if (Files.exists(CONFIG_FILE)) {
                String json = Files.readString(CONFIG_FILE, StandardCharsets.UTF_8);
                instance = GSON.fromJson(json, NailongAiConfig.class);
                NailongMod.LOGGER.info("已加载奶龙AI配置");
            } else {
                instance = new NailongAiConfig();
                instance.save();
                NailongMod.LOGGER.info("已创建默认奶龙AI配置");
            }
        } catch (IOException e) {
            NailongMod.LOGGER.error("加载AI配置失败，使用默认配置", e);
            instance = new NailongAiConfig();
        }

        return instance;
    }

    public static NailongAiConfig getInstance() {
        if (instance == null) {
            return load();
        }
        return instance;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            NailongMod.LOGGER.error("保存AI配置失败", e);
        }
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
