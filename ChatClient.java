import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatClient {
    private String hostName;
    private int port;
    private String username = null;
    private ReadThread readThread;
    private WriteThread writeThread;
    private ChatClientGUI clientGUI;

    // constructor
    public ChatClient(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public ChatClient() {

    }

    public void startClient() {
        try {
            Socket socket = new Socket(hostName, port);

            System.out.println("Connected to the chat server");

            writeThread = new WriteThread(socket, this);
            clientGUI = new ChatClientGUI(this, writeThread);

            readThread = new ReadThread(socket, this, clientGUI);
            readThread.start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IO Error: " + ex.getMessage());

        }
    }

    // setters and getters for username
    void setUsername(String username) {
        this.username = username;
    }

    String getUsername() {
        return username;
    }

    public static void main(String[] args) {

        // host name
        String hostName = "localhost";
        // default port 5000 as test
        int port = 5000;

        ChatClient client = new ChatClient(hostName, port);
        client.startClient();
    }
}

// thread to read input from the server and printing it
class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;
    private ChatClientGUI clientGUI;

    public ReadThread(Socket socket, ChatClient client, ChatClientGUI clientGUI) {
        this.socket = socket;
        this.client = client;
        this.clientGUI = clientGUI;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("RETREIVING INPUT STREAM ERROR" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a");
        String response = "";

        while (true) {
            try {
                response = reader.readLine();
                if (response != null) {
                    String formatedResponse = "["+ dtf.format(LocalDateTime.now()) + "] " + response;
                    // System.out.println(formatedResponse);
                    clientGUI.window.append(formatedResponse + "\n");
                } else {
                    socket.close();
                }
            } catch (IOException ex) {
                System.out.println("READING FROM SERVER ERROR: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }
}

// thread to read input from the client user and send the message to server
class WriteThread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient chatClient;
    private String username;

    public WriteThread(Socket socket, ChatClient chatClient) {
        this.socket = socket;
        this.chatClient = chatClient;
        username = "";

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("OUTPUTSTREAM ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        String text = message;

        try {
            if (text.equals(".")) {
                writer.println(".");
            } else
                writer.println(text);
        } catch (Exception ex) {
            System.out.println("WRITING TO SERVER ERROR: " + ex.getMessage());
        }
    }

    public void setUsername(String username) {
        this.username = username;
        chatClient.setUsername(username);
        writer.println(username);
    }

    public String getUsername() {
        return username;
    }
}

// each client connection is processed in a separate thread to handle multiple
// clients
// reads messages sent from the client and broadcasts for all other connected
// clients
class UserThread extends Thread {
    private Socket socket;
    public ChatServer chatServer;
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
            while (chatServer.usernames.contains(username))
            {
                writer.println("\nUsername taken, please enter a new usernameClient: ");
                username = reader.readLine();
            }
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