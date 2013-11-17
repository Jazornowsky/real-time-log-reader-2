package net.damian.wojdat.talktalk_server;

import static net.damian.wojdat.talktalk_server.Commands.CMD_REQ_USR_NAME;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SET_ID;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_DISCONNECT;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_MSG;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_LOG_MSG;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_PING;
import static net.damian.wojdat.talktalk_server.Commands.CMD_USR_LST_UPDATE;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ClientThread extends ClientThreadBasic{
	
	private Socket socket = null;
	private ServerInterface server = null;
	private Integer id = null;
	String clientName = null;
	private ObjectOutputStream sockObjOut = null;
	private ObjectInputStream sockObjIn = null;
	private Timer connectionCheckTimer = null;
	private Integer noMessageFromClient = 0;
	final Integer timeOutTime = 5;
	private boolean pingResponse = true;
	Object obj = null;
	String str = null;
	
	public void setPingResponse(boolean response) {
		pingResponse = response;
	}
	
	public ClientThread(Socket socket, ServerInterface server) throws IOException {
		
		super(socket, server);
		
		this.server = server;		
		this.socket = socket;
		
		sockObjOut = new ObjectOutputStream(socket.getOutputStream());		
		sockObjOut.flush();
		sockObjIn = new ObjectInputStream(socket.getInputStream());
		
		server.sendMessage(-1, "[SERVER:] New client connected from " + socket.getInetAddress(), CMD_SRV_MSG);
		
		if(server.getClientCount() >= Server.maxClients) {
			sendMessage(CMD_SRV_LOG_MSG + " Maxim connections reached!");
			sendMessage(CMD_SRV_DISCONNECT + " ");	
			sockObjOut.close();
			sockObjIn.close();
			socket.close();
		}

		sendMessage(CMD_SRV_LOG_MSG + " Receiving ID...");
		
		for(Integer i = 0; i < Server.maxClients; i++) {
			if(server.containsClient(i)) {
				continue;
			} else {
				id = i;	
				
				server.addClient(id, this);

				sendMessage(CMD_SET_ID + " " + id);
				sendMessage(CMD_SRV_LOG_MSG + " ID received !");
				
				requestClientNameFromClient();
				
				break;
			}
		}
		
	}

	public void run() {
		
		ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();	
		
		worker.schedule(new Runnable() {
			@Override
			public void run() {
				setConnectionChecker();
			}}, 2, TimeUnit.SECONDS);
		
		try {
			while(!socket.isClosed()) {
				obj = sockObjIn.readObject();
				if(obj instanceof String && obj != null) {
					str = (String) obj;
					if(str == null) {
						break;
					}
					server.sendMessage(id, str);
				}
				
			}
		} catch (IOException e) {
			System.err.println("[SERVER:] Client socket input/output error");
		} catch (ClassNotFoundException e) {
			System.err.println("[SERVER:] Class not found exception " + e.getMessage());
		}
		
	}
	
	void disconnectUser() {
		
		if(connectionCheckTimer != null) {
			terminateConnectionChecker();
		}
		
		server.removeClient(id);
		
		try {
			this.closeSocket();
		} catch (IOException e) {
			System.err.println("[SERVER:] Error closing socket");
		}
		
	}
	
	@Override
	public void sendMessage(String message) {
		
		try {
			sockObjOut.writeObject(message);
			sockObjOut.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void sendObject(Object object) {
		try {
			sockObjOut.writeObject(object);
			sockObjOut.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void requestClientNameFromClient() {
		System.out.println("[DEBUG:] getUserName");
		sendMessage(CMD_REQ_USR_NAME + " ");
	}
	
	@Override
	public String getClientName() {
		return clientName;
	}
	
	@Override
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	@Override
	public void sendNewUserList() {
		
		System.out.println("[DEBUG:] sendNewUserList, id = " + id + ", users = " + server.getClientNames());
		sendMessage(CMD_USR_LST_UPDATE + " ");
		sendObject(server.getClientNames());
	}
	
	public void closeSocket() throws IOException {
		socket.close();
	}
	
	public Integer getClientId() {
		return id;
	}
	
	private void setConnectionChecker() {
		if(connectionCheckTimer != null) {
			connectionCheckTimer.cancel();
		}
		
		class connectionChecker extends TimerTask {
			@Override
			public void run() {
				if(pingResponse) {
					try {
						pingResponse = false;
						sockObjOut.writeObject(CMD_SRV_PING + " ");
						noMessageFromClient = 0;
					} catch (IOException e) {
						noMessageFromClient++;
						
						System.out.println("[SERVER:] No message received from client Id " + id + " for " + noMessageFromClient + " seconds");
						
						if(noMessageFromClient == timeOutTime) {
							this.cancel();
							disconnectUser();
						}
						else {
							try {
								sockObjOut.reset();
							} catch (IOException e1) {
								this.cancel();
								disconnectUser();
							}
						}
					}
				}
			}
		}
		
		connectionCheckTimer = new Timer();
		connectionCheckTimer.schedule(new connectionChecker(), 0, 2000);		
	}	
	
	private void terminateConnectionChecker() {
		connectionCheckTimer.cancel();
		connectionCheckTimer = null;
	}
	
}
