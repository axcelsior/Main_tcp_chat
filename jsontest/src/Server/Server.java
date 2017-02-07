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
import java.util.ArrayList;
import java.util.Iterator;

import Shared.ChatMessage;

enum serverGroup {
	USER, ADMIN, MODERATOR
}

public class Server {

	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private ServerSocket m_socket;
	private String adminPassword = "password";
	private String modPassword = "password";

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
		m_connectedClients.add(new ClientConnection(name, oStream));
		return true;
	}

	public void sendPrivateMessage(ChatMessage message, String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.sendMessage(message);
			} else {
				System.out.println("Error! Name: " + name + " not found.");
			}
		}
	}

	public void promoteModerator(String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.setGroup(serverGroup.MODERATOR);
			} else {
				System.out.println("Error! Name: " + name + " not found.");
			}
		}
	}

	public void promoteAdmin(String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.setGroup(serverGroup.ADMIN);
			} else {
				System.out.println("Error! Name: " + name + " not found.");
			}
		}
	}

	public serverGroup getGroup(String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return c.getGroup();
			} else {
				System.out.println("Error! Name: " + name + " not found.");
			}
		}
		return serverGroup.USER;
	}

	public void broadcast(ChatMessage message) {
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message);
		}
	}

	public boolean kickUser(String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				m_connectedClients.remove(c);
				ChatMessage kickMessage = new ChatMessage("", "/kick", "");
				sendPrivateMessage(kickMessage, name);
				return true; // successful
			}
		}
		return false;
	}

	private String getList() {
		String returnValue = null;
		String list = "Connected clients: ";
		for (int i = 0; i < m_connectedClients.size(); i++) {
			list += m_connectedClients.get(i).getGroupName() + " " + m_connectedClients.get(i).getName();
			if (i <= m_connectedClients.size() - 2) {
				list += ", ";
			}
		}
		returnValue = list;
		return returnValue;
	}

	public boolean removeClient(String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				m_connectedClients.remove(c);
				return true; // successful
			}
		}
		return false;
	}

	class Connection extends Thread {
		ObjectInputStream in;
		ObjectOutputStream out;
		Socket c_socket;
		ChatMessage recieved_message;
		String senderName;

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
					senderName = recieved_message.getSender();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("Class not found: " + e.getMessage());
				} catch (IOException e) {
					// Disconnected
					removeClient(senderName);
					ChatMessage disconnectBroadcast = new ChatMessage(senderName, "",
							"[Server] User " + senderName + " lost connection. ; - (");
					broadcast(disconnectBroadcast);
					System.out.println(" HÄR IO Exception: " + e.getMessage());
					this.interrupt();
					return;
				}
				System.out.println(recieved_message.getCommand());
				if (recieved_message.getCommand() != null && recieved_message.getCommand().startsWith("/")) {
					/// COMMANDS
					if (recieved_message.getCommand().equals("/connect")) {
						String welcomeMessage = "[Server] Use /help for commands and more. ";
						String[] splitedMessage = recieved_message.getParameters().split(" ");
						String name = splitedMessage[0];
						if (addClient(name, out)) {
							System.out.println(name + " connected.");
							ChatMessage response = new ChatMessage("", "/connected", "");
							try {
								out.writeObject(response);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							ChatMessage broadcastMsg = new ChatMessage("", "",
									"[Server] User " + recieved_message.getSender() + " joined the chat!");
							broadcast(broadcastMsg);
							ChatMessage privateMsg = new ChatMessage("", "", welcomeMessage);
							sendPrivateMessage(privateMsg, recieved_message.getSender());
						} else {
							ChatMessage response = new ChatMessage("", "/disconnected", "");
							try {
								out.writeObject(response);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					} else if (recieved_message.getCommand().equals("/tell")) {
						String[] splitedMessage = recieved_message.getParameters().split("\\s+");
						if (splitedMessage.length > 1) {
							String receiver = splitedMessage[1];
							splitedMessage[1] = "";
							String text = "[Private] from " + recieved_message.getSender() + ": "
									+ String.join(" ", splitedMessage);
							String selfText = "[Private] to " + receiver + ": " + String.join(" ", splitedMessage);

							ChatMessage msg = new ChatMessage(recieved_message.getSender(), "", text);
							ChatMessage selfMsg = new ChatMessage(recieved_message.getSender(), "", selfText);

							sendPrivateMessage(msg, receiver);
							sendPrivateMessage(selfMsg, recieved_message.getSender());
						} else {
							ChatMessage ERROR = new ChatMessage(recieved_message.getSender(), "",
									"[Server] Incorrect usage of command. (Use /help tell)");
							sendPrivateMessage(ERROR, recieved_message.getSender());
						}
					} else if (recieved_message.getCommand().equals("/leave")) {
						ChatMessage broadcastMsg = new ChatMessage("", "",
								"[Server] User " + recieved_message.getSender() + " left the server. Bye "
										+ recieved_message.getSender() + "!");
						broadcast(broadcastMsg);
						kickUser(recieved_message.getSender());
						this.interrupt();
						break;
					} else if (recieved_message.getCommand().equals("/list")) {
						ChatMessage selfMsg = new ChatMessage(recieved_message.getSender(), "", getList());
						sendPrivateMessage(selfMsg, recieved_message.getSender());
					} else if (recieved_message.getCommand().equals("/login")) {
						System.out.println(recieved_message.getParameters());
						String[] parameters = recieved_message.getParameters().split(" ");
						if (parameters.length > 1) {
							System.out.println(parameters[1] + " : " + parameters[2]);
							if (parameters[2].equals("admin")) {
								if (parameters[3].equals(adminPassword)) {
									promoteAdmin(recieved_message.getSender());
									ChatMessage selfMsg = new ChatMessage(recieved_message.getSender(), "",
											"[Server] You have been promoted to Administrator.");
									sendPrivateMessage(selfMsg, recieved_message.getSender());
								}
							}
							if (parameters[2].equals("moderator")) {
								if (parameters[3].equals(modPassword)) {
									promoteModerator(recieved_message.getSender());
									ChatMessage selfMsg = new ChatMessage(recieved_message.getSender(), "",
											"[Server] You have been promoted to Moderator.");
									sendPrivateMessage(selfMsg, recieved_message.getSender());
								}
							}
						} else {
							ChatMessage ERROR = new ChatMessage(recieved_message.getSender(), "",
									"[Server] Incorrect usage of command. (Use /help login)");
							sendPrivateMessage(ERROR, recieved_message.getSender());
						}

					} else if (recieved_message.getCommand().equals("/kick")) {
						String target = null;
						String reason = null;
						String[] parameters = recieved_message.getParameters().split("\\s+");
						if (parameters.length > 0) {
							target = parameters[1];
							parameters[1] = "";
							reason = String.join(" ", parameters);
							if (getGroup(recieved_message.getSender()) == serverGroup.ADMIN
									|| getGroup(recieved_message.getSender()) == serverGroup.MODERATOR) {
								// tell target he got kicked
								// tell chat target got kicked
								// kick(target);
								ChatMessage broadcastMSG = new ChatMessage(recieved_message.getSender(), "",
										"[Server] User " + target + " got kicked for " + reason);
								broadcast(broadcastMSG);
							} else {
								// NO PERMISSION
								ChatMessage msg = new ChatMessage(recieved_message.getSender(), "",
										"[Server] You got no permission for that command...");
								sendPrivateMessage(msg, recieved_message.getSender());
							}
						} else {
							if (getGroup(recieved_message.getSender()) == serverGroup.ADMIN
									|| getGroup(recieved_message.getSender()) == serverGroup.MODERATOR) {
								ChatMessage ERROR = new ChatMessage(recieved_message.getSender(), "",
										"[Server] Incorrect usage of command. (Use /help kick)");
								sendPrivateMessage(ERROR, recieved_message.getSender());
							}else{
								// NO PERMISSION
								ChatMessage msg = new ChatMessage(recieved_message.getSender(), "",
										"[Server] You got no permission for that command...");
								sendPrivateMessage(msg, recieved_message.getSender());
							}
						}

					} else if (recieved_message.getCommand().equals("/help")) {
						String[] split = recieved_message.getParameters().split("\\s+");
						String parameter = String.join("", split);
						String outPut = null;
						String userHelp = "[Server] User Help \n" + "Commands: \n" + "		/tell \n"
								+ "		/list \n" + "		/leave \n" + "		/login \n" + "\n"
								+ "For more help about commands type: /help [command] ex /help tell \n"
								+ "---------------------------------------------------------------- \n"
								+ "To send broadcasts simply send messages in the chat without a '/'  \n"
								+ "---------------------------------------------------------------- \n";
						String adminHelp = "[Server] Admin Help \n" + "Commands: \n" + "		/kick \n"
								+ "---------------------------------------------------------------- \n"
								+ "For more help about commands type: /help [command] ex /help kick \n"
								+ "---------------------------------------------------------------- \n";
						String modHelp = "[Server] Mod Help \n" + "Commands: \n" + "		/kick \n"
								+ "---------------------------------------------------------------- \n"
								+ "For more help about commands type: /help [command] ex /help kick \n"
								+ "---------------------------------------------------------------- \n ";
						String _tell = "[Help] Usage: /tell [user] <message> \n" + "  - Sends a message to a user.";
						String _list = "[Help] Usage: /list \n" + "  - Lists connected users.";
						String _leave = "[Help] Usage: /leave \n" + "  - Leaves the chatroom.";
						String _login = "[Help] Usage: /login [role] <password> \n"
								+ "  - Logs in and gives priveledges. Roles: admin,moderator .";
						String _help = "[Help] Usage: /help <command> \n"
								+ "  - /help without parameters gives a list of commands and basic info. \n"
								+ "  - /help <command> ex /help leave gives info about how to use a certain command.";
						String _kick = "[Help] Usage: /kick [user] <reason> \n"
								+ "  - Kicks user and broadcasts with given reason. \n" + " \n";
						// ADMIN EXCLUSIVE HELP
						if (getGroup(recieved_message.getSender()) == serverGroup.ADMIN) {
							if (parameter.equals("")) {
								outPut = userHelp + adminHelp;

							} else {
								if (parameter.equals("kick")) {
									outPut = _kick;
								} else {
									ChatMessage msg = new ChatMessage(recieved_message.getSender(), "",
											"[Server] Unknown command... (/help for help)");
									sendPrivateMessage(msg, recieved_message.getSender());
								}
							}
						} else if (getGroup(recieved_message.getSender()) == serverGroup.MODERATOR) {
							if (parameter.equals("")) {
								outPut = userHelp + modHelp;
							} else {
								if (parameter.equals("kick")) {
									outPut = _kick;
								} else {
									ChatMessage msg = new ChatMessage(recieved_message.getSender(), "",
											"[Server] Unknown command... (/help for help)");
									sendPrivateMessage(msg, recieved_message.getSender());
								}
							}
						} else {
							if (parameter.equals("")) {
								outPut = userHelp;
							} else {
								if (parameter.equals("tell")) {
									outPut = _tell;
								} else if (parameter.equals("list")) {
									outPut = _list;
								} else if (parameter.equals("leave")) {
									outPut = _leave;
								} else if (parameter.equals("login")) {
									outPut = _login;
								} else if (parameter.equals("kick")) {
									outPut = "[Server] You dont have permission for that command...";
								} else {
									ChatMessage msg = new ChatMessage(recieved_message.getSender(), "",
											"[Server] Unknown command... (Use /help)");
									sendPrivateMessage(msg, recieved_message.getSender());
								}
							}
						}
						ChatMessage msg = new ChatMessage(recieved_message.getSender(), "", outPut);
						sendPrivateMessage(msg, recieved_message.getSender());
					} else {
						ChatMessage msg = new ChatMessage(recieved_message.getSender(), "",
								"[Server] Unknown command... (Use /help)");
						sendPrivateMessage(msg, recieved_message.getSender());
					}

					/// COMMANDS
				} else {
					ChatMessage broadcastMsg = new ChatMessage(recieved_message.getSender(), "",
							recieved_message.getSender() + ": " + recieved_message.getParameters());
					broadcast(broadcastMsg);
				}

			} while (true);

		}

	}
}
