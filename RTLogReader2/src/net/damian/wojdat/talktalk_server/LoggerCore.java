package net.damian.wojdat.talktalk_server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static net.damian.wojdat.talktalk_server.Commands.CMD_SRV_MSG;

public class LoggerCore extends Thread {

	private ServerInterface server = null;
	private BufferedReader file;
	private String line;
	
	public LoggerCore(ServerInterface server) {
		
		this.server = server;
		
		try {
			file = new BufferedReader(new FileReader("server_log.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		// Initial file read
		try {
			
			while((line = file.readLine()) != null) {
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		// Continuous file read
		try {
			
			while(true) {
				
				while((line = file.readLine()) != null) {
					
					if(line.matches(".*[Xx][Aa][Mm][Xx][Ee].*")) {
						this.server.sendMessage(-1, "Someone is requesting Xamxe", CMD_SRV_MSG);
						this.server.sendMessage(-1, "Content: " + line, CMD_SRV_MSG);
					}
					
					
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
