import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AuthServer {
    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static String usersFile = System.getProperty("user.dir") + "\\users.txt";

    private static String loginLogs = System.getProperty("user.dir") + "\\logins.txt";

    private static String changesLogs = System.getProperty("user.dir") + "\\changes.txt";

    private static void setupFiles() {

    }

    private static CommandHandler setupCommandHandler() {
        List<User> users;
        try (FileInputStream usersInput = new FileInputStream(usersFile)){
            users = User.readUsersFromFile(usersInput);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            FileOutputStream usersOutput = new FileOutputStream(usersFile);
            FileOutputStream loginsOutput = new FileOutputStream(loginLogs);
            FileOutputStream changesOutput = new FileOutputStream(changesLogs);

            Logger logger = new AuthLogger(loginsOutput, changesOutput);
            return new AuthCommandHandler(users, usersOutput, logger);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            CommandHandler commandHandler = setupCommandHandler();

            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    // select() is blocking but may still return with 0, check javadoc
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();

                        String ip = sc.getRemoteAddress().toString();

                        buffer.clear();
                        int r = sc.read(buffer);
                        if (r < 0) {
                            System.out.println("Client has closed the connection");
                            sc.close();
                            continue;
                        }
                        buffer.flip();
                        byte[] byteArray = new byte[buffer.remaining()];
                        buffer.get(byteArray);
                        String command = new String(byteArray, "UTF-8");
                        String response = commandHandler.execute(command, ip);
                        buffer.clear();
                        buffer.put(response.getBytes());
                        buffer.flip();

                        sc.write(buffer);

                    } else if (key.isAcceptable()) {
                        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
                        SocketChannel accept = sockChannel.accept();
                        accept.configureBlocking(false);
                        accept.register(selector, SelectionKey.OP_READ);
                    }

                    keyIterator.remove();
                }

            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }
}

