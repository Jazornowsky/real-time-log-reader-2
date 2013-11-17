package net.damian.wojdat.talktalk_server;

import static net.damian.wojdat.talktalk_server.Commands.CMD_CLT_DISCONNECT;
import static net.damian.wojdat.talktalk_server.Commands.CMD_CLT_PING;
import static net.damian.wojdat.talktalk_server.Commands.CMD_GIVE_USR_NAME;
import static net.damian.wojdat.talktalk_server.Commands.CMD_MSG;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_MSG;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_LOG_MSG;

public class Messages {
	// TODO: Store messages by date (Calendar class)
	
	ServerInterface server = null;
	
	public Messages(ServerInterface server) {
		this.server = server;
		System.out.println("[SERVER:] Message service initialized");
	}
	
	public void sendMessage(Integer fromId, String message, String command) {
		
		Integer strIdx = null;
		
		System.out.println("[INFO:][MESSAGE:][" + fromId + ":] " + message);		
		
		if(command == "") {
			if((strIdx = message.indexOf(" ")) != -1) {
				command = message.substring(0, strIdx);					
			}
			else {
				if(fromId == -1) {
					command = CMD_SRV_LOG_MSG;
				}
				else {
					command = CMD_MSG;
				}
			}
		}
		
		switch(command) {
			case CMD_MSG: {
				for(Integer key: server.getClientSet()) {
					if(key != fromId) {
						server.getClientInstance(key).sendMessage(message);
					}
				}
				break;
			}
			case CMD_SRV_LOG_MSG: {
				for(Integer key: server.getClientSet()) {
					server.getClientInstance(key).sendMessage(CMD_SRV_LOG_MSG);
				}
				break;
			}
			case CMD_SRV_MSG: {
				for(Integer key: server.getClientSet()) {
					server.getClientInstance(key).sendMessage(CMD_SRV_MSG + " " + message);
				}
				break;
			}
			case CMD_CLT_DISCONNECT: {
				server.getClientInstance(fromId).disconnectUser();
				break;
			}
			case CMD_GIVE_USR_NAME: {
				System.out.println("[DEBUG:] CMD_GIVE_USR_NAME, name = " + message.substring(strIdx+1));
				server.setClientName(fromId, message.substring(strIdx+1));
				server.getClientInstance(fromId).setClientName(message.substring(strIdx+1));
				
				for(Integer key: server.getClientSet()) {
					server.getClientInstance(key).sendNewUserList();
				}
				
				break;
			}
			case CMD_CLT_PING: {
				System.out.println("[DEBUG:] CMD_CLT_PING, id = " + fromId);
				server.getClientInstance(fromId).setPingResponse(true);
				break;
			}
		}
	}
	
	public void sendMessage(Integer clientId, String message) {
		sendMessage(clientId, message, "");
	}
}
