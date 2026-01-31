package xyz.voroniyx.deathtotem.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ModConfig {
    public boolean EnableTotemConsume = true;
    public boolean TotemConsumeOnlyWhenLastTotemUsed = true;

    public static String GetConfigPath() {
        Path dataDirectory = FabricLoader.getInstance().getConfigDir();
        return dataDirectory.resolve("death_totem.json").toString();
    }
}

