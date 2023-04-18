package com.github.martns;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import com.github.martns.interfaces.Command;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Hello world!
 *
 */
public class App {

    private final static Map<String, Command> commands = new HashMap<>();

    public static void main(String[] args) throws LoginException {

        Dotenv env = Dotenv.load();

        String token = env.get("DISCORD_TOKEN");

        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build()
                .login()
                .block();
        client.getEventDispatcher().on(MessageCreateEvent.class)
                // subscribe is like block, in that it will *request* for action
                // to be done, but instead of blocking the thread, waiting for it
                // to finish, it will just execute the results asynchronously.
                .subscribe(event -> {
                    // 3.1 Message.getContent() is a String
                    final String content = event.getMessage().getContent();

                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        // We will be using ! as our "prefix" to any command in the system.
                        if (content.startsWith('%' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });
        client.onDisconnect().block();

    }

    static {
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Pong!").block());
    }

}