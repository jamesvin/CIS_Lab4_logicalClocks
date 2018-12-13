package edu.gvsu.cis.cis656.client;

public class Client {
	
	 private static MessageHandler messageHandler;
	 private static OutputWriter writer;
	 private static String username;
	 private static int portNo;


	    public static void main(String args[]) {

	        if (args.length == 0) {
	           // printHelp();
	            System.exit(1);
	        }

	        username = args[0];
	        portNo = Integer.parseInt(args[1]);

	        messageHandler = new MessageHandler(username, portNo);
	        messageHandler.registerUser();
	        Thread messageListener = new Thread(messageHandler);
	        messageListener.start();
	        
	       
	        
	        writer = new OutputWriter(username);
	        Thread inputThread = new Thread(new InputReader());
	        inputThread.start();

	        output("Welcome to the chat, all messages you send will be broadcased");

	    }
	   
	  
	    public static void output(String message) {
	        writer.addEvent(message);
	    }

	   
	    public static void receiveMessage(String messageText) {
	        output(messageText);
	    }

	  
	    public static void sendMessage(String messageText) {
	        output("You: " + messageText);
	        messageHandler.sendChatMessage(messageText);
	    }


	    public static void terminate(String message) {
	        System.out.println(message);
	        System.exit(1);
	    }
	
}
