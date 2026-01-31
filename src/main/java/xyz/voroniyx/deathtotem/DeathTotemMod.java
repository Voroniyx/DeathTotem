package xyz.voroniyx.deathtotem;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.voroniyx.deathtotem.config.JsonConfigManager;
import xyz.voroniyx.deathtotem.config.ModConfig;

import java.io.IOException;

public class DeathTotemMod implements ModInitializer {

    public static final String MOD_ID = "deathtotemmod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static JsonConfigManager<ModConfig> CONFIG;


    @Override
    public void onInitialize() {
        LOGGER.info("DeathTotemMod Initializing");

        loadConfig();
    }

    public void loadConfig() {
        CONFIG = new JsonConfigManager<>(ModConfig.class, ModConfig.GetConfigPath());
        try {
            CONFIG.load();
            CONFIG.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
