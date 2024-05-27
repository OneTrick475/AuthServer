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
        User user = new User("admin", "admin", "admin",
                "admin", new Password("admin", false));
        user.setAdmin(true);
        users.add(user);

        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(users);
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
        String fileName = System.getProperty("user.dir") + "\\test\\users123.txt";
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(null);
        } catch (IOException i) {
            i.printStackTrace();
        }
        commandHandler = new AuthCommandHandler(fileName, logger);

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
        String login = "login --username admin --password admin";

        String id = commandHandler.execute(login, "");

        String params = "delete-user --session-id " +  id + " --username idk";

        assertEquals("user to delete doesnt exist", commandHandler.execute(params, ""));
    }

    @Test
    public void testDeleteUserWithInvalidParameters() {
        String login = "login --username --password admin";

        String id = commandHandler.execute(login, "");

        String params = "delete-user --session-id " +  id + " --username idk";

        assertEquals("invalid parameters", commandHandler.execute(params, ""));
    }

    @Test
    public void testDeleteUserNotAdmin() {
        commandHandler.users.get("admin").setAdmin(false);
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "delete-user --session-id " +  session.getId() + " --username idk";

        commandHandler.execute(params, "");

        assertEquals("user must be admin", commandHandler.execute(params, ""));
    }

    @Test
    public void testDeleteUserDeletesSessionOfUser() {
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));

        String login = "login --username idk --password idk";

        commandHandler.execute(login, "");

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "delete-user --session-id " +  session.getId() + " --username idk";

        commandHandler.execute(params, "");

        assertFalse(commandHandler.sessionsForUser.containsKey("idk"));
    }

    @Test
    public void testRemoveAdminUser() {
        String fileName = System.getProperty("user.dir") + "\\test\\users.txt";

        List<User> usersList = new ArrayList<>();
        User user = new User("admin", "admin", "admin",
                "admin", new Password("admin", false));
        User user2 = new User("idk", "idk", "idk",
                "idk", new Password("idk", false));
        user.setAdmin(true);
        user2.setAdmin(true);
        usersList.add(user);
        usersList.add(user2);

        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(usersList);
        } catch (IOException i) {
            i.printStackTrace();
        }

        commandHandler = new AuthCommandHandler(fileName, logger);

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "remove-admin-user --session-id " +  session.getId() + " --username idk";

        commandHandler.execute(params, "");

        assertFalse(commandHandler.users.get("idk").isAdmin());
        verify(logger, times(1)).logAdminOperation("Remove Admin", "admin", "",
                "remove admin from " + "idk",
                "success");
    }

    @Test
    public void testRemoveAdminUserWithInvalidParameters() {
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));
        commandHandler.users.get("idk").setAdmin(true);

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "remove-admin-user session-id " +  session.getId() + " --username idk";

        assertEquals("invalid parameters", commandHandler.execute(params, ""));
    }

    @Test
    public void testRemoveAdminUserWithUserThatDoesntExist() {
        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "remove-admin-user --session-id " +  session.getId() + " --username idk";

        assertEquals("user to remove admin from doesnt exist", commandHandler.execute(params, ""));
    }

    @Test
    public void testRemoveAdminUserWithUserMakingTheChangeNotAdmin() {
        commandHandler.users.get("admin").setAdmin(false);
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));
        commandHandler.users.get("idk").setAdmin(true);

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "remove-admin-user --session-id " +  session.getId() + " --username idk";

        assertEquals("user must be admin", commandHandler.execute(params, ""));
        verify(logger, times(1)).logAdminOperation("Remove Admin", "admin", "",
                "remove admin from " + "idk",
                "failed because the user making the change is not admin");
    }

    @Test
    public void testRemoveAdminUserWithOneAdmin() {
        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "remove-admin-user --session-id " +  session.getId() + " --username admin";

        assertEquals("user is the only admin, operation denied", commandHandler.execute(params, ""));
        verify(logger, times(1)).logAdminOperation("Remove Admin", "admin", "",
                "remove admin from " + "admin",
                "user is the only admin, operation denied");
    }

    @Test
    public void testRemoveAdminUserWithUserAlreadyNotAdmin() {
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "remove-admin-user --session-id " +  session.getId() + " --username idk";

        assertEquals("user is already not admin", commandHandler.execute(params, ""));
    }

    @Test
    public void testLogOutWithInvalidParameters() {
        String params = "logout --sessionid 3";

        assertEquals("invalid parameters", commandHandler.execute(params, ""));
    }

    @Test
    public void testLogOut() {
        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "logout --session-id " + session.getId();

        commandHandler.execute(params, "");

        assertFalse(commandHandler.sessions.containsKey(session.getId()));
    }

    @Test
    public void testAddAdminUserWithInvalidParameters() {
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "add-admin-user sessionid " +  session.getId() + " --username idk";

        assertEquals("invalid parameters", commandHandler.execute(params, ""));
    }

    @Test
    public void testAddAdminUser() {
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "add-admin-user --session-id " +  session.getId() + " --username idk";

        commandHandler.execute(params, "");

        assertTrue(commandHandler.users.get("idk").isAdmin());
        verify(logger, times(1)).logAdminOperation("Make admin", "admin",
                "", "make admin " + "idk",
                "success");
    }

    @Test
    public void testAddAdminUserWhenUserMakingTheChangeNotAdmin() {
        commandHandler.users.get("admin").setAdmin(false);
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "add-admin-user --session-id " +  session.getId() + " --username idk";



        assertEquals("user must be admin", commandHandler.execute(params, ""));
        verify(logger, times(1)).logAdminOperation("Make admin", "admin",
                "", "make admin " + "idk",
                "failed because the user making the change is not admin");
    }

    @Test
    public void testAddAdminUserWhenUserDoesntExist() {
        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "add-admin-user --session-id " +  session.getId() + " --username idk";

        assertEquals("user to make admin doesnt exist", commandHandler.execute(params, ""));
    }

    @Test
    public void testAddAdminUserWhenUserIsAlreadyAdmin() {
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));
        commandHandler.users.get("idk").setAdmin(true);

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "add-admin-user --session-id " +  session.getId() + " --username idk";

        assertEquals("user is already admin", commandHandler.execute(params, ""));
    }

    @Test
    public void testUpdateUser() {
        commandHandler.users.put("idk", new User("idk", "idk", "idk",
                "idk", new Password("idk", false)));
        commandHandler.users.get("idk").setAdmin(true);

        Session session = new Session("admin");

        commandHandler.sessions.put(session.getId(), session);
        commandHandler.sessionsForUser.put("admin", session);

        String params = "add-admin-user --session-id " +  session.getId() + " --username idk";

        assertEquals("user is already admin", commandHandler.execute(params, ""));
    }

}
