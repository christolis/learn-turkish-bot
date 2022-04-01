package learnenglish.listener;

import learnenglish.App;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * @author Christolis
 */
public class ListenerCommands extends ListenerAdapter {

    /**
     * Handles command events.
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        App app = App.getInstance();

        /* If a user is not supposed to use our command, let them know. */
        if (!App.canMemberUseCommands(member)) {
            event.reply("Insufficient permissions.").setEphemeral(true).queue();
            return;
        }

        /* If we are dealing with the secret command... */
        if (event.getName().equals("secret")) {
            String key = event.getOption("secret").getAsString();

            /* Couldn't think of any better key secrets.
               But I guess these two work just fine! */
            switch (key.toLowerCase()) {
                case "aprilfools_enable": {
                    event.reply("Nisan aptalları temasını belirleme...").queue();

                    if (!app.enableAprilFools())
                        event.getTextChannel().sendMessage("I could not do that for some reason!");
                    break;
                }
                case "aprilfools_disable": {
                    event.reply("Disabling the April Fools theme...").queue();
                    app.disableAprilFools();
                    break;
                }
                default: {
                    event.reply("*whispering* Psst.. I couldn't understand your secret code!").setEphemeral(true).queue();
                }
            }
        }
    }
}
