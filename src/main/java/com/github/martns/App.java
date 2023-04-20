package com.github.martns;

import javax.security.auth.login.LoginException;

import com.github.martns.service.CommandsService;

/**
 * Hello world!
 *
 */
public class App {
    
    public static void main(String[] args) throws LoginException {

        CommandsService botService = new CommandsService();

        botService.commandsInit();

    }

}