package com.github.martns;

import javax.security.auth.login.LoginException;

import com.github.martns.config.JdaConfig;

import net.dv8tion.jda.api.JDA;



/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws LoginException {

        JdaConfig JdaConfig = new JdaConfig();

        JDA jda = JdaConfig.iniciarConfiguracao();

    }
}