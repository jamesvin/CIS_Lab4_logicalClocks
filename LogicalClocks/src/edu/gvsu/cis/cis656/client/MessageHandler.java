package edu.gvsu.cis.cis656.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.sun.imageio.spi.OutputStreamImageOutputStreamSpi;

import edu.gvsu.cis.cis656.clock.VectorClock;
import edu.gvsu.cis.cis656.message.Message;
import edu.gvsu.cis.cis656.message.MessageComparator;
import edu.gvsu.cis.cis656.message.MessageTypes;
import edu.gvsu.cis.cis656.queue.PriorityQueue;

public class MessageHandler implements Runnable {

	private VectorClock clock;
    private String username;
    private int port;
    private int pid;
    private DatagramSocket socket;
    private static final int SERVER_PORT = 8000;
    private PriorityQueue<Message> messagePriorityQueue;
    private static final String SERVER = "_SERVER";	
    private static final int NO_PID = -1;
    
    private String REGISTRATION_MESSAGE = "Registering new user";

    public MessageHandler(String username, int port) {
        this.username = username;
        clock = new VectorClock();
        messagePriorityQueue = new PriorityQueue<Message>(new MessageComparator());
        pid = NO_PID;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

   
    public void sendMessage(Message message) {
        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getLocalHost();
            if (pid != NO_PID) {
                clock.tick(pid);
            }
            Message.sendMessage(message, socket, serverAddress, SERVER_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void sendChatMessage(String text) {
        Message chatMessage = new Message(
                MessageTypes.CHAT_MSG,
                username,
                pid,
                clock,
                text
        );
        sendMessage(chatMessage);
    }

 
    public void registerUser() {
        Message registrationMessage = new Message(
                MessageTypes.REGISTER,
                username,
                pid,
                clock,
                REGISTRATION_MESSAGE);
        sendMessage(registrationMessage);
    }

    @Override
    public void run() {
        while (true) {
            Message message = Message.receiveMessage(socket);
            if (message == null) {
                continue;
            }

            if (message.sender.equals(SERVER)) {
                handleServerMessage(message);
            } else {
                messagePriorityQueue.add(message);
                outputPendingMessages();
            }
        }
    }

    private void handleServerMessage(Message message) {
        switch (message.type) {
            case MessageTypes.ERROR:
                Client.terminate(message.message);
                break;
            case MessageTypes.ACK:
                pid = message.pid;
                System.out.println(message.sender + ": " + message.message);
                break;
        }
    }


    private void outputPendingMessages() {
        Message topMessage = messagePriorityQueue.peek();
        while (topMessage != null) {
            if (messageCanBeDisplayed(topMessage)) {
                displayMessage();
                topMessage = messagePriorityQueue.peek();
                //messagePriorityQueue.remove(topMessage);
            } else {
                topMessage = null;
            }
        }
    }

    private void displayMessage() {
        Message message = messagePriorityQueue.poll();
        clock.update(message.ts);
        Client.receiveMessage(message.sender + ": " + message.message);
    }

    private boolean messageCanBeDisplayed(Message message) {
        VectorClock messageClock = message.ts;
        int messagePid = message.pid;

        boolean receiverSeenAllMessages = true;
        for (String pid : message.getTs().getKnownPids()) {
            int integerPid = Integer.parseInt(pid);
            if (integerPid == messagePid) {
                continue;
            }

            if (messageClock.getTime(integerPid) > clock.getTime(integerPid)) {
            	receiverSeenAllMessages = false;
                break;
            }
        }

        boolean messagNextExpected = clock.getTime(messagePid) + 1 == messageClock.getTime(messagePid);

        return messagNextExpected && receiverSeenAllMessages;
    }

}
