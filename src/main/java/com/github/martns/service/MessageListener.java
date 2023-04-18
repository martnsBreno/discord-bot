package com.github.martns.service;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        switch(event.getMessage().getContentRaw()) {
            case "/n oi":
            event.getMessage().reply("Olá, como posso te ajudar?").mentionUsers(event.getAuthor().getId()).queue();
            break;
            case "/n ajuda":
            event.getMessage().reply(
                "Comandos disponíveis até o momento: \n /n Ajuda - Lista todos os comandos dísponiveis \n/n tocar - Entra no seu canal e toca a música do link enviado").queue();
        }
        }

}
