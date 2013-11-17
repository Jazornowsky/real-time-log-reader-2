package net.damian.wojdat.talktalk_server;

import static net.damian.wojdat.talktalk_server.Commands.CMD_SET_ID;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_MSG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

public class Server implements ServerInterface {
	
	public HashMap<Integer, ClientThreadBasic> clientsList = new HashMap<Integer, ClientThreadBasic>();
	public HashMap<Integer, String> clientsNames = new HashMap<Integer, String>();
	public Messages messagesService = null;
	boolean listening = true;
	ServerSocket serverSocket = null;
	
	class History {
		private String path = null;
		private File fileHandle = null;
		private HashMap<Long, String> entries = null;
		public FileInputStream fileInputStream = null;
		public FileOutputStream fileOutputStream = null;
		
		{
			path = "history.dat";
			entries = new HashMap<Long, String>();
		}
		
		public History() {
			fileHandle = new File(path);
			if(!fileHandle.exists()) {
				try {
					fileHandle.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			if(!fileHandle.canRead() || !fileHandle.canWrite()) {
				System.err.println("Can't read/write history.dat");
				System.exit(0);
			}
			try {
				fileInputStream = new FileInputStream(fileHandle);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
			}
			try {
				fileOutputStream = new FileOutputStream(fileHandle);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		//public writeEntry
	}
	
	public static void main(String[] args) {
		ServerInterface server = new Server();
		LoggerCore loggerCore = new LoggerCore(server);
		loggerCore.start();
		((Server) server).startListening();
	}
	
	public Server() {
		System.out.println("[SERVER:] TalkTalk server launching...");
		
		try {
	    	serverSocket = new ServerSocket(4444);
	    } catch (IOException e) {
	    	System.err.println("[SERVER:] " + e.getMessage());
	        System.exit(1);
	    }
	
		System.out.println("[SERVER:] Binded to port " + serverSocket.getLocalPort());
		
		System.out.println("[SERVER:] Init Message Service...");
		
		messagesService = new Messages(this);
	}

	public void startListening() {
		System.out.println("[SERVER:] Listening for incomming connections...");
		
		Socket socket;
		
	    while(listening) {
    		try {
				socket = serverSocket.accept();
				new ClientThread(socket, this).start();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("[SERVER:] " + e.getMessage());
			}
			
	    }
	}
	
	@Override
	public void closeSocket() {
		listening = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("[SERVER:] Exception on serverSocket.close()");
			System.err.println("[SERVER:] " + e.getMessage());
		}
	}
	
	@Override
	public Integer getClientCount() {
		return clientsList.size();
	}
	
	@Override
	public boolean addClient(Integer id, ClientThreadBasic clientThreadBasic) {
		if(!clientsList.containsKey(id)) {
			clientsList.put(id, clientThreadBasic);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void setClientName(Integer id, String name) {
		clientsNames.put(id, name);
	}
	
	@Override
	public void removeClient(Integer id) {
		
		if(clientsList.containsKey(id))
		{
			clientsList.remove(id);
			if(clientsNames.containsKey(id)) {
				clientsNames.remove(id);
			}
		}
		
		for(Integer key: getClientSet()) {
			if(key != id) {
				getClientInstance(key).sendNewUserList();
			}
		}
		
		sendMessage(-1, "[SERVER:] Client [" + id + "] disconnected from the server", CMD_SRV_MSG);
		System.out.println("[SERVER:] Client [" + id + "] disconnected from the server");
	}
	
	@Override
	public boolean containsClient(Integer id) {
		return clientsList.containsKey(id);
	}

	@Override
	public ServerInterface getServerInstance() {
		return this;
	}

	@Override
	public ClientThreadBasic getClientInstance(Integer id) {
		return clientsList.get(id);
	}

	@Override
	public Set<Integer> getClientSet() {
		return clientsList.keySet();
	}

	@Override
	public HashMap<Integer, String> getClientNames() {
		return clientsNames;
	}

	@Override
	public void sendMessage(Integer fromId, String message, String command) {
		messagesService.sendMessage(fromId, message, command);
		
	}

	@Override
	public void sendMessage(Integer clientId, String message) {
		messagesService.sendMessage(clientId, message);		
	}

}
