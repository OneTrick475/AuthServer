import java.sql.Timestamp;
import java.util.Date;

public class Session {
    static int idCounter = 0;

    int id;

    final int secondsToLive = 60;

    Date created;

    Session() {
        id = idCounter;
        idCounter++;
        created = new Date();
    }
    public int getId() {
        return id;
    }

    public boolean isExpired() {
        return (new Date().getTime()) - created.getTime() <= secondsToLive;
    }
}
