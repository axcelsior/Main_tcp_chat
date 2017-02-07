/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;

import Shared.ChatMessage;

/**
 * 
 * @author brom
 */
public class ClientConnection {

	static double TRANSMISSION_FAILURE_RATE = 0.3;

	private final String m_name;
	private ObjectOutputStream m_out = null;
	private serverGroup group;
	

	/*
	 * private final InetAddress m_address; private final int m_port;
	 */
	public ClientConnection(String name, ObjectOutputStream out) {
		m_name = name;
		m_out = out;
		this.group = serverGroup.USER;
	}
	public void setGroup(serverGroup grp){
		this.group = grp;
	}

	public void sendMessage(ChatMessage message) {
		System.out.println("Sending message to " + m_out);
		try {
			m_out.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean hasName(String testName) {
		return testName.equals(m_name);
	}
	public serverGroup getGroup(){
		return group;
	}
	public String getGroupName(){
		switch (group){
			case USER:
				return "[U]";
			case ADMIN:
				return "[Admin]";
			case MODERATOR:
				return "[Mod]";
		}
		return "NULL";
	}
	public String getName() {
		return m_name;
	}

}
