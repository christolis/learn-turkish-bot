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

    /* The configuration file */
    public Configuration config;
    private static Logger logger = LoggerFactory.getLogger(App.class);

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
    }

    public Configuration getConfiguration() {
        return config;
    }

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }
}
