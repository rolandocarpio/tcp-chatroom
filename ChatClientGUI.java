import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class ChatClientGUI extends ChatClient {

    private JFrame jfrm;
    private static JTextField textEntry;
    public JTextArea window;
    private static JButton enter;
    private static JScrollPane scrollPane;
    private ChatClient chatClient;
    private WriteThread writeThread;
    private boolean hasUsername = false;

    ChatClientGUI(ChatClient chatClient, WriteThread writeThread) {
        this.chatClient = chatClient;
        this.writeThread = writeThread;

        //creates window
        jfrm = new JFrame();
        jfrm.setLayout(new FlowLayout());

        // sizing for chat window
        jfrm.setSize(800, 800);
        jfrm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jfrm.setLocationRelativeTo(null);

        jfrm.setTitle("Chat Client");

        window = new JTextArea();

        // adjusted font and text size
        Font font = new Font("Times New Roman", Font.PLAIN, 25);
        window.setFont(font);
        window.setText("Enter your username: \n");
        window.setEditable(false);
        scrollPane = new JScrollPane(window);

        // increases size of scroll pane
        scrollPane.setPreferredSize(new Dimension(800, 600));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // increases size of the text field
        textEntry = new JTextField("", 50);
        textEntry.setPreferredSize(new Dimension(300, 50));

        // create the send button
        enter = new JButton("SEND");
        enter.setSize(200, 200);
        enter.setMnemonic(KeyEvent.VK_ENTER);
        enter.addActionListener(e -> {

            // add the action listener
            String text = textEntry.getText();
            textEntry.setText("");

            // adds date and time
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a");

            if (!hasUsername) {
                writeThread.setUsername(text);
                hasUsername = true;
                jfrm.setTitle(text + "'s Chat Messenger");
                window.append("\n Welcome to the group chat: " + writeThread.getUsername() + "\n");
            } else {
                String prefix = "[" + dtf.format(LocalDateTime.now()) + "] [" + writeThread.getUsername() + "]: ";
                writeThread.sendMessage(text);
                if (text.equals(".")) {
                    window.append(" \n Thank you for chatting. Goodbye! (Window will close in 5 seconds) \n");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                // Handle the exception
                            }
                            System.exit(0);
                        }
                    });
                }
                else
                    window.append("\n" + prefix + text + "\n");
            }

        });

        // set the default button to send
        jfrm.getRootPane().setDefaultButton(enter);

        // add the components to the window
        jfrm.add(scrollPane);
        jfrm.add(textEntry);
        jfrm.add(enter);
        enter.requestFocus();
        jfrm.setVisible(true);

    }
}
