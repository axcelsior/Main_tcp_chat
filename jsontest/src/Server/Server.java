package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//
// Source file for the server side. 
//
// Created by Sanny Syberfeldt
// Maintained by Marcus Brohede
//

import java.net.*;
//import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import Shared.ChatMessage;

public class Server {

	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private ServerSocket m_socket;

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		try {
			Server instance = new Server(Integer.parseInt(args[0]));
			instance.listenForClientMessages();
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Server(int portNumber) {
		// TODO: create a socket, attach it to port based on portNumber, and
		// assign it to m_socket
		try {
			m_socket = new ServerSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		Socket connectionSocket = null;
		Connection connection = null;
		do {
			// TODO: Listen for client messages.
			// On reception of message, do the following:
			// * Unmarshal message
			// * Depending on message type, either
			// - Try to create a new ClientConnection using addClient(), send
			// response message to client detailing whether it was successful
			// - Broadcast the message to all connected users using broadcast()
			// - Send a private message to a user using sendPrivateMessage()

			try {
				connectionSocket = m_socket.accept(); // Listens for input from
														// client. Blocks until
														// message received...
				connection = new Connection(connectionSocket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} while (true);
	}

	public boolean addClient(String name, ObjectOutputStream oStream) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return false; // Already exists a client with this name
			}
		}
		m_connectedClients.add(new ClientConnection(name,oStream));
		return true;
	}

	public void sendPrivateMessage(ChatMessage message, String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.sendMessage(message);
			}else{
				System.out.println("Error! Name: "+ name + " not found.");
			}
		}
	}

	public void broadcast(ChatMessage message) {
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message);
		}
	}

	class Connection extends Thread {
		ObjectInputStream in;
		ObjectOutputStream out;
		Socket c_socket;
		ChatMessage recieved_message;

		public Connection(Socket aClientSocket) {
			c_socket = aClientSocket;
			try {
				in = new ObjectInputStream(c_socket.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				out = new ObjectOutputStream(c_socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.start();
		}

		public void run() {
			do {
				try {
					recieved_message = (ChatMessage) in.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("Class not found: " + e.getMessage());
				} catch (IOException e) {
					System.out.println("IO Exception: " + e.getMessage());
				}

				System.out.println(recieved_message.getCommand());
				if (recieved_message.getCommand().startsWith("/")) {
					/// COMMANDS
					if (recieved_message.getCommand().equals("/connect")) {
						String[] splitedMessage = recieved_message.getParameters().split(" ");
						String name = splitedMessage[0];
						addClient(name,out);
						System.out.println(name + " connected.");
						ChatMessage response = new ChatMessage("", "/connected", "");
						try {
							out.writeObject(response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (recieved_message.getCommand().equals("/test")){
						System.out.println("Test command works!");
						ChatMessage msg = new ChatMessage("Server", "", "You issued a test command!");
						sendPrivateMessage(msg,recieved_message.getSender());
					}
					if (recieved_message.getCommand().equals("/tell")){
						System.out.println(recieved_message.getParameters());
						String[] splitedMessage = recieved_message.getParameters().split("\\s+");
						String receiver = splitedMessage[1];
						splitedMessage[1] = "";
						String text = "[Private] from " + recieved_message.getSender() + ": " + String.join(" ", splitedMessage);
						String selfText = "[Private] to " + receiver + ": " + String.join(" ", splitedMessage);
						System.out.println("<>" + recieved_message.getSender() + "<>");
						System.out.println("<>" + receiver + "<>");
						ChatMessage msg = new ChatMessage(recieved_message.getSender(), "", text);
						ChatMessage selfMsg = new ChatMessage(recieved_message.getSender(), "", selfText);
						
						
						sendPrivateMessage(msg, receiver);
						sendPrivateMessage(selfMsg,recieved_message.getSender());
					}
					
					/// COMMANDS
				}
				
			} while (true);

		}

	}
}
