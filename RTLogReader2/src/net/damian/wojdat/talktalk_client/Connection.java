package net.damian.wojdat.talktalk_client;

import static net.damian.wojdat.talktalk_server.Commands.CMD_CLT_PING;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Connection {

	Socket echoSocket = null;
    ObjectOutputStream sockObjOut = null;
    ObjectInputStream sockObjIn = null;
    String userInput;
    private String host = "localhost";
    private Integer port = 4444;
    Integer providedId = null;
    IncommingMessagesThread incommingMessagesThread = null;
    ScheduledExecutorService reconnectWorker = null;
    Boolean reconnectWorkerWorking = false;
	Timer connectionCheckTimer = null;
	
	public void checkConnection() throws IOException {
		sockObjOut.writeObject(CMD_CLT_PING + " ");
	}	

	public void setConnectionChecker() {
		
		class CheckConnectionTimerTask extends TimerTask {
			@Override
			public void run() {
				try {
					// sendNewInfo("Client ping");
					checkConnection();
				} catch (IOException e) {
					connect();
				}
			}
		}
		
		connectionCheckTimer = new Timer();
		connectionCheckTimer.scheduleAtFixedRate(new CheckConnectionTimerTask(), 2000, 2000);
	}

	public Boolean isReconnectWorkerWorking() {
		if(reconnectWorker != null) {
			if(reconnectWorker.isShutdown()) {
				return false;	
			}
			else {
				return true;
			}
		}
		else {
			return false;
		}
	}
	
	public void shutdownReconnectWorker() {
		if(isReconnectWorkerWorking()) {
			reconnectWorker.shutdownNow();
		}
	}
	
	public void resetConnection() {
		
		if(isReconnectWorkerWorking()) {
			return;
		}
		
		sendNewInfo("Reconnecting in 5 sec.");		
		reconnectWorker = Executors.newSingleThreadScheduledExecutor();
		reconnectWorker.schedule(new Runnable() {
			@Override
			public void run() {
				connect();
			}
		}, 5000, TimeUnit.MILLISECONDS);
		
	}
	
	/*public void resetIncommingMessagesThread() throws InterruptedException {
		
		if(incommingMessagesThread.isAlive()) {
        	incommingMessagesThread.join();
        }
		
		incommingMessagesThread = new IncommingMessagesThread(this, windowController);
        incommingMessagesThread.start();  
		
	}*/
	
	protected abstract void sendNewInfo(String message);
	
	abstract void preConnect();
	
	public void connect() {
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				if(isReconnectWorkerWorking()) {
					reconnectWorker.shutdownNow();
				}
				
				preConnect();
				
				sendNewInfo("Connecting to: " + host + ":" + port);
				
				if(echoSocket == null) {					
					echoSocket = new Socket();					
				}
				else {					
					disconnect();
					echoSocket = new Socket();				
				}
				try {					
					echoSocket.connect(new InetSocketAddress(host, port), 5000);					
				} catch (UnknownHostException e) {
					
					sendNewInfo("Don't know about host: " + host + ":" + port);			
					resetConnection();
		        	
		        } catch (IOException e) {   
		        	
					sendNewInfo("Couldn't get I/O for the connection to: " + host + ":" + port);        	
					resetConnection();
		        	
		        }
				
				if(!echoSocket.isConnected()) {
					resetConnection();
					return;
				}

				sendNewInfo("Connected");
				
				try {
					
					sockObjOut = new ObjectOutputStream(echoSocket.getOutputStream());
					sockObjOut.flush();
					sockObjIn = new ObjectInputStream(echoSocket.getInputStream());
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				setConnectionChecker();
				
				postConnect();
				
			}
			
		}).start();
		
		//WindowController.getSendTextArea().setDisable(false);
		//WindowController.getSubmitButton().setDisable(false);
	}
	
	abstract void postConnect();
	
	abstract void postDisconnect();
	
	public void disconnect() {
		
		//windowController.getSendTextArea().setDisable(true);
		//windowController.getSubmitButton().setDisable(true);
		
		shutdownReconnectWorker();
    	
		if(connectionCheckTimer != null) {
			connectionCheckTimer.cancel();			
		}
		
		if(!echoSocket.isClosed()) {
			preDisconnect();
			
			/*if(sockObjIn != null) {
    			try {
					sockObjIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}*/
			if(sockObjOut != null) {
				writeObject(Commands.CMD_CLT_DISCONNECT + " ");
    			/*try {
					sockObjOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
			try {
				echoSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			postDisconnect();
		}
        	
		//windowController.updateUsersList(new HashMap<Integer, String>());  
		sendNewInfo("Disconnected from the server");
    	
	}
	
	abstract void preDisconnect();
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setPort(Integer port) {
		this.port = port;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public Integer getUserId() {
		return providedId;
	}
	
	public Boolean isBound() {
		return echoSocket.isBound();
	}
	
	public Boolean isClosed() {
		return echoSocket.isClosed();
	}

	public Boolean isConnected() {
		return echoSocket.isConnected();
	}
	
	public Boolean isInputShutdown() {
		return echoSocket.isInputShutdown();
	}
	
	public Boolean isOutputShutdown() {
		return echoSocket.isOutputShutdown();
	}
	
	public void writeObject(Object obj) {
		try {
			if(sockObjOut != null) {
				sockObjOut.writeObject(obj);
			}
		} catch (IOException e) {
			//WindowController.putFormattedLog(e.toString());
			e.printStackTrace();
		}
	}
}
