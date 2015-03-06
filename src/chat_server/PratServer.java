package chat_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PratServer extends Thread {
	private ServerSocket serverSocket;
	private String id = null;
	private ArrayList<Message> pendingMessages;
	private ArrayList<Client> clients;

	public PratServer(int port) {
		pendingMessages = new ArrayList<Message>();
		clients = new ArrayList<Client>();
		try {
			serverSocket = new ServerSocket(port);
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void removeClient(Client client) {
		clients.remove(client);
		sendMessage(new Message(client.getUsername() + " disconnected"));
		sendUserlist();
		client = null; 
	}

	public void run() {
		System.out.println("Server running");
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				Client client = new Client(socket, this);
				id = client.waitForInitialMessage();
				addClient(client);
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	public void sendUserlist() {
		String[] clientList = new String[clients.size()];
		for (int i = 0; i < clients.size(); i++) {
			clientList[i] = clients.get(i).getUsername();
		}
		
		for (Client client : clients) {
			try {
				client.getConnection().getOutputStream().writeObject(clientList);
			} catch (IOException e) {
				System.out.println("Could not send user list to "
						+ client.getUsername());
				e.printStackTrace();
			}
		}
	}

	public void addClient(Client client) {
		ArrayList<Client> temp= new ArrayList<Client>();
		sendMessage(new Message(client.getUsername() + " connected at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())), clients);
		clients.add(client);
		sendUserlist();
		client.start();
		System.out.println("Client " + id + " connected");
		temp.add(client);
		sendMessage(new Message("Connected"),temp);
	}

	public void removeClient(String id) {
		boolean removed = false;
		for (Client client : clients) {
			if (client.getUsername() == id) {
				clients.remove(client);
				removed = true;
			} else {
			}
		}
		if (removed) {
			System.out.println("Client " + id + " removed from connections");
		} else {
			System.out.println("No client with the name " + id + " was found");
		}
	}

	public void sendMessage(Message m, ArrayList<Client> recipients) {
		for (Client recipient : recipients) {
			sendMessage(m, recipient);
		}
	}

	public void sendMessage(Message m, Client client) {
		try {
			client.send(m);
		} catch (SocketException ex) {
			pendingMessages.add(m);
			clients.remove(client);
		} catch (IOException ex) {
			pendingMessages.add(m);
			clients.remove(client);
		}
	}

	public void sendMessage(Message m) {
		sendMessage(m, clients);
	}
			
	
	public void extractRecipients(Message m) {
		if (m.all == true) {
			sendMessage(m, clients);
		} else {
			for (String recipent : m.getRecipients()) {
				for (Client client : clients) {
					if (recipent.equals(client.getUsername())) {
						sendMessage(m, client);
					}
				}
			}
		}
	}

	public Client findUser(String id) {
		for (Client client : clients) {
			if (client.getUsername() == id) {
				return client;
			}
		}
		System.out.println("No client with the name " + id + " was found");
		return null;
	}
}
