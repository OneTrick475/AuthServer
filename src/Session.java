import java.sql.Timestamp;
import java.util.Date;

public class Session {
    private static int idCounter = 0;

    private int id;

    private final int secondsToLive = 60;

    private Date created;

    public String getUsername() {
        return username;
    }

    private String username;

    Session(String username) {
        id = idCounter;
        idCounter++;
        created = new Date();
        this.username= username;
    }
    public int getId() {
        return id;
    }

    public boolean isExpired() {
        return (new Date().getTime()) - created.getTime() <= secondsToLive;
    }
}
