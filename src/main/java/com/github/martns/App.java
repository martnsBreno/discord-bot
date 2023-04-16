package com.github.martns;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;



/**
 * Hello world!
 *
 */
public class App extends ListenerAdapter {

    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault("MTA5NzI2NDMyNzgwMDY2NDE4NQ.GYkLUl.d5j45RXSLwg2br7PFGkgAvQwxIbFtqR3NPC1lI")
        .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
        .build();

        jda.addEventListener(new App());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // TODO Auto-generated method stub
        System.out.println(event.getMember().getId());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        switch(event.getMessage().getContentRaw()) {
            case "/n Oi":
            event.getMessage().reply("Olá, como posso te ajudar? Para ter uma lista de todos os meus comandos basta digitar /n Ajuda").mentionUsers(event.getAuthor().getId()).queue();
            break;
            case "/n Ajuda":
            event.getMessage().reply("Comandos disponíveis até o momento: \n /n Ajuda").queue();
        }
        }
    }