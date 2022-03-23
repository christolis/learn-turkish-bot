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

        /* If a user is not supposed to use our command, let them know. */
        if (App.canMemberUseCommands(member)) {
            event.reply("Insufficient permissions.").setEphemeral(true).queue();
            return;
        }

        /* If we are dealing with the secret command... */
        if (event.getName().equals("secret")) {
            String key = event.getOption("secret").getAsString();

            switch (key.toLowerCase()) {
                case "aprilfools_enable": {
                    event.reply("Nisan aptalları temasını belirleme...").queue();
                    break;
                }
                case "aprilfools_disable": {
                    event.reply("Disabling the April Fools theme...").queue();
                    break;
                }
                default: {
                    event.reply("*whispering* Psst.. I couldn't understand your secret code!").setEphemeral(true).queue();
                }
            }
        }
    }
}
