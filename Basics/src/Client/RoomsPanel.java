package Client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

public class RoomsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<RoomListItem> rooms = new ArrayList<RoomListItem>();
	JPanel container;
	JFrame parent;

	public RoomsPanel(JFrame frame) {
		parent = frame;
		container = new JPanel();
		JScrollPane scroll = new JScrollPane(container);
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setAlignmentY(TOP_ALIGNMENT);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		// this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();

		JLabel searchLabel = new JLabel("Search");
		JTextField search = new JTextField();

		search.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendAction");
		search.getActionMap().put("sendAction", new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				String query = search.getText();
				// only search if the string isn't empty
				if (!query.isEmpty()) {
					removeAllRooms();
					SocketClient.INSTANCE.sendGetRooms(query);
				}
			}
		});
		Dimension d = new Dimension(this.getSize().width, 40);
		search.setPreferredSize(d);
		search.setMaximumSize(d);
		// create the "create" label and input
		JLabel createLabel = new JLabel("Create");
		JTextField create = new JTextField();
		// trigger it on enter key
		create.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendAction");
		create.getActionMap().put("sendAction", new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				String query = create.getText();
				// send the request only if a value was entered
				if (!query.isEmpty()) {
					SocketClient.INSTANCE.sendCreateRoom(query);
				}
			}
		});
		//

		JButton back = new JButton("Go Back");

		//
		back.setPreferredSize(d);
		back.setMaximumSize(d);
		back.setSize(d);
		//

		back.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				((ClientUI) parent).previous();
			}
		});
		// this.add(back);
		// this.add(container);

		//
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BorderLayout());
		searchPanel.add(searchLabel, BorderLayout.NORTH);
		searchPanel.add(search, BorderLayout.CENTER);
		tabbedPane.add(searchPanel, "Search");
		JPanel createPanel = new JPanel();
		createPanel.setLayout(new BorderLayout());
		createPanel.add(createLabel, BorderLayout.NORTH);
		createPanel.add(create, BorderLayout.CENTER);
		tabbedPane.add(createPanel, "Create");
		// this.add(searchPanel, BorderLayout.NORTH);
		this.add(tabbedPane, BorderLayout.NORTH);
		this.add(back, BorderLayout.SOUTH);
		this.add(container, BorderLayout.CENTER);
		//

	}

	public void addRoom(String room) {
		if (room != null) {
			System.out.println("Adding: " + room);
			RoomListItem r = new RoomListItem(room, (String roomName) -> handleSelection(roomName));
			Dimension size = new Dimension(this.getSize().width, 40);
			r.setPreferredSize(size);
			r.setMaximumSize(size);
			r.setMinimumSize(size);
			container.add(r);
			rooms.add(r);
		}
	}

	public void removeRoom(String room) {
		Iterator<RoomListItem> iter = rooms.iterator();
		while (iter.hasNext()) {
			RoomListItem r = iter.next();
			if (r.getRoomName().equalsIgnoreCase(room)) {
				r.close();
				r.removeAll();
				container.remove(r);
				iter.remove();
				break;
			}
		}
		parent.invalidate();
		parent.repaint();
	}

	public void removeAllRooms() {
		System.out.println("Clearing rooms");
		Iterator<RoomListItem> iter = rooms.iterator();
		while (iter.hasNext()) {
			RoomListItem r = iter.next();
			System.out.println("Removing " + r.getRoomName());
			container.remove(r);
			r.close();
			iter.remove();
		}
		parent.invalidate();
		parent.repaint();
	}

	public void handleSelection(String room) {
		SocketClient.INSTANCE.sendJoinRoom(room);
	}

}

class RoomListItem extends JPanel implements AutoCloseable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8340982098822138336L;
	private JTextField roomName;
	private JButton joinButton;

	/**
	 * We pass the room name, and a callback for when the room is selected
	 * 
	 * @param room   - Name of room to show on the UI
	 * @param onJoin - Callback to trigger when button is clicked
	 */

	public RoomListItem(String room, Consumer<String> onJoin) {
		// TODO see below links regarding Consumer
		// https://medium.com/swlh/understanding-java-8s-consumer-supplier-predicate-and-function-c1889b9423d
		// https://mkyong.com/java8/java-8-consumer-examples/
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		roomName = new JTextField(room);
		roomName.setEditable(false);
		joinButton = new JButton("Join");
		joinButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// invokes the callback
				onJoin.accept(roomName.getText());
			}
		});
		this.add(roomName);
		this.add(joinButton);
	}

	@Override
	public void close() {
		for (ActionListener al : joinButton.getActionListeners()) {
			joinButton.removeActionListener(al);
		}
		this.removeAll();
	}

	public String getRoomName() {
		return roomName.getText();
	}

}
