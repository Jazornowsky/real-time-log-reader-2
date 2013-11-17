package net.damian.wojdat.talktalk_client;import static net.damian.wojdat.talktalk_server.Commands.CMD_MSG;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public final class WindowController {

	@FXML private static TextArea messagesArea;
	@FXML private static TextArea logArea;
	@FXML private static CheckBox notifyStatus;
	@FXML private static CheckBox notifySoundStatus;
	@FXML private static Slider notifyVolume;
    @FXML static TextArea sendTextArea;
    @FXML static Button submitMessage;
    @FXML private static TreeView<String> userListMain;
    @FXML private static TreeItem<String> userListRoot = new TreeItem<>("Users");
    
	@FXML protected void handleSubmitButtonAction(ActionEvent event) {
		if(Client.getInstance().connection.providedId == null)
			putFormattedLog("[ERROR:] No ID provided by the server, you can't talk");
		else {
			if(sendTextArea.getText() == "" || sendTextArea.getText() == null)
				return;
			processClientMessage(sendTextArea.getText());
			sendTextArea.setText("");
		}
	}
	
	@FXML protected void handleConnectButtonAction(ActionEvent event) {		
		Client.getInstance().connection.connect();		
	}
	
	@FXML protected void handleDisconnectButtonAction(ActionEvent event) {
		Client.getInstance().connection.disconnect();		
	}
	
	@FXML protected void handleNotifyStatusChange(ActionEvent event) {
		Client.getInstance().notifiStatus = notifyStatus.isSelected();
	}

	@FXML protected void handleNotifySoundStatusChange(ActionEvent event) {
		Client.getInstance().notifiSoundStatus = notifySoundStatus.isSelected();
	}
	
	@FXML protected void onNotifyVolumeDrag(ActionEvent event) {
		System.out.println("VOLUME PERCENT = " + (float) notifyVolume.getValue());
		//Client.getInstance().notifyVolume = -50.0f + (float) notifyVolume.getValue();
	}
	
	@FXML protected void resetSendTextArea(ActionEvent event) {
		sendTextArea.clear();
	}
	
	@FXML protected void onEnterHit(KeyEvent event) {
		
		if(event.isShiftDown()) {
			return;
		}
		if(event.getCode() == KeyCode.ENTER) {
			submitMessage.fire();
			event.consume();
		}
	}
	
	public void cleanUsersList() {
		updateUsersList(new HashMap<Integer, String>());
	}
	
	public void updateUsersList(final HashMap<Integer,String> list) {
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				System.out.println(list.toString());
				userListRoot.getChildren().clear();
				userListRoot.setExpanded(true);
				for(Integer key: list.keySet()) {
					userListRoot.getChildren().add(new TreeItem<String>(list.get(key)));
				}
				userListMain.setRoot(userListRoot);
			}
		});
		
	}
	
	public TextArea getTextArea() {
		return messagesArea;
	}
	
	public TextArea getLogArea() {
		return logArea;
	}
	
	public TextArea getSendTextArea() {
		return sendTextArea;
	}
	
	public Button getSubmitButton() {
		return submitMessage;
	}
	
	public void putFormattedLog(String msg) {
		
		class PutFormattedLog implements Runnable {
			String msg;
			PutFormattedLog(String s) { msg = s; }
			
			public void run() {
				
				Calendar calendar = Calendar.getInstance();
				calendar.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				getLogArea().appendText("[" + sdf.format(calendar.getTime()) + "] " + msg + "\n");
				
			}
		}
		
		Platform.runLater(new PutFormattedLog(msg));	
		
	}
	
	public void putFormattedMessage(String msg) {
		
		class PutFormattedMessage implements Runnable {
			String msg;
			PutFormattedMessage(String s) { msg = s; }
			
			public void run() {
				
				Calendar calendar = Calendar.getInstance();
				calendar.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				getTextArea().appendText("[" + sdf.format(calendar.getTime()) + "] " + msg + "\n");
				
			}
		}
		
		Platform.runLater(new PutFormattedMessage(msg));		
		
	}
	
	public void processClientMessage(String message) {
		
		Integer index = null;
		
		if(message != null && (index = message.indexOf(" ")) != -1) {
			
			switch(message.substring(0, index)) {
				case CMD_MSG: {
					putFormattedMessage("[" + Client.getInstance().getName() + ":] " + message.substring(index+1));
					Client.getInstance().connection.writeObject(CMD_MSG + " [" + Client.getInstance().getName() + ":] " + message.substring(index+1));
					return;
				}
				default: {
					putFormattedMessage("[" + Client.getInstance().getName() + ":] " + message);
					Client.getInstance().connection.writeObject(CMD_MSG + " [" + Client.getInstance().getName() + ":] " + message);
					return;
				}
			}
		}
		else {
			putFormattedMessage("[" + Client.getInstance().getName() + ":] " + message);
			Client.getInstance().connection.writeObject(CMD_MSG + " [" + Client.getInstance().getName() + ":] " + message);
		}
	}
	
}
