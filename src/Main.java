import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        List<User> users2 = User.readUsersFromFile(System.getProperty("user.dir") + "\\users.txt");

        for ( var user : users2) {
            System.out.println(user);
        }
    }
}