package org.mobicents.protocols.ss7.tcap.tc.dialog.events;

import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.AbortReason;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCUserAbortRequest;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.UserInformation;

/**
 * 
 * @author amit bhayani
 * 
 */
public class TCUserAbortRequestImpl extends DialogRequestImpl implements
		TCUserAbortRequest {

	private Byte qos;

	// fields
	private ApplicationContextName applicationContextName;
	private UserInformation userInformation;

	private AbortReason abortReason;

	public AbortReason getAbortReason() {
		return this.abortReason;
	}

	public ApplicationContextName getApplicationContextName() {
		return this.applicationContextName;
	}

	public Byte getQOS() {
		return qos;
	}

	public UserInformation getUserInformation() {
		return this.userInformation;
	}

	public void setAbortReason(AbortReason abortReason) {
		this.abortReason = abortReason;
	}

	public void setApplicationContextName(ApplicationContextName acn) {
		this.applicationContextName = acn;
	}

	public void setQOS(Byte b) throws IllegalArgumentException {
		this.qos = b;
	}

	public void setUserInformation(UserInformation userInformation) {
		this.userInformation = userInformation;

	}

}
