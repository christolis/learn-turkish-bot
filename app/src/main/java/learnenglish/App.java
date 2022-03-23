package learnenglish;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import learnenglish.config.Configuration;
import learnenglish.listener.ListenerCommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author Christolis
 */
public class App {
    private static final String CONFIG_PATH = "config.json";

    /* An instance of the app running */
    private static App instance;

    /* The configuration file */
    private Configuration config;

    /* An instance of the JDA */
    private JDA jda;

    private static final Set<ListenerAdapter> listeners = new HashSet<>();
    {
        listeners.add(new ListenerCommands());
    }

    /* Application logger */
    public static Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * @return whether the inputted member can use the bot management commands.
     */
    public static boolean canMemberUseCommands(Member member) {
        String[] roles = App.getInstance().getConfiguration().getAllowedRoles();
        List<String> allowedRoles = Arrays.asList(roles);

        return member.getRoles().stream().anyMatch(role -> {
            for (String allowedRole : allowedRoles) {
                if (allowedRole.equals(role.getId()))
                    return true;
            }
            return false;
        });
    }

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

            /* JDABuilder configuration stuff */
            builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
            builder.setBulkDeleteSplittingEnabled(false);
            builder.setActivity(Activity.playing(config.getActivity()));

            /* Register all of our listeners */
            for (ListenerAdapter listener : listeners) {
                builder.addEventListeners(listener);
            }

            jda = builder.build();
            jda.upsertCommand("secret", "Does something really secret!")
                .addOptions(new OptionData(
                            OptionType.STRING, 
                            "secret",
                            "Secret code!",
                            true, true)
                ).queue();
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

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        instance = new App();
        instance .start();
    }
}
