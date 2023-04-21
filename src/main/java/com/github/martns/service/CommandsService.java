package com.github.martns.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.martns.interfaces.Command;
import com.github.martns.util.EnvToken;
import com.github.martns.util.LavaPlayerAudioProvider;
import com.github.martns.util.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Permission;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;

public class CommandsService {

    private final static Map<String, Command> commands = new HashMap<>();

    private ScheduledExecutorService scheduler;

    private ApplicationCommand applicationCommand = new ApplicationCommand();

    private EnvToken env = new EnvToken();

    private final String token = env.getToken();

    public void commandsInit() {

        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        AudioSourceManagers.registerRemoteSources(playerManager);

        final AudioPlayer player = playerManager.createPlayer();

        AudioProvider provider = new LavaPlayerAudioProvider(player);

        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            final Snowflake memberId = event.getMember().get().getId();
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        VoiceConnection voiceCon = channel.join(spec -> spec.setProvider(provider)).block();
                        event.getMessage().getChannel().block().createMessage("Entrando no voice chat!").block();
                        startChannelStatusChecker(memberId, voiceCon, channel, event);
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
                    final String content = event.getMessage().getContent();

                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        if (content.startsWith('*' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });

        applicationCommand.initAppCommand(client);

        client.onDisconnect().block();
    }

    private void startChannelStatusChecker(Snowflake memberId, VoiceConnection voiceCon, VoiceChannel channel,
            MessageCreateEvent event) {

        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {

                if (!channel.isMemberConnected(memberId).block().booleanValue()) {
                    event.getMessage().getChannel().block()
                            .createMessage(
                                    "Saindo do canal, a pessoa que me convidou para a chamada se desconectou por muito tempo, para me chamar de volta bastar usar *join novamente")
                            .block();
                    voiceCon.disconnect().block();
                    stopChannelStatusChecker();
                }
            }, 60, 60, TimeUnit.SECONDS);
        }
    }

    private void stopChannelStatusChecker() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
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
                        "*join : entro na sua sala \n" +
                        "*tocar <link>: toca uma música \n" +
                        "*kick: expulsa o usuário mencionado \n" +
                        "*ban: bane o usuário mencionado")
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
                            .getMemberMentions().get(0).kick().block();

                    event.getMessage().getChannel().block()
                            .createMessage("O usuário foi expulso, porém pode voltar ao receber um convite.")
                            .block();
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