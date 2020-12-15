package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Room implements AutoCloseable {
	private static SocketServer server;// used to refer to accessible server functions
	private String name;
	private final static Logger log = Logger.getLogger(Room.class.getName());

	// Commands
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private final static String FLIP = "flip";
	private final static String ROLL = "roll";
	private final static String MUTE = "mute";
	private final static String UNMUTE = "unmute";
	private final static String PM = "pm";
	private List<ClientPlayer> clients = new ArrayList<ClientPlayer>();

	public Room(String name) {
		this.name = name;
	}

	public static void setServer(SocketServer server) {
		Room.server = server;
	}

	public String getName() {
		return name;
	}

	protected synchronized void addClient(ServerThread client) {
		//
		client.setCurrentRoom(this);
		boolean exists = false;

		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			if (c.client == client) {
				exists = true;
				break;
			}
		}

		if (exists) {
			log.log(Level.INFO, "Attempting to add a client that already exists");
		} else {
			// create a player reference for this client
			// so server can determine position
			// add Player and Client reference to ClientPlayer object reference
			ClientPlayer cp = new ClientPlayer(client);
			clients.add(cp);// this is a "merged" list of Clients (ServerThread) and Players (Player)
			// objects
			// that's so we don't have to keep track of the same client in two different
			// list locations
			syncClient(cp);

		}
	}

	//
	private void syncClient(ClientPlayer cp) {
		if (cp.client.getClientName() != null) {
			cp.client.sendClearList();
			sendConnectionStatus(cp.client, true, "joined the room " + getName());
			// get the list of connected clients (for ui panel)
			updateClientList(cp.client);
		}
	}
	//

	private void updateClientList(ServerThread client) {
		//
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			if (c.client != client) {
				client.sendConnectionStatus(c.client.getClientName(), true, null);
			}
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		//
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			if (c.client == client) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.client.getClientName() + " from " + getName());
			}
		}
		if (clients.size() > 0) {
			sendConnectionStatus(client, false, "left the room " + getName());
		} else {
			cleanupEmptyRoom();
		}
	}

	private void cleanupEmptyRoom() {
		// If name is null it's already been closed. And don't close the Lobby
		if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
			return;
		}
		try {
			log.log(Level.INFO, "Closing empty room: " + name);
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void joinRoom(String room, ServerThread client) {
		server.joinRoom(room, client);
	}

	protected void joinLobby(ServerThread client) {
		server.joinLobby(client);
	}

	protected void createRoom(String room, ServerThread client) {
		if (server.createNewRoom(room)) {
			sendMessage(client, "Created a new room");
			joinRoom(room, client);
		}
	}

	/***
	 * Helper function to process messages to trigger different functionality.
	 * 
	 * @param message The original message being sent
	 * @param client  The sender of the message (since they'll be the ones
	 *                triggering the actions)
	 */
	private String processCommands(String message, ServerThread client) {
		String response = message;
		boolean msg = true;
		try {
			if (message.indexOf(COMMAND_TRIGGER) > -1) {
				String[] comm = message.split(COMMAND_TRIGGER);
				log.log(Level.INFO, message);
				String part1 = comm[1];
				String[] comm2 = part1.split(" ");
				String command = comm2[0];
				if (command != null) {
					command = command.toLowerCase();
				}
				String roomName;
				switch (command) {
				case CREATE_ROOM:
					roomName = comm2[1];
					if (server.createNewRoom(roomName)) {
						joinRoom(roomName, client);
					}
					response = null;
					break;
				case JOIN_ROOM:
					roomName = comm2[1];
					joinRoom(roomName, client);
					response = null;
					break;
				case FLIP:
					response = server.flipCoin();
					break;
				case ROLL:
					String part2 = comm2[1];
					String[] dice = part2.split("d");
					int num = Integer.parseInt(dice[0]);
					int val = Integer.parseInt(dice[1]);
					response = server.rollDice(num, val);
					break;
				case MUTE:
					msg = false;
					String mutePerson = comm2[1];
					client.mute(mutePerson);
					List<String> Mute = new ArrayList<String>();
					Mute.add(mutePerson);
					sendPrivateMessage(client, Mute, "You have been muted");
					response = null;
					break;
				case UNMUTE:
					msg = false;
					String unmutePerson = comm2[1];
					client.unmute(unmutePerson);
					List<String> unMute = new ArrayList<String>();
					unMute.add(unmutePerson);
					sendPrivateMessage(client, unMute, "You have been unmuted");
					response = null;
					break;
				case PM:
					msg = false;
					String[] tmp1 = part1.split("@");
					String placeHolder = tmp1[1];
					String[] tmp = placeHolder.split(" ", 2);
					String clientName = tmp[0];
					message = tmp[1];
					clientName = clientName.trim().toLowerCase();
					List<String> clients = new ArrayList<String>();
					clients.add(clientName);
					sendPrivateMessage(client, clients, message);
					for (int i = 1; i == 1; i++) {
						List<String> temp = new ArrayList<String>();
						String me = client.getClientName();
						temp.add(me);
						sendPrivateMessage(client, temp, message);
					}
					response = null;
					break;
				}
			} else {

				if (msg) {
					response = message;
					String tempMsg = message;

					if (true) {

						tempMsg = tempMsg.replaceAll("\\[b", "<b>");
						tempMsg = tempMsg.replaceAll("b\\]", "</b>");

						// Italics

						tempMsg = tempMsg.replaceAll("\\[i", "<i>");
						tempMsg = tempMsg.replaceAll("i\\]", "</i>");

						tempMsg = tempMsg.replaceAll("\\[u", "<u>");
						tempMsg = tempMsg.replaceAll("u\\]", "</u>");

						tempMsg = tempMsg.replaceAll("\\[r", "<FONT COLOR=red>");
						tempMsg = tempMsg.replaceAll("r\\]", "</FONT>");
					}

					response = tempMsg;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
		//
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			boolean messageSent = c.client.sendConnectionStatus(client.getClientName(), isConnect, message);
			if (!messageSent) {
				iter.remove();
			}
		}
	}

	/***
	 * Takes a sender and a message and broadcasts the message to all clients in
	 * this room. Client is mostly passed for command purposes but we can also use
	 * it to extract other client info.
	 * 
	 * @param sender  The client sending the message
	 * @param message The message to broadcast inside the room
	 */
	protected void sendMessage(ServerThread sender, String message) {
		//
		log.log(Level.INFO, getName() + ": Sending message to " + clients.size() + " clients");
		String resp = processCommands(message, sender);
		if (resp == null) {
			// it was a command, don't broadcast
			return;
		}
		message = resp;
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			if (!client.client.isMuted(sender.getClientName())) {
				boolean messageSent = client.client.send(sender.getClientName(), message);
				if (!messageSent) {
					iter.remove();
				}
			}
			// System.out.println("here@@@@@@@@@@@@@@@@@@@@@");
		}
	}

	protected void sendPrivateMessage(ServerThread sender, List<String> dest, String message) {
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			if (dest.contains(client.client.getClientName().toLowerCase())) {
				boolean messageSent = client.client.send(sender.getClientName(), message);
				if (!messageSent) {
					iter.remove();
				}
				break;
			}

		}
		// System.out.println("here!!!!!!!!!!!!!!!!!!!!");
	}

	public List<String> getRooms(String search) {
		return server.getRooms(search);
	}

	/***
	 * Will attempt to migrate any remaining clients to the Lobby room. Will then
	 * set references to null and should be eligible for garbage collection
	 */
	@Override
	public void close() throws Exception {
		int clientCount = clients.size();
		if (clientCount > 0) {
			log.log(Level.INFO, "Migrating " + clients.size() + " to Lobby");
			Iterator<ClientPlayer> iter = clients.iterator();
			Room lobby = server.getLobby();
			while (iter.hasNext()) {
				ClientPlayer client = iter.next();
				lobby.addClient(client.client);
				iter.remove();
			}
			log.log(Level.INFO, "Done Migrating " + clients.size() + " to Lobby");
		}
		server.cleanupRoom(this);
		name = null;
		// isRunning = false;
	}

}