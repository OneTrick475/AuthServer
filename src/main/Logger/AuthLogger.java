package main.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.Timestamp;

public class AuthLogger implements Logger {
    OutputStream logins;
    OutputStream changes;

    private static int idCounter = 0;

    public AuthLogger(OutputStream logins, OutputStream changes) {
        this.logins = logins;
        this.changes = changes;
    }

    @Override
    public void logFailedLogin(String username, String ip) {
        try {
            logins.write(new Timestamp(System.currentTimeMillis()).toString().getBytes());
            logins.write("\n".getBytes());
            logins.write(username.getBytes());
            logins.write("\n".getBytes());
            logins.write(ip.getBytes());
            logins.write("\n".getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void logAdminOperation(String type, String username, String ip, String change, String result) {
        try {
            changes.write(new Timestamp(System.currentTimeMillis()).toString().getBytes());
            changes.write("\n".getBytes());
            changes.write(idCounter);
            changes.write("\n".getBytes());
            changes.write(type.getBytes());
            changes.write("\n".getBytes());
            changes.write(username.getBytes());
            changes.write("\n".getBytes());
            changes.write(ip.getBytes());
            changes.write("\n".getBytes());
            changes.write(change.getBytes());
            changes.write("\n".getBytes());

            changes.write(new Timestamp(System.currentTimeMillis()).toString().getBytes());
            changes.write("\n".getBytes());
            changes.write(idCounter++);
            changes.write("\n".getBytes());
            changes.write(type.getBytes());
            changes.write("\n".getBytes());
            changes.write(username.getBytes());
            changes.write("\n".getBytes());
            changes.write(ip.getBytes());
            changes.write("\n".getBytes());
            changes.write(result.getBytes());
            changes.write("\n".getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
