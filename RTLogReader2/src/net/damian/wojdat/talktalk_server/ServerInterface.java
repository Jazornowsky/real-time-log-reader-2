package net.damian.wojdat.talktalk_server;

import java.util.HashMap;
import java.util.Set;

public interface ServerInterface {
	final static Integer maxClients = 5;
	boolean addClient(Integer id, ClientThreadBasic clientThreadInterface);
	void removeClient(Integer id);
	void setClientName(Integer id, String name);
	boolean containsClient(Integer id);
	Integer getClientCount();
	ClientThreadBasic getClientInstance(Integer id);
	Set<Integer> getClientSet();
	HashMap<Integer, String> getClientNames();
	
	void closeSocket();
	ServerInterface getServerInstance();
	
	void sendMessage(Integer fromId, String message, String command);
	void sendMessage(Integer clientId, String message);
}
