package de.pepe4u.space.config;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import de.pepe4u.space.dto.CommunicationPartner;

@Root
public class CommunicationPartnerList {

	@ElementList
	private List<CommunicationPartner> communicationPartnerList;

	public List<CommunicationPartner> getCommunicationPartnerList() {
		return communicationPartnerList;
	}

	public void setCommunicationPartnerList(List<CommunicationPartner> communicationPartnerList) {
		this.communicationPartnerList = communicationPartnerList;
	}
}
