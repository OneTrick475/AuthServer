import java.io.*;
import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir") + "\\users.txt";

        List<User> users = new ArrayList<>();
        users.add(new User("a", "sad", "aedga", "asgfas", new Password("asfdas", false)));

        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(users);
            System.out.println("Users have been serialized to " + fileName);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}