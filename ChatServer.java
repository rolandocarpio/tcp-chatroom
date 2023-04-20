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


// each client connection is processed in a separate thread to handle multiple
// clients
// reads messages sent from the client and broadcasts for all other connected
// clients
class UserThread extends Thread {
    private Socket socket;
    private ChatServer chatServer;
    private PrintWriter writer;

    public UserThread(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    // allow this thread to broadcast chat chatServer
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(input)));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            printUsers();

            String username = reader.readLine();
            chatServer.addUsername(username);

            String serverMessage = "\nA NEW USER HAS NOW CONNECTED: " + username;
            chatServer.broadcast(serverMessage, this);

            String clientMessage;

            // disconnects the user when they type ".
            while (!(clientMessage = reader.readLine()).equals(".")) {
                serverMessage = "[" + username + "]: " + clientMessage;
                chatServer.broadcast(serverMessage, this);
            }

            chatServer.removeUser(username, this);
            serverMessage = username + " IS NOW DISCONNECTED";
            chatServer.broadcast(serverMessage, this);
            socket.close();

        } catch (IOException ex) {
            System.out.println("USERTHREAD ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // will show a newly connected user the list of users already in the chat room
    public void printUsers() {
        if (chatServer.hasUsers()) {
            writer.println("CONNECTED USERS: " + chatServer.getUsernames());
        } else {
            writer.println("YOU'RE ALL ALONE");
        }
    }

    // message sent to client
    void sendMessage(String message) {
        writer.println(message);
    }
}