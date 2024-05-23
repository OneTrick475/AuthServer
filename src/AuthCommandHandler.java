import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthCommandHandler implements CommandHandler {
    OutputStream usersOutput;
    Map<String, User> users = new HashMap<>();
    Map<String, Session> sessionsForUser = new HashMap<>();
    Map<Integer, Session> sessions = new HashMap<>();

    Logger logger;

    public AuthCommandHandler(List<User> usersList, OutputStream usersOutput, Logger logger) {
        this.logger = logger;

        this.usersOutput = usersOutput;

        for (User user : usersList) {
            users.put(user.getUsername(), user);
        }
    }

    public String register(String[] paramList) {
        if (paramList.length != 11 || !paramList[1].equals("--username") || !paramList[3].equals("--password") ||
                !paramList[5].equals("--first-name") || !paramList[7].equals("--last-name") ||
                !paramList[9].equals("--email")) {
            return "invalid parameters";
        }
        if (users.containsKey(paramList[2])) {
            return "user already registered";
        }

        User user = new User(paramList[2], paramList[6], paramList[8], paramList[10],
                new Password(paramList[4], false));

        users.put(user.getUsername(), user);

        return login(new String[]{"", "--username", user.getUsername(), "--password", paramList[4]}, "");
    }

    private String login(String[] paramList, String ip) {
        if (paramList[1].equals("--session-id")) {
            return "";
        } else if (paramList[1].equals("--username") && paramList[3].equals("--password")) {
            if (!users.get(paramList[2]).getPassword().getEncryptedPassword().
                    equals(new Password(paramList[4], false).getEncryptedPassword())) {
                logger.logFailedLogin(paramList[2], ip);
                return "wrong password";
            }

            Session session = new Session(paramList[2]);

            sessionsForUser.put(paramList[2], session);

            sessions.put(session.getId(), session);

            return String.valueOf(session.getId());
        } else {
            return "invalid parameters";
        }
    }

    private String deleteUser(String[] paramList) {
        if (paramList.length != 5 || !paramList[1].equals("--session-id") || !paramList[3].equals("--username")) {
            return "invalid parameters";
        }

        if (!sessions.containsKey(Integer.parseInt(paramList[2]))
                || sessions.get(Integer.parseInt(paramList[2])).isExpired()) {
            return "session expired";
        }

        if (!users.containsKey(paramList[4])) {
            return "user to delete doesnt exist";
        }

        Session session = sessions.get(Integer.parseInt(paramList[2]));

        if (!users.get(session.getUsername()).isAdmin()) {
            return "user must be admin";
        }

        users.remove(paramList[4]);
        sessions.remove(sessionsForUser.get(paramList[4]).getId());
        sessionsForUser.remove(paramList[4]);

        return "deleted user: " + paramList[4];
    }

    private String removeAdminUser(String[] paramList, String ip) {
        if (paramList.length != 5 || !paramList[1].equals("--session-id") || !paramList[3].equals("--username")) {
            return "invalid parameters";
        }

        if (!sessions.containsKey(Integer.parseInt(paramList[2]))
                || sessions.get(Integer.parseInt(paramList[2])).isExpired()) {
            return "session expired";
        }

        if (!users.containsKey(paramList[4])) {
            return "user to remove admin from doesnt exist";
        }

        Session session = sessions.get(Integer.parseInt(paramList[2]));

        if (!users.get(session.getUsername()).isAdmin()) {
            logger.logAdminOperation("Remove Admin", session.getUsername(), ip,
                    "remove admin from " + paramList[4],
                    "failed because the user making the change is not admin");
            return "user must be admin";
        }

        if (!users.get(paramList[4]).isAdmin()) {
            return "user is already not admin";
        }

        users.get(paramList[4]).setAdmin(false);

        logger.logAdminOperation("Remove Admin", session.getUsername(), ip,
                "remove admin from " + paramList[4],
                "success");

        return "removed admin: " + paramList[4];
    }

    private String addAdminUser(String[] paramList, String ip) {
        if (paramList.length != 5 || !paramList[1].equals("--session-id") || !paramList[3].equals("--username")) {
            return "invalid parameters";
        }

        if (!sessions.containsKey(Integer.parseInt(paramList[2]))
                || sessions.get(Integer.parseInt(paramList[2])).isExpired()) {
            return "session expired";
        }

        if (!users.containsKey(paramList[4])) {
            return "user to make admin doesnt exist";
        }

        Session session = sessions.get(Integer.parseInt(paramList[2]));

        if (!users.get(session.getUsername()).isAdmin()) {
            logger.logAdminOperation("Make admin", session.getUsername(), ip,
                    "make admin " + paramList[4],
                    "failed because the user making the change is not admin");
            return "user must be admin";
        }

        if (users.get(paramList[4]).isAdmin()) {
            return "user is already admin";
        }

        users.get(paramList[4]).setAdmin(true);

        logger.logAdminOperation("Make admin", session.getUsername(), ip,
                "make admin " + paramList[4],
                "success");

        return "added admin: " + paramList[4];
    }

    private String resetPassword(String[] paramList) {
        if (paramList.length != 9 || !paramList[1].equals("--session-id") || !paramList[3].equals("--username") ||
                !paramList[5].equals("--old-password") || !paramList[7].equals("--new-password")) {
            return "invalid parameters";
        }

        if (!sessions.containsKey(Integer.parseInt(paramList[2]))
                || sessions.get(Integer.parseInt(paramList[2])).isExpired()) {
            return "session expired";
        }

        if (!users.containsKey(paramList[4])) {
            return "user doesnt exist";
        }

        if (Integer.parseInt(paramList[2]) != sessionsForUser.get(paramList[4]).getId()) {
            return "session doesnt match user";
        }

        if (!(new Password(paramList[6], false).equals(users.get(paramList[4]).getPassword()))) {
            return "password isnt correct";
        }

        users.get(paramList[4]).setPassword(new Password(paramList[8], false));

        return "changed password";
    }

    private String updateUser(String[] paramList) {
        if (paramList.length < 2 || !paramList[1].equals("--session-id")) {
            return "invalid parameters";
        }

        if (!sessions.containsKey(Integer.parseInt(paramList[2]))
                || sessions.get(Integer.parseInt(paramList[2])).isExpired()) {
            return "session expired";
        }

        User user = users.get(sessions.get(Integer.parseInt(paramList[2])).getUsername());

        for (int i = 3; i < paramList.length; i++) {
            switch (paramList[i]) {
                case "--new-username":
                    if (i + 1 < paramList.length) {
                        user.setUsername(paramList[++i]);
                    }
                    break;
                case "--new-first-name":
                    if (i + 1 < paramList.length) {
                        user.setFirstName(paramList[++i]);
                    }
                    break;
                case "--new-last-name":
                    if (i + 1 < paramList.length) {
                        user.setLastName(paramList[++i]);
                    }
                    break;
                case "--new-email":
                    if (i + 1 < paramList.length) {
                        user.setEmail(paramList[++i]);
                    }
                    break;
            }
        }
        return "user info updated";
    }

    private String logout(String[] paramList) {
        if (paramList.length < 2 || !paramList[1].equals("--session-id")) {
            return "invalid parameters";
        }

        if (!sessions.containsKey(Integer.parseInt(paramList[2]))
                || sessions.get(Integer.parseInt(paramList[2])).isExpired()) {
            return "session expired";
        }

        sessions.remove(Integer.parseInt(paramList[2]));

        return "log out";
    }

    @Override
    public String execute(String params, String ip) {
        String[] paramList = params.split(" ");

        if (paramList[0].equals("register")) {
            return register(paramList);
        } else if (paramList[0].equals("login")) {
            return login(paramList, ip);
        } else if (paramList[0].equals("logout")) {
            return logout(paramList);
        } else if (paramList[0].equals("update-user")) {
            return updateUser(paramList);
        } else if (paramList[0].equals("reset-password")) {
            return resetPassword(paramList);
        } else if (paramList[0].equals("add-admin-user")) {
            return addAdminUser(paramList, ip);
        } else if (paramList[0].equals("remove-admin-user")) {
            return removeAdminUser(paramList, ip);
        } else if (paramList[0].equals("delete-user")) {
            return deleteUser(paramList);
        }

        User.writeUsersToFile(usersOutput, (List<User>) users.values());

        return "invalid command";
    }
}
