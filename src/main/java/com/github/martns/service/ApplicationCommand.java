package com.github.martns.service;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class ApplicationCommand {

        void initAppCommand(GatewayDiscordClient client) {
                // Get our application's ID
                long applicationId = client.getRestClient().getApplicationId().block();

                long guildId = 1032071775514808391L;

                // Build our command's definition
                ApplicationCommandRequest greetCmdRequest = ApplicationCommandRequest.builder()
                                .name("greet")
                                .description("Greets You")
                                .addOption(ApplicationCommandOptionData.builder()
                                                .name("name")
                                                .description("Your name")
                                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                                .required(true)
                                                .build())
                                .build();

                // Create the command with Discord
                client.getRestClient().getApplicationService()
                                .createGuildApplicationCommand(applicationId, guildId, greetCmdRequest)
                                .subscribe();

                client.on(ChatInputInteractionEvent.class, event -> {
                        if (event.getCommandName().equals("greet")) {
                                return event.reply("Greetings!");
                        }
                        return null;
                }).subscribe();
        }
}
