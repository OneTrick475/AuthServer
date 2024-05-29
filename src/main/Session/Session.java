package main.Session;

import java.util.Date;

public class Session {
    private static int idCounter = 0;

    private int id;

    private final int secondsToLive = 60;

    private long created;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Session(String username) {
        id = idCounter;
        idCounter++;
        created = new Date().getTime();
        this.username= username;
    }
    public int getId() {
        return id;
    }

    public boolean isExpired() {
        return ((new Date().getTime()) - created >= secondsToLive);
    }
}
