package net.damian.wojdat.talktalk_server;

import java.net.Socket;

public abstract class ClientThreadBasic extends Thread{
	
	public ClientThreadBasic(Socket socket, ServerInterface server) {
	}
	
	abstract void requestClientNameFromClient();
	abstract void setClientName(String clientName);
	abstract String getClientName();
	abstract void disconnectUser();
	abstract void sendMessage(String message);
	abstract void sendObject(Object object);
	abstract void sendNewUserList();
	abstract void setPingResponse(boolean response);
}