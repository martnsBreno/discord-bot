package com.github.martns.util;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvToken {
    
    Dotenv dotenv = Dotenv.load();

    public String getToken() {
        return dotenv.get("DISCORD_TOKEN");
    }
}
