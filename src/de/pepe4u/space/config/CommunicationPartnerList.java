package de.pepe4u.space.config;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import de.pepe4u.space.dto.CommunicationPartner;

/**
 * DTO Object for saving contact list
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
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
