package simulation;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import messages.server.Server;
import simulation.xmpp.ConnectionListenerImpl;
import simulation.xmpp.PacketListenerImpl;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;


public class SimulationOrchestrator {
	private static final String RESOURCE = "cpswarm";
	private XMPPTCPConnection connection;
	private ConnectionListenerImpl connectionListener;
	//private RosterListener rosterListener;
	private String serverName = null;
	private Map<Jid, Server> simulationManagers = null;
	
	public static void main (String args[]) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		String serverURI = "";
		String serverName = "";
		String serverPassword = "";
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(SimulationOrchestrator.class.getResourceAsStream("/orchestrator.xml"));
			serverURI = document.getElementsByTagName("serverURI").item(0).getTextContent();
			serverName = document.getElementsByTagName("serverName").item(0).getTextContent();
			serverPassword = document.getElementsByTagName("serverPassword").item(0).getTextContent();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new SimulationOrchestrator(serverURI, serverName, serverPassword);
		while(true) {}
	}
	
	public SimulationOrchestrator(String serverIP, String serverName, String serverPassword) {
		this.serverName = serverName;
		this.simulationManagers = new HashMap<Jid, Server>();
		try {

			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, null, new SecureRandom());

			XMPPTCPConnectionConfiguration connectionConfig = XMPPTCPConnectionConfiguration
					.builder().setHost(serverIP).setPort(5222)
					.setXmppDomain(serverName)
					.setCompressionEnabled(false).setCustomSSLContext(sc)
					.setDebuggerEnabled(true).build();
			connection = new XMPPTCPConnection(connectionConfig);
			connection.connect();

			connection.login("orchestrator", serverPassword , Resourcepart.from(RESOURCE));
			System.out.println("Connected to server");

			connectionListener = new ConnectionListenerImpl(this);
			// Adds a listener for the status of the connection
			connection.addConnectionListener(connectionListener);

			
			
			//rosterListener = new RosterListenerImpl(this);
			// Adds a roster listener
			//addRosterListener(rosterListener);

			final StanzaFilter presenceFilter = new StanzaTypeFilter(
					Presence.class);
			System.out.println("adding the packet listener to the local connection");
			// This listener checks the presences
			final PacketListenerImpl packetListener = new PacketListenerImpl(
					this);
			this.addAsyncStanzaListener(packetListener, presenceFilter);
			
			// Does the login
			connection.login(serverName, serverPassword, Resourcepart.from(RESOURCE));
			Thread.sleep(2000);
			final Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("Pronto");
			try {
				connection.sendStanza(presence);
			} catch (final NotConnectedException | InterruptedException e) {
				e.printStackTrace();
			}
		} catch (final SmackException | IOException | XMPPException e) {
			if (e instanceof SASLErrorException) {
				connection.disconnect();
				createAccount(serverPassword);
			}
		} catch(Exception me) {
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
	}

    
    private boolean createAccount(final String password) {
		final AccountManager accountManager = AccountManager
				.getInstance(connection);
		final HashMap<String, String> props = new HashMap<String, String>();
		// The description will be the property name of the account
		props.put("name", "server");
		Localpart part;
		try {
			part = Localpart.from("orchestrator");
			connection.connect();
			accountManager.createAccount(part, password, props);
			connection.login("orchestrator", password, Resourcepart.from(RESOURCE));
		} catch (InterruptedException | SmackException | IOException | XMPPException me) {
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
			return false;
		}
		return true;
    }
    
    
	
	/**
	 * Method used to add a {@link PacketListener</code> to the connection
	 *
	 * @param listener
	 *            the listener that will receive the notification
	 *
	 * @return a <code>boolean</code>: true if all is ok, otherwise false
	 *
	 *
	 * @throws AsserionError
	 *             if something is wrong
	 *
	 *
	 */
	private boolean addAsyncStanzaListener(final StanzaListener listener,
			final StanzaFilter filter) {
		try {
			connection.addAsyncStanzaListener(listener, filter);
			return true;
			// The client is disconnected
		} catch (final IllegalStateException e) {
			System.out.println(
					"Connection disconnected, packet listener addition interrupted");
			return false;
		}
	}
    	
		
	public XMPPTCPConnection getConnection() {
		return connection;
	}
	
	public void putSimulationManager(Jid jid, Server server) {
		simulationManagers.put(jid,server);
	}	

	public void removeSimulationManager(Jid jid) {
		simulationManagers.remove(jid);
	}
}