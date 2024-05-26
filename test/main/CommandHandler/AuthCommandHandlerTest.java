package main.CommandHandler;

import main.Logger.AuthLogger;
import main.Logger.Logger;
import main.Session.Session;
import main.User.Password;
import main.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthCommandHandlerTest {
    @Mock
    AuthLogger logger = mock();

    AuthCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        String fileName = System.getProperty("user.dir") + "\\test\\users.txt";

        List<User> users = new ArrayList<>();
        users.add(new User("admin", "admin", "admin",
                "admin", new Password("admin", false)));

        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(users);
            System.out.println("Users have been serialized to " + fileName);
        } catch (IOException i) {
            i.printStackTrace();
        }

        commandHandler = new AuthCommandHandler(fileName, logger);
    }

    @Test
    public void testExecuteWithInvalidCommand() {
        String params = "some command";

        assertEquals("invalid command", commandHandler.execute(params, ""));
    }

    @Test
    public void testRegisterWithValidParams() {
        String params = "register --username idk --password idk --first-name idk --last-name idk --email idk";

        commandHandler.execute(params, "");

        assertTrue(commandHandler.users.containsKey("idk"));
    }

    @Test
    public void testRegisterWithAlreadyRegistered() {
        String params = "register --username admin --password idk --first-name idk --last-name idk --email idk";

        commandHandler.execute(params, "");

        assertEquals("user already registered", commandHandler.execute(params, ""));
    }

    @Test
    public void testRegisterWithInvalid() {
        String params = "register --username admin idk --first-name idk --last-name idk --email idk";

        commandHandler.execute(params, "");

        assertEquals("invalid parameters", commandHandler.execute(params, ""));
    }

    @Test
    public void testRegisterCheckIfMakesAdminWhenNoAdmin() {
        String params = "register --username idk --password idk --first-name idk --last-name idk --email idk";

        commandHandler.execute(params, "");

        assertTrue(commandHandler.users.get("idk").isAdmin());
    }

    @Test
    public void testLoginCreatesSession() {
        String params = "login --username admin --password admin";

        commandHandler.execute(params, "");

        assertFalse(commandHandler.sessions.isEmpty());
    }
    @Test
    public void testLoginWithInvalidParams() {
        String params = "login --user admin --password admin";

        assertEquals("invalid parameters", commandHandler.execute(params, ""));
    }

    @Test
    public void testLoginWithInvalidUsername() {
        String params = "login --username admin1 --password admin";

        assertEquals("user doesnt exist", commandHandler.execute(params, ""));
    }

    @Test
    public void testLoginWithWrongPassword() {
        String params = "login --username admin --password admin1";

        assertEquals("wrong password", commandHandler.execute(params, ""));

        verify(logger, times(1)).logFailedLogin("admin", "");
    }

    @Test
    public void testDeleteUser() {
        commandHandler.users.get("admin").setAdmin(true);
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                        "idk", new Password("idk", false)));

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "delete-user --session-id " +  session.getId() + " --username idk";

        commandHandler.execute(params, "");

        assertFalse(commandHandler.users.containsKey("idk"));
    }

    @Test
    public void testDeleteUserWhenDoesntExist() {
        commandHandler.users.get("admin").setAdmin(true);

        String login = "login --username admin --password admin";

        String id = commandHandler.execute(login, "");

        String params = "delete-user --session-id " +  id + " --username idk";

        assertEquals("user to delete doesnt exist", commandHandler.execute(params, ""));
    }
}
