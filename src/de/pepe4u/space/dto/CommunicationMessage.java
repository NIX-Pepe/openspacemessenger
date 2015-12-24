package de.pepe4u.space.dto;

import java.util.Date;

public class CommunicationMessage {
	
	private CommunicationPartner partner;
	private CommunicationPartner channelMember;
	private String messageId;
	private Date date;
	private String message;
	
	public CommunicationPartner getPartner() {
		return partner;
	}
	public void setPartner(CommunicationPartner partner) {
		this.partner = partner;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public CommunicationPartner getChannelMember() {
		return channelMember;
	}
	public void setChannelMember(CommunicationPartner channelMember) {
		this.channelMember = channelMember;
	}
}
