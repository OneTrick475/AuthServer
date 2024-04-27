import java.util.List;

public class AuthCommandHandler implements CommandHandler {
    public String register(String[] paramList) {

    }

    @Override
    public String execute(String params) {
        String[] paramList = params.split(" ");

        if (paramList[0].equals("register")){
            return register(paramList);
        }

    }
}
