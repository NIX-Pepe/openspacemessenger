package de.pepe4u.space.dto;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class CommunicationPartner {
	
	public enum CPType {
		USER,
		CHANNEL
	}
	@Element
	private String name;
	private CPType type;
	@Element(required=false)
	private String ip;
	private long lastSeen;	
	private boolean online;
	private boolean newMessageFlag;
	private boolean deleteFlag;
	
	private List<CommunicationMessage> messages;
	
	public CommunicationPartner () {
		name = "";
		type = CPType.USER;
		ip = "";
		lastSeen = 0;
		online = false;
		newMessageFlag = false;
		setDeleteFlag(false);
		messages = new ArrayList<CommunicationMessage>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CPType getType() {
		return type;
	}
	public void setType(CPType type) {
		this.type = type;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public long getLastSeen() {
		return lastSeen;
	}
	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}
	public boolean isOnline() {
		return online;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
	public List<CommunicationMessage> getMessages() {
		return messages;
	}
	public void setMessages(List<CommunicationMessage> messages) {
		this.messages = messages;
	}

	public boolean isNewMessageFlag() {
		return newMessageFlag;
	}

	public void setNewMessageFlag(boolean newMessageFlag) {
		this.newMessageFlag = newMessageFlag;
	}

	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
}
