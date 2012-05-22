/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.protocols.ss7.map.service.subscriberInformation;

import java.io.IOException;

import org.mobicents.protocols.asn.AsnException;
import org.mobicents.protocols.asn.AsnInputStream;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.asn.Tag;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParsingComponentException;
import org.mobicents.protocols.ss7.map.api.MAPParsingComponentExceptionReason;
import org.mobicents.protocols.ss7.map.api.primitives.MAPExtensionContainer;
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.DomainType;
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.RequestedInfo;
import org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive;
import org.mobicents.protocols.ss7.map.primitives.MAPExtensionContainerImpl;

/**
 * @author amit bhayani
 * 
 */
public class RequestedInfoImpl implements RequestedInfo, MAPAsnPrimitive {
	
	public static final int _ID_locationInformation = 0;
	public static final int _ID_subscriberState = 1;
	public static final int _ID_extensionContainer = 2;
	public static final int _ID_currentLocation = 3;
	public static final int _ID_requestedDomain = 4;
	public static final int _ID_imei = 6;
	public static final int _ID_msclassmark = 5;
	public static final int _ID_mnpRequestedInfo = 7;
	
	
	private Boolean locationInformation;
	private Boolean subscriberState;
	private MAPExtensionContainer extensionContainer;
	private Boolean currentLocation;
	private DomainType requestedDomain;
	private Boolean imei;
	private Boolean msClassmark;
	private Boolean mnpRequestedInfo;

	/**
	 * 
	 */
	public RequestedInfoImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param locationInformation
	 * @param subscriberState
	 * @param extensionContainer
	 * @param currentLocation
	 * @param requestedDomain
	 * @param imei
	 * @param msClassmark
	 * @param mnpRequestedInfo
	 */
	public RequestedInfoImpl(Boolean locationInformation, Boolean subscriberState, MAPExtensionContainer extensionContainer, Boolean currentLocation,
			DomainType requestedDomain, Boolean imei, Boolean msClassmark, Boolean mnpRequestedInfo) {
		super();
		this.locationInformation = locationInformation;
		this.subscriberState = subscriberState;
		this.extensionContainer = extensionContainer;
		this.currentLocation = currentLocation;
		this.requestedDomain = requestedDomain;
		this.imei = imei;
		this.msClassmark = msClassmark;
		this.mnpRequestedInfo = mnpRequestedInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#getTag()
	 */
	@Override
	public int getTag() throws MAPException {
		return Tag.SEQUENCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#getTagClass()
	 */
	@Override
	public int getTagClass() {
		return Tag.CLASS_UNIVERSAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#getIsPrimitive
	 * ()
	 */
	@Override
	public boolean getIsPrimitive() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#decodeAll(
	 * org.mobicents.protocols.asn.AsnInputStream)
	 */
	@Override
	public void decodeAll(AsnInputStream ansIS) throws MAPParsingComponentException {
		try {
			int length = ansIS.readLength();
			this._decode(ansIS, length);
		} catch (IOException e) {
			throw new MAPParsingComponentException("IOException when decoding RequestedInfo: " + e.getMessage(), e,
					MAPParsingComponentExceptionReason.MistypedParameter);
		} catch (AsnException e) {
			throw new MAPParsingComponentException("AsnException when decoding RequestedInfo: " + e.getMessage(), e,
					MAPParsingComponentExceptionReason.MistypedParameter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#decodeData
	 * (org.mobicents.protocols.asn.AsnInputStream, int)
	 */
	@Override
	public void decodeData(AsnInputStream ansIS, int length) throws MAPParsingComponentException {
		try {
			this._decode(ansIS, length);
		} catch (IOException e) {
			throw new MAPParsingComponentException("IOException when decoding RequestedInfo: " + e.getMessage(), e,
					MAPParsingComponentExceptionReason.MistypedParameter);
		} catch (AsnException e) {
			throw new MAPParsingComponentException("AsnException when decoding RequestedInfo: " + e.getMessage(), e,
					MAPParsingComponentExceptionReason.MistypedParameter);
		}
	}

	private void _decode(AsnInputStream ansIS, int length) throws MAPParsingComponentException, IOException, AsnException {
		AsnInputStream ais = ansIS.readSequenceStreamData(length);

		int num = 0;
		while (true) {
			if (ais.available() == 0)
				break;

			int tag = ais.readTag();
			switch (tag) {
			case _ID_locationInformation:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				ais.readNull();
				this.locationInformation = Boolean.TRUE;
				break;
			case _ID_subscriberState:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				ais.readNull();
				this.subscriberState = Boolean.TRUE;
				break;
			case _ID_extensionContainer:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				extensionContainer = new MAPExtensionContainerImpl();
				((MAPExtensionContainerImpl) extensionContainer).decodeAll(ais);
				break;
			case _ID_currentLocation:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				ais.readNull();
				this.currentLocation = Boolean.TRUE;
				break;
			case _ID_requestedDomain:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				int i1 = (int) ais.readInteger();
				this.requestedDomain = DomainType.getInstance(i1);
				break;
			case _ID_msclassmark:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				ais.readNull();
				this.msClassmark = Boolean.TRUE;
				break;
			case _ID_imei:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				ais.readNull();
				this.imei = Boolean.TRUE;
				break;
			case _ID_mnpRequestedInfo:
				if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive())
					throw new MAPParsingComponentException("Error while decoding RequestedInfo: Parameter 0 bad tag class or not primitive",
							MAPParsingComponentExceptionReason.MistypedParameter);
				ais.readNull();
				this.mnpRequestedInfo = Boolean.TRUE;
				break;
			default:
				ais.advanceElement();
				break;

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#encodeAll(
	 * org.mobicents.protocols.asn.AsnOutputStream)
	 */
	@Override
	public void encodeAll(AsnOutputStream asnOs) throws MAPException {
		this.encodeAll(asnOs, Tag.CLASS_UNIVERSAL, Tag.SEQUENCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#encodeAll(
	 * org.mobicents.protocols.asn.AsnOutputStream, int, int)
	 */
	@Override
	public void encodeAll(AsnOutputStream asnOs, int tagClass, int tag) throws MAPException {
		try {
			asnOs.writeTag(tagClass, false, tag);
			int pos = asnOs.StartContentDefiniteLength();
			this.encodeData(asnOs);
			asnOs.FinalizeContent(pos);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding reportSMDeliveryStatusRequest: " + e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive#encodeData
	 * (org.mobicents.protocols.asn.AsnOutputStream)
	 */
	@Override
	public void encodeData(AsnOutputStream asnOs) throws MAPException {
		try {
			if (this.locationInformation != null) {
				asnOs.writeNull(Tag.CLASS_CONTEXT_SPECIFIC, _ID_locationInformation);
			}
		} catch (IOException e) {
			throw new MAPException("IOException when encoding parameter locationInformation: ", e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding parameter locationInformation: ", e);
		}

		try {
			if (this.subscriberState != null) {
				asnOs.writeNull(Tag.CLASS_CONTEXT_SPECIFIC, _ID_subscriberState);
			}
		} catch (IOException e) {
			throw new MAPException("IOException when encoding parameter locationInformation: ", e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding parameter locationInformation: ", e);
		}

		if (this.extensionContainer != null)
			((MAPExtensionContainerImpl) this.extensionContainer).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_extensionContainer);

		try {
			if (this.currentLocation != null) {
				asnOs.writeNull(Tag.CLASS_CONTEXT_SPECIFIC, _ID_currentLocation);
			}
		} catch (IOException e) {
			throw new MAPException("IOException when encoding parameter currentLocation: ", e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding parameter currentLocation: ", e);
		}

		try {
			if (this.requestedDomain != null) {
				asnOs.writeInteger(Tag.CLASS_CONTEXT_SPECIFIC, _ID_requestedDomain, this.requestedDomain.getType());
			}
		} catch (IOException e) {
			throw new MAPException("IOException when encoding parameter requestedDomain: ", e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding parameter requestedDomain: ", e);
		}

		try {
			if (this.imei != null) {
				asnOs.writeNull(Tag.CLASS_CONTEXT_SPECIFIC, _ID_imei);
			}
		} catch (IOException e) {
			throw new MAPException("IOException when encoding parameter imei: ", e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding parameter imei: ", e);
		}

		try {
			if (this.msClassmark != null) {
				asnOs.writeNull(Tag.CLASS_CONTEXT_SPECIFIC, _ID_msclassmark);
			}
		} catch (IOException e) {
			throw new MAPException("IOException when encoding parameter msClassmark: ", e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding parameter msClassmark: ", e);
		}

		try {
			if (this.mnpRequestedInfo != null) {
				asnOs.writeNull(Tag.CLASS_CONTEXT_SPECIFIC, _ID_mnpRequestedInfo);
			}
		} catch (IOException e) {
			throw new MAPException("IOException when encoding parameter mnpRequestedInfo: ", e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding parameter mnpRequestedInfo: ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getLocationInformation()
	 */
	@Override
	public Boolean getLocationInformation() {
		return this.locationInformation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getSubscriberState()
	 */
	@Override
	public Boolean getSubscriberState() {
		return this.subscriberState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getExtensionContainer()
	 */
	@Override
	public MAPExtensionContainer getExtensionContainer() {
		return this.extensionContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getCurrentLocation()
	 */
	@Override
	public Boolean getCurrentLocation() {
		return this.currentLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getRequestedDomain()
	 */
	@Override
	public DomainType getRequestedDomain() {
		return this.requestedDomain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getImei()
	 */
	@Override
	public Boolean getImei() {
		return this.imei;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getMsClassmark()
	 */
	@Override
	public Boolean getMsClassmark() {
		return this.msClassmark;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * RequestedInfo#getMnpRequestedInfo()
	 */
	@Override
	public Boolean getMnpRequestedInfo() {
		return this.mnpRequestedInfo;
	}

}
