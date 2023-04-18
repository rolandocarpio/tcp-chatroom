import java.net.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ChatServer extends ChatClient {
    private int port;
    private ChatServerGUI serverGUI;

    private Set<String> usernames = new HashSet<>();
    private Set<UserThread> userThreads = new HashSet<>();

    public ChatServer(int port) {
        this.port = port;
        serverGUI = new ChatServerGUI();
    }

    public ChatServer() {

    }

    //delivers the message from one user to the others
    public void broadcast(String message, UserThread excludeUser) {
        for (UserThread user : userThreads) {
            if (user != excludeUser) {
                user.sendMessage(message);
            }
        }
        serverGUI.displayMessage(message);
    }

    // newly connected client's username stored
    public void addUsername(String username) {
        usernames.add(username);
    }

    //removes user and UserThread when client types "." to disconnect
    public void removeUser(String userName, UserThread user) {
        boolean removed = usernames.remove(userName);
        if (removed) {
            userThreads.remove(user);
            serverGUI.displayMessage("The user " + userName + " left");
        }
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Set<String> getUsernames() {
        return this.usernames;
    }

    public Set<UserThread> getUserThreads() {
        return this.userThreads;
    }

    // checks if user is alone or there are others connected in groupchat
    boolean hasUsers() {
        return !this.usernames.isEmpty();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverGUI.displayMessage("Chat server is listening to port: " + port);
            while (true) {
                Socket socket = serverSocket.accept();

                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException ex) {
            serverGUI.displayMessage(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(5000);
        server.startServer();
    }
}