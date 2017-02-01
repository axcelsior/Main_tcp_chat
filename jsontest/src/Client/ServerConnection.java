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
		ObjectOutputStream oStream = null;
		ObjectInputStream iStream = null;
		ChatMessage msg = new ChatMessage("/connect", name);
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
			System.out.println("Connection established to server at " + m_socket.getInetAddress() +":" + m_socket.getPort() );
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

		// Update to return message contents
		return "";
	}

	public void sendChatMessage(String message) {
		Random generator = new Random();
		double failure = generator.nextDouble();

		if (failure > TRANSMISSION_FAILURE_RATE) {
			// TODO:
			// * marshal message if necessary
			// * send a chat message to the server
		} else {
			// Message got lost
		}
	}

}
