/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

import Shared.ChatMessage;

/**
 *
 * @author brom
 */

public class ServerConnection {

	// Artificial failure rate of 30% packet loss
	static double TRANSMISSION_FAILURE_RATE = 0.3;

	private Socket m_socket = null;
	private InetAddress m_serverAddress = null;
	private int m_serverPort = -1;
	private ObjectOutputStream oStream = null;
	private ObjectInputStream iStream = null;
	boolean m_connected = false;
	private String m_name = null;

	public ServerConnection(String hostName, int port) {
		m_serverPort = port;

		// TODO:
		// * get address of host based on parameters and assign it to
		// m_serverAddress
		// * set up socket and assign it to m_socket
		try {
			m_socket = new Socket(hostName, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean handshake(String name) {
		// TODO:
		// * marshal connection message containing user name
		// * send message via socket
		// * receive response message from server
		// * unmarshal response message to determine whether connection was
		// successful
		// * return false if connection failed (e.g., if user name was taken)
		m_name = name;
		ChatMessage msg = new ChatMessage(name, "/connect", name);
		ChatMessage response = null;
		try {
			oStream = new ObjectOutputStream(m_socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			iStream = new ObjectInputStream(m_socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			oStream.writeObject(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			response = (ChatMessage) iStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (response.getCommand().equals("/connected")) {
			System.out.println(
					"Connection established to server at " + m_socket.getInetAddress() + ":" + m_socket.getPort());
			m_connected = true;
			return true;
		} else
			return true;
	}

	public String receiveChatMessage() {
		// TODO:
		// * receive message from server
		// * unmarshal message if necessary

		// Note that the main thread can block on receive here without
		// problems, since the GUI runs in a separate thread
		ChatMessage recieved_message = null;
		String outPut = null;
		try {
			System.out.println("Client waiting for server messages...");
			recieved_message = (ChatMessage) iStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Class not found: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO Exception: " + e.getMessage());
		}
		System.out.println("Message recieved.");
		String sender = recieved_message.getSender();
		String command = recieved_message.getCommand();
		String message = recieved_message.getParameters();

		if (command.startsWith("/")) {
			// Its a command
			if (command.equals("/kick")){
				disconnect();
			}
		} else {
			outPut = message;
			System.out.println(message);
		}
		// Update to return message contents
		return outPut;
	}
	public void disconnect(){
		m_connected = false;
		try {
			oStream.close();
			iStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void sendChatMessage(String message) {
		ChatMessage msg = null;
		int emptyEntries = 1;
		String[] splited = message.split("\\s+");
		String sender = splited[0];
		String command = null;
		splited[0] = "";
		if (splited[1].startsWith("/")) {
			command = splited[1];
			splited[1] = "";
		}
		String outPut = String.join(" ", splited);
		System.out.println("<>" + outPut + "<>");
		if (splited.length > 1) {

		}

		if (command.equals("/connect")) {
			if (!m_connected) {
				handshake(m_name);
			}
		} else {
			msg = new ChatMessage(sender, command, outPut);

			try {
				oStream.writeObject(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
