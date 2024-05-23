import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static java.io.FileDescriptor.out;

public class User implements Serializable {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Password password;

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password=" + password +
                ", isAdmin=" + isAdmin +
                '}';
    }

    private boolean isAdmin = false;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public User(String username, String firstName, String lastName, String email, Password password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public static void writeUsersToFile(OutputStream outputStream, List<User> users) {
        try (ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(users);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<User> readUsersFromFile(InputStream inputStream) {
        List<User> users = null;
        try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
            users = (List<User>) in.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
}
