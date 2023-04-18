import java.awt.*;
import javax.swing.*;

public class ChatServerGUI extends ChatServer {

    private JFrame jfrm;
    private JTextArea window;

    ChatServerGUI() {
        //creates window
        jfrm = new JFrame();
        jfrm.setLayout(new FlowLayout());

        // sizing for chat window
        jfrm.setSize(800, 800);
        jfrm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jfrm.setLocationRelativeTo(null);

        jfrm.setTitle("Chat Server");

        window = new JTextArea();
        
        // adjusted font and text size
        Font font = new Font("Times New Roman", Font.PLAIN, 25);
        window.setFont(font);
        JScrollPane scrollPane = new JScrollPane(window);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        window.setEditable(false);

        jfrm.add(scrollPane);
        jfrm.setVisible(true);
    }

    public void setPortNumber(int port) {
        super.setPort(port);
    }

    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> window.append(message + "\n"));
    }

    public static void main(String[] args) {
        ChatServerGUI server = new ChatServerGUI();
        server.setPortNumber(5000);
        server.startServer();
    }
}