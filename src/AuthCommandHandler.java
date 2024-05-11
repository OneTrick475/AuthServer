import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthCommandHandler implements CommandHandler {

    Map<String, User> users = new HashMap<>();
    Map<String, Session> sessions = new HashMap<>();

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

        users.put(user.username(), user);

        return login(new String[]{user.username(), paramList[4]});
    }

    private String login(String[] paramList) {
        if (paramList[1].equals("--session-id")) {
            return "";
        } else if (paramList[1].equals("--username") && paramList[2].equals("--password")) {
            Session session = new Session();

            sessions.put(paramList[2], session);

            return String.valueOf(session.id);
        } else {
            return "invalid parameters";
        }
    }

    private String deleteUser(String[] paramList) {
    }

    private String removeAdminUser(String[] paramList) {
    }

    private String addAdminUser(String[] paramList) {
    }

    private String resetPassword(String[] paramList) {
    }

    private String updateUser(String[] paramList) {
    }

    private String logout(String[] paramList) {
    }

    @Override
    public String execute(String params) {
        String[] paramList = params.split(" ");

        if (paramList[0].equals("register")) {
            return register(paramList);
        } else if (paramList[0].equals("login")) {
            return login(paramList);
        } else if (paramList[0].equals("logout")) {
            return logout(paramList);
        } else if (paramList[0].equals("update-user")) {
            return updateUser(paramList);
        } else if (paramList[0].equals("reset-password")) {
            return resetPassword(paramList);
        } else if (paramList[0].equals("add-admin-user")) {
            return addAdminUser(paramList);
        } else if (paramList[0].equals("remove-admin-user")) {
            return removeAdminUser(paramList);
        } else if (paramList[0].equals("delete-user")) {
            return deleteUser(paramList);
        }

        return "Invalid Command";
    }
}
