package com.github.martns.config;

import com.github.martns.service.MessageListener;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class JdaConfig {

    /*
     * 
     * public static final AudioPlayerManager PLAYER_MANAGER;
     * 
     * static {
     * PLAYER_MANAGER = new DefaultAudioPlayerManager();
     * // This is an optimization strategy that Discord4J can utilize to minimize
     * // allocations
     * PLAYER_MANAGER.getConfiguration().setFrameBufferFactory(
     * NonAllocatingAudioFrameBuffer::new);
     * AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
     * AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
     * }
     * 
     */

    Dotenv dotenv = Dotenv.load();
    String token = dotenv.get("DISCORD_TOKEN");

    public JDA iniciarConfiguracao() {

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
                .build();

        jda.addEventListener(new MessageListener());

        return jda;
    }

}
