package learnenglish;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.security.auth.login.LoginException;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import learnenglish.config.Configuration;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author Christolis
 */
public class App {
    private static final String CONFIG_PATH = "config.json";

    /* An instance of the app running */
    public static App app;

    /* The configuration file */
    private Configuration config;

    /* Application logger */
    private static Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * Initializes the bot's configuration file.
     * Parses the selected file and deserializes it
     * into an instance of Configuration.class
     */
    public void initConfig() {
        try {
            final Gson gson = new Gson();
            final ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(CONFIG_PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            config = gson.fromJson(reader, Configuration.class);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Could not initialize config.json!");
            System.exit(1);
        }
    }

    /**
     * Starts the application that handles the bot.
     * Automatically sets up the configuration of it as well.
     */
    public void start() {
        this.initConfig();

        try {
            JDABuilder builder = JDABuilder.createDefault(
                    getConfiguration().getToken());

            builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
            builder.setBulkDeleteSplittingEnabled(false);
            builder.setActivity(Activity.playing(config.getActivity()));

            builder.build();
        } catch (LoginException e) {
            logger.error("Failed to initialize bot!");
            System.exit(1);
        }

        logger.info("Ready to go!");
    }

    /**
     * @return an instance of the bot configuration.
     */
    public Configuration getConfiguration() {
        return config;
    }

    public static void main(String[] args) {
        app = new App();
        app.start();
    }
}
