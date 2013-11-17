package net.damian.wojdat.talktalk_client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Client extends Application {
    
	static Properties config = null; 
	static Stage gStage = null;
	private String name = "TalkTalk User";
	ConnectionManager connection = null;
	boolean notifiStatus = true;
	boolean notifiSoundStatus = true;
	Float notifyVolume = 70.0f;
	WindowController windowController = null;
	IncommingMessagesThread incommingMessagesThread = null;
	private static Client CLIENT = null;
	
	
	public static Client getInstance() {
		return CLIENT;
	}
	
    @Override
    public void init() {
    	double p1 = 2.5;
    	System.out.println("p1 value = " + (int)p1);
    	CLIENT = this;
    	windowController = new WindowController();
		connection = new ConnectionManager();
    }
    
    @Override
    public void stop() {
    	connection.disconnect();
    }
    
	@Override
	public void start(Stage stage) {		

		gStage = stage;
		stage.setTitle("TalkTalk!");
        
		loadConfig();
		
        Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        stage.setScene(new Scene(root));
        stage.setMinHeight(500.0);
        stage.setMinWidth(400.0);
        stage.show();
        
        windowController.cleanUsersList();        
        windowController.putFormattedLog("Welcome to TalkTalk !");
        
        new Thread(new Runnable() {

			@Override
			public void run() {
				connection.connect();
			}
        	
        }).start();
        
	}

	public static void main(String[] args) throws IOException {
		launch(args);
	}
	
	public void loadConfig() {
		FileInputStream fis = null;
		config = new Properties();
		try {
			fis = new FileInputStream("talktalk.properties");
		} catch (FileNotFoundException e) {
			System.err.println("[INFO:] Couldn't find talktalk.properties file !");
			return;
		}
		
		try {
			config.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		connection.setHost(config.getProperty("host"));
		connection.setPort(Integer.parseInt(config.getProperty("port")));
		name = config.getProperty("name");
		
		try {
			fis.close();
		} catch (IOException e) {}
		
	}
	
	public String getName() {
		return name;
	}
	
	class ConnectionManager extends Connection {

		@Override
		protected void sendNewInfo(String message) {
			windowController.putFormattedLog(message);			
		}

		@Override
		void preConnect() {
			windowController.cleanUsersList();			
		}

		@Override
		void postConnect() {
			incommingMessagesThread = new IncommingMessagesThread(connection, windowController);
			incommingMessagesThread.start();
			WindowController.sendTextArea.setDisable(false);
			WindowController.submitMessage.setDisable(false);
		}

		@Override
		void postDisconnect() {
			sendNewInfo("[INFO:] Checking if incommingMessagesThread isAlive");
			if(incommingMessagesThread != null) {
				if(incommingMessagesThread.isAlive()) {
					sendNewInfo("[INFO:] Waiting for incommingMessagesThread to end");
    	        	try {
						incommingMessagesThread.join(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    	        }
			}
			
		}

		@Override
		void preDisconnect() {
			WindowController.sendTextArea.setDisable(true);
			WindowController.submitMessage.setDisable(true);			
		}

    }
	
}


