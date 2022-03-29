package learnenglish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author Christolis
 */
public class App {
    private static final String CONFIG_PATH = "config.json";
    private static final char ENTRY_DELIMITER = '|';

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
     * Enables April fools on the server.
     * Automatically performs the text values replacement
     * algorithm once called. Execute with caution if on
     * production build.
     */
    public boolean enableAprilFools() {
        // Translation files shortened
        final String translationsDir = config.getChannelTranslationsDir();
        final String originalsDir = config.getOriginalTranslationsDir();

        /* If original translations file already exists,
           then the April Fools has probably been already
           enabled. */
        if (new File(originalsDir).isFile()) {
            logger.warn("Tried to enable April Fools more than once!");
            logger.warn("Skipping command...");
            return true;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(translationsDir))) {
            // English backup file.
            FileWriter fw = new FileWriter(originalsDir);

            for (String line; (line = br.readLine()) != null; ) {
                String[] fields = line.split("\\" + ENTRY_DELIMITER);
                final String trChannelID = fields[0];
                final String trName = fields[1];

                // Backup original name.
                TextChannel chnl = getJDA().getTextChannelById(trChannelID);
                if (chnl != null) {
                    fw.write(trChannelID + "|" + chnl.getName() + "\n");

                    logger.info("Renaming " + chnl.getName() + " to " + trName + "...");
                    chnl.getManager().setName(trName).queue();

                    /* We could potentially sleep the thread for each iteration 
                     * to refrain from hitting Discord's rate limits. */
                }
            }
            
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Disables April fools on the server.
     * Automatically performs the text values replacement
     * algorithm once called. Execute with caution if on
     * production build.
     */
    public void disableAprilFools() {
        // TODO: Implement
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

    public JDA getJDA() {
        return jda;
    }

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        instance = new App();
        instance.start();
    }
}
