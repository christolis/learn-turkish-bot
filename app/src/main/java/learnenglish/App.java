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
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author Christolis
 */
public class App {
    private static final char ENTRY_DELIMITER = '|';
    private static final int UPDATE_DELAY = 500;
    private static final String[] CONFIG_PATHS = {
        "dev-config.json", "prod-config.json"
    };

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

        Thread t = new Thread(() -> {
                FileWriter fw = null; // English backup file.

                try (BufferedReader br = new BufferedReader(new FileReader(translationsDir))) {
                fw = new FileWriter(originalsDir);

                for (String line; (line = br.readLine()) != null; ) {
                    String[] fields = line.split("\\" + ENTRY_DELIMITER);
                    final String trType = fields[0];
                    final String trChannelID = fields[1];
                    final String trName = fields[2].replace('_', ' ');

                    switch (trType.toLowerCase()) {
                        case "text_channel": {
                            TextChannel chnl = getJDA().getTextChannelById(trChannelID);

                            if (chnl != null) {
                                fw.write(trType + "|" + trChannelID + "|" + chnl.getName() + "\n");

                                logger.info("Renaming text channel " + chnl.getName() + " to " + trName + "...");
                                chnl.getManager().setName(trName).queue();

                            }
                            break;
                        }
                        case "voice_channel": {
                            VoiceChannel vc = getJDA().getVoiceChannelById(trChannelID);

                            if (vc != null) {
                                fw.write(trType + "|" + trChannelID + "|" + vc.getName() + "\n");

                                logger.info("Renaming voice channel " + vc.getName() + " to " + trName + "...");
                                vc.getManager().setName(trName).queue();
                            }
                            break;
                        }
                        case "category": {
                            Category category = getJDA().getCategoryById(trChannelID);

                            if (category != null) {
                                fw.write(trType + "|" + trChannelID + "|" + category.getName() + "\n");

                                logger.info("Renaming category " + category.getName() + " to " + trName + "...");
                                category.getManager().setName(trName).queue();
                            }
                        }
                    }
                    /* We could potentially sleep the thread for each iteration 
                    * to refrain from hitting Discord's rate limits. */
                    Thread.sleep(UPDATE_DELAY);
                }
                
                fw.close();
            } catch (IOException | InterruptedException e) {
                try {
                    if (fw != null) fw.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });
        t.start();
        return true;
    }

    /**
     * Disables April fools on the server.
     * Reads fields from the original text file as set
     * in the configuration file and updates the channels.
     */
    public void disableAprilFools() {
        final String originalsDir = config.getOriginalTranslationsDir();

        Thread t = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new FileReader(originalsDir))) {
                for (String line; (line = br.readLine()) != null; ) {
                    String[] fields = line.split("\\" + ENTRY_DELIMITER);
                    final String type = fields[0];
                    final String channelID = fields[1];
                    final String name = fields[2].replace('_', ' ');

                    switch (type.toLowerCase()) {
                        case "text_channel": {
                            TextChannel chnl = getJDA().getTextChannelById(channelID);

                            if (chnl != null) {
                                logger.info("Reverting text channel" + chnl.getName() + " to " + name + "...");
                                chnl.getManager().setName(name).queue();
                            }
                            break;
                        }
                        case "voice_channel": {
                            VoiceChannel vc = getJDA().getVoiceChannelById(channelID);

                            if (vc != null) {
                                logger.info("Reverting voice channel " + vc.getName() + "\n");
                                vc.getManager().setName(name).queue();
                            }
                            break;
                        }
                        case "category": {
                            Category category = getJDA().getCategoryById(channelID);

                            if (category != null) {
                                logger.info("Reverting category " + category.getName() + "\n");
                                category.getManager().setName(name).queue();
                            }
                            break;
                        }
                    }

                    Thread.sleep(UPDATE_DELAY);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    /**
     * Initializes the bot's configuration file.
     * Parses the selected file and deserializes it
     * into an instance of Configuration.class
     * @param The configuration's path.
     */
    public boolean initConfig(String path) {
        try {
            final Gson gson = new Gson();
            final ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            config = gson.fromJson(reader, Configuration.class);
            reader.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Starts the application that handles the bot.
     * Automatically sets up the configuration of it as well.
     */
    public void start() {
        for (String conf : CONFIG_PATHS) {
            if (this.initConfig(conf)) break;
        }

        if (this.getConfiguration() == null) {
            logger.error("I was unable to find any configurations! Exiting...");
            System.exit(1);
        }

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
