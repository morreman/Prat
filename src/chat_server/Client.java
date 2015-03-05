package chat_server;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {
	private Connection connection;
	private String username;
	private ArrayList<Message> messages;
	private PratServer controller;
	
	public Client(Socket socket, ArrayList<Message> messages, PratServer controller) {
		this.controller = controller;
		this.messages = messages;
		this.connection = new Connection(socket);
	}
	
	public String waitForInitialMessage() {
		do {
			try {
				username = connection.getInputStream().readUTF();
			} catch (IOException ex) {}
		} while (username == null);
		return username;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String id) {
		this.username = id;
	}
	
	public void run() {
		while(!Thread.interrupted()) {
			recieve();
		}
	}
	
	public void recieve() {
		try {
			Object obj = connection.getInputStream().readObject();
			if (obj instanceof Message) {
				Message m = (Message) obj;
				messages.add(m);
				controller.sendMessage(m);
				// Temp code below
				System.out.println(m.toString());
			}
		} catch (EOFException e) {
			controller.removeClient(this);
			this.interrupt();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}

	public void send(Message m) throws IOException {
			connection.getOutputStream().writeObject(m);
			connection.getOutputStream().flush();
	}
}
