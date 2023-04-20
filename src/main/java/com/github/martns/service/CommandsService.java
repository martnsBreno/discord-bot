package com.github.martns.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.martns.interfaces.Command;
import com.github.martns.util.EnvToken;
import com.github.martns.util.LavaPlayerAudioProvider;
import com.github.martns.util.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.voice.AudioProvider;

public class CommandsService {

    private final static Map<String, Command> commands = new HashMap<>();

    private EnvToken env = new EnvToken();

    private final String token = env.getToken();

    public void commandsInit() {

        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        // This is an optimization strategy that Discord4J can utilize.
        // It is not important to understand
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);

        // Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer player = playerManager.createPlayer();

        // We will be creating LavaPlayerAudioProvider in the next step
        AudioProvider provider = new LavaPlayerAudioProvider(player);

        commands.put("join", event -> {
            event.getMessage().getChannel().block().createMessage("Entrando no voice chat!").block();
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        // join returns a VoiceConnection which would be required if we were
                        // adding disconnection features, but for now we are just ignoring it.
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }
        });

        final TrackScheduler scheduler = new TrackScheduler(player);
        commands.put("play", event -> {
            final String content = event.getMessage().getContent();
            final List<String> command = Arrays.asList(content.split(" "));
            String link = command.get(1);
            event.getMessage().getChannel().block().createMessage("Tocando: " + link + ", aproveite :)").block();
            playerManager.loadItem(command.get(1), scheduler);
        });

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
                        if (content.startsWith('*' + entry.getKey())) {
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

    static {
        commands.put("ajuda", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Comandos dísponiveis até o momento: \n" +
                        "*ping : te respondo com um pong! \n " +
                        "*join : entro na sua sala \n" +
                        "*tocar <link>: canto uma música")
                .block());
    }

    static {
        commands.put("kick", event -> {

            boolean userHasPermission = event.getMember().get().getBasePermissions().block()
                    .contains(Permission.KICK_MEMBERS);

            if (!userHasPermission) {
                event.getMessage().getChannel().block().createMessage("Ops! Você não possui permissão para expulsar.")
                        .block();

            } else {

                if (event.getMessage().getMemberMentions().isEmpty()) {
                    event.getMessage().getChannel().block().createMessage("Mencione um usuario").block();
                } else {

                    event.getMessage()
                            .getMemberMentions().get(0).kick()
                            .and(event.getMessage().getChannel().block().createMessage("O usuário foi expulso, porém pode voltar ao receber um convite.")).block();
                }

            }
        });
    }

    static {
        commands.put("ban", event -> {

            boolean userHasPermission = event.getMember().get().getBasePermissions().block()
                    .contains(Permission.BAN_MEMBERS);

            if (!userHasPermission) {
                event.getMessage().getChannel().block().createMessage("Ops! Você não possui permissão para banir.")
                        .block();

            } else {

                if (event.getMessage().getMemberMentions().isEmpty()) {
                    event.getMessage().getChannel().block().createMessage("Mencione um usuário").block();
                } else {

                    event.getMessage()
                            .getMemberMentions().get(0).ban()
                            .and(event.getMessage().getChannel().block().createMessage("Usuário banido.")).block();
                }

            }
        });

    }
}