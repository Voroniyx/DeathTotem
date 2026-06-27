package xyz.voroniyx.deathtotem.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

public class ModConfig {
    public boolean EnableTotemConsume = true;
    public boolean TotemConsumeOnlyWhenLastTotemUsed = true;
    public HashSet<PlayerOverrides> PlayerOverrides;

    public ModConfig() {
        PlayerOverrides = new HashSet<>();
    }

    public static String GetConfigPath() {
        Path dataDirectory = FabricLoader.getInstance().getConfigDir();
        return dataDirectory.resolve("death_totem.json").toString();
    }

    public class PlayerOverrides {
        public PlayerOverrides(UUID playerUUID) {
            PlayerUUID = playerUUID;
        }

        public UUID PlayerUUID;
        public Boolean EnableTotemConsume;
        public Boolean TotemConsumeOnlyWhenLastTotemUsed;
    }

    public boolean HasActiveEnableTotemConsumeOverwriteThatIsTrue(UUID playerUUID) {
        var overrideConfig = PlayerOverrides.stream().filter(x -> x.PlayerUUID == playerUUID).findAny();
        if (overrideConfig.isEmpty()) {
            return false;
        }

        var value = overrideConfig.get().EnableTotemConsume;

        if (value == null) {
            return false;
        }

        return value;
    }

    public Boolean GetTotemConsumeOnlyWhenLastTotemUsedOverwrite(UUID playerUUID) {
        var overrideConfig = PlayerOverrides.stream().filter(x -> x.PlayerUUID == playerUUID).findAny();
        return overrideConfig.map(playerOverrides -> playerOverrides.TotemConsumeOnlyWhenLastTotemUsed).orElse(null);

    }

    public boolean AddOrUpdatePlayerOverride(UUID playerUUID, String option, boolean newValue) {
        var overrideConfig = PlayerOverrides.stream().filter(x -> x.PlayerUUID == playerUUID).findAny();
        ModConfig.PlayerOverrides config;

        if (overrideConfig.isEmpty()) {
            var newConfig = new PlayerOverrides(playerUUID);
            PlayerOverrides.add(newConfig);
            config = newConfig;
        } else {
            config = overrideConfig.get();
        }

        var updated = false;
        switch (option) {
            case "EnableTotemConsume": {
                config.EnableTotemConsume = newValue;
                updated = true;
                break;
            }
            case "TotemConsumeOnlyWhenLastTotemUsed": {
                config.TotemConsumeOnlyWhenLastTotemUsed = newValue;
                updated = true;
                break;
            }
            default: {
            }
        }

        return updated;
    }

    public void DeletePlayerOverride(UUID playerUUID, String option) {

    }
}

