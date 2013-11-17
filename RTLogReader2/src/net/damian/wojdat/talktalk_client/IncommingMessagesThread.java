package net.damian.wojdat.talktalk_client;
import static net.damian.wojdat.talktalk_server.Commands.CMD_GIVE_USR_NAME;
import static net.damian.wojdat.talktalk_server.Commands.CMD_MSG;
import static net.damian.wojdat.talktalk_server.Commands.CMD_REQ_USR_NAME;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SET_ID;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_DISCONNECT;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_MSG;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_LOG_MSG;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_PING;
import static net.damian.wojdat.talktalk_server.Commands.CMD_USR_LST_UPDATE;

import java.io.IOException;
import java.util.HashMap;


public class IncommingMessagesThread extends Thread{
	
	Object obj = null;
	String message = null;
	Connection connection = null;
	WindowController windowController = null;
	
	IncommingMessagesThread(Connection connection, WindowController windowController) {
		
		this.connection = connection;
		this.windowController = windowController;
		
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		
		System.out.println("[INFO:] TalkTalkIncommingMessagesThread started");
		while(!this.connection.isClosed()) {
	    	try {
	    		
	    		obj = connection.sockObjIn.readObject();
	    		
	    		if(obj == null) {
	    			continue;
	    		}
	    		
	    		if(obj instanceof String) {
	    			processMessage((String) obj);		    		
	    		}
	    		else if(obj instanceof HashMap<?,?>) {
	    			windowController.updateUsersList((HashMap<Integer, String>) obj);
	    		}
	    		
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	    }
		System.out.println("[INFO:] TalkTalkIncommingMessagesThread terminated");
		
	}	
	
	public void processMessage(String message) {

		Integer index = null;
		
		if(message != null && (index = message.indexOf(" ")) != -1) {	
			
			switch(message.substring(0, index)) {
				case CMD_SET_ID: {
					connection.providedId = Integer.parseInt(message.substring(index+1));
					return;
				}
				case CMD_SRV_DISCONNECT: {
					// TODO: To remove
					return;
				}
				case CMD_USR_LST_UPDATE: {
					// TODO: To remove			    				
					return;
				}
				case CMD_REQ_USR_NAME: {
					connection.writeObject(CMD_GIVE_USR_NAME + " " + Client.getInstance().getName());
					return;
				}
				case CMD_MSG: {
					windowController.putFormattedMessage(message.substring(index+1));
					
					if(Client.getInstance().notifiStatus) {
						Client.gStage.toFront();
					}
					
					if(Client.getInstance().notifiSoundStatus) {
						Audio.playSound("msg.wav", Client.getInstance().notifyVolume);
					}
					
					return;
				}
				case CMD_SRV_LOG_MSG: {
					windowController.putFormattedLog(message.substring(index+1));
				}
				case CMD_SRV_MSG: {
					windowController.putFormattedMessage(message.substring(index+1));
					
					if(Client.getInstance().notifiStatus) {
						Client.gStage.toFront();
					}
					
					if(Client.getInstance().notifiSoundStatus) {
						Audio.playSound("msg.wav", Client.getInstance().notifyVolume);
					}
					
					return;
				}
				case CMD_SRV_PING: {
					// TODO: Do some stuff ?
					return;
				}
				default: {
					windowController.putFormattedMessage(message);
					return;
				}
			}
		}
	}
}
