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
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.DiameterIdentity;
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.EUtranCgi;
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.GeodeticInformation;
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.GeographicalInformation;
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.LocationInformationEPS;
import org.mobicents.protocols.ss7.map.api.service.subscriberInformation.TAId;
import org.mobicents.protocols.ss7.map.primitives.MAPAsnPrimitive;
import org.mobicents.protocols.ss7.map.primitives.MAPExtensionContainerImpl;

/**
 * @author amit bhayani
 * 
 */
public class LocationInformationEPSImpl implements LocationInformationEPS, MAPAsnPrimitive {

	public static final int _ID_eUtranCellGlobalIdentity = 0;
	public static final int _ID_trackingAreaIdentity = 1;
	public static final int _ID_extensionContainer = 2;
	public static final int _ID_geographicalInformation = 3;
	public static final int _ID_geodeticInformation = 4;
	public static final int _ID_currentLocationRetrieved = 5;
	public static final int _ID_ageOfLocationInformation = 6;
	public static final int _ID_mme_Name = 7;

	public static final String _PrimitiveName = "LocationInformationEPS";

	private EUtranCgi eUtranCellGlobalIdentity = null;
	private TAId trackingAreaIdentity = null;
	private MAPExtensionContainer extensionContainer = null;
	private GeographicalInformation geographicalInformation = null;
	private GeodeticInformation geodeticInformation = null;
	private Boolean currentLocationRetrieved = null;
	private Integer ageOfLocationInformation = null;
	private DiameterIdentity mmeName = null;

	/**
	 * 
	 */
	public LocationInformationEPSImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param eUtranCellGlobalIdentity
	 * @param trackingAreaIdentity
	 * @param extensionContainer
	 * @param geographicalInformation
	 * @param geodeticInformation
	 * @param currentLocationRetrieved
	 * @param ageOfLocationInformation
	 * @param mmeName
	 */
	public LocationInformationEPSImpl(EUtranCgi eUtranCellGlobalIdentity, TAId trackingAreaIdentity, MAPExtensionContainer extensionContainer,
			GeographicalInformation geographicalInformation, GeodeticInformation geodeticInformation, Boolean currentLocationRetrieved,
			Integer ageOfLocationInformation, DiameterIdentity mmeName) {
		super();
		this.eUtranCellGlobalIdentity = eUtranCellGlobalIdentity;
		this.trackingAreaIdentity = trackingAreaIdentity;
		this.extensionContainer = extensionContainer;
		this.geographicalInformation = geographicalInformation;
		this.geodeticInformation = geodeticInformation;
		this.currentLocationRetrieved = currentLocationRetrieved;
		this.ageOfLocationInformation = ageOfLocationInformation;
		this.mmeName = mmeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getEUtranCellGlobalIdentity()
	 */
	@Override
	public EUtranCgi getEUtranCellGlobalIdentity() {
		return this.eUtranCellGlobalIdentity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getTrackingAreaIdentity()
	 */
	@Override
	public TAId getTrackingAreaIdentity() {
		return this.trackingAreaIdentity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getExtensionContainer()
	 */
	@Override
	public MAPExtensionContainer getExtensionContainer() {
		return this.extensionContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getGeographicalInformation()
	 */
	@Override
	public GeographicalInformation getGeographicalInformation() {
		return this.geographicalInformation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getGeodeticInformation()
	 */
	@Override
	public GeodeticInformation getGeodeticInformation() {
		return this.geodeticInformation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getCurrentLocationRetrieved()
	 */
	@Override
	public Boolean getCurrentLocationRetrieved() {
		return this.currentLocationRetrieved;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getAgeOfLocationInformation()
	 */
	@Override
	public Integer getAgeOfLocationInformation() {
		return this.ageOfLocationInformation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.map.api.service.subscriberInformation.
	 * LocationInformationEPS#getMmeName()
	 */
	@Override
	public DiameterIdentity getMmeName() {
		return this.mmeName;
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
			throw new MAPParsingComponentException("IOException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
					MAPParsingComponentExceptionReason.MistypedParameter);
		} catch (AsnException e) {
			throw new MAPParsingComponentException("AsnException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
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
			throw new MAPParsingComponentException("IOException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
					MAPParsingComponentExceptionReason.MistypedParameter);
		} catch (AsnException e) {
			throw new MAPParsingComponentException("AsnException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
					MAPParsingComponentExceptionReason.MistypedParameter);
		}
	}

	private void _decode(AsnInputStream ansIS, int length) throws MAPParsingComponentException, IOException, AsnException {

		this.eUtranCellGlobalIdentity = null;
		this.trackingAreaIdentity = null;
		this.extensionContainer = null;
		this.geographicalInformation = null;
		this.geodeticInformation = null;
		this.currentLocationRetrieved = null;
		this.ageOfLocationInformation = null;

		AsnInputStream ais = ansIS.readSequenceStreamData(length);

		while (true) {
			if (ais.available() == 0)
				break;

			int tag = ais.readTag();

			// optional parameters
			if (ais.getTagClass() == Tag.CLASS_CONTEXT_SPECIFIC) {

				switch (tag) {
				case _ID_eUtranCellGlobalIdentity:
					this.eUtranCellGlobalIdentity = new EUtranCgiImpl();
					((EUtranCgiImpl) this.eUtranCellGlobalIdentity).decodeAll(ais);
					break;
				case _ID_trackingAreaIdentity:
					this.trackingAreaIdentity = new TAIdImpl();
					((TAIdImpl) this.trackingAreaIdentity).decodeAll(ais);
					break;
				case _ID_extensionContainer:
					this.extensionContainer = new MAPExtensionContainerImpl();
					((MAPExtensionContainerImpl) this.extensionContainer).decodeAll(ais);
					break;
				case _ID_geographicalInformation:
					this.geographicalInformation = new GeographicalInformationImpl();
					((GeographicalInformationImpl) this.geographicalInformation).decodeAll(ais);
					break;

				case _ID_geodeticInformation:
					this.geodeticInformation = new GeodeticInformationImpl();
					((GeodeticInformationImpl) this.geodeticInformation).decodeAll(ais);
					break;
				case _ID_currentLocationRetrieved:
					if (ais.getTagClass() != Tag.CLASS_CONTEXT_SPECIFIC || !ais.isTagPrimitive()) {
						throw new MAPParsingComponentException(
								"Error while decoding LocationInformation: Parameter [currentLocationRetrieved	[8] NULL ] bad tag class, tag or not primitive",
								MAPParsingComponentExceptionReason.MistypedParameter);
					}
					this.currentLocationRetrieved = true;
					break;
				case _ID_ageOfLocationInformation:
					this.ageOfLocationInformation = (int) ais.readInteger();
					break;
				default:
					ais.advanceElement();
					break;
				}
			} else {
				ais.advanceElement();
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
		this.encodeAll(asnOs, Tag.CLASS_UNIVERSAL, this.getTag());
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
			asnOs.writeTag(tagClass, true, tag);
			int pos = asnOs.StartContentDefiniteLength();
			this.encodeData(asnOs);
			asnOs.FinalizeContent(pos);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding " + _PrimitiveName + ": " + e.getMessage(), e);
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
			
			if(this.eUtranCellGlobalIdentity != null)
				((EUtranCgiImpl) this.eUtranCellGlobalIdentity).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_eUtranCellGlobalIdentity);
			
			if(this.trackingAreaIdentity != null){
				((TAIdImpl) this.trackingAreaIdentity).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_trackingAreaIdentity);
			}
			
			if (this.extensionContainer != null)
				((MAPExtensionContainerImpl) this.extensionContainer).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_extensionContainer);
			
			if (this.geographicalInformation != null)
				((GeographicalInformationImpl) this.geographicalInformation).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_geographicalInformation);

			if (this.geodeticInformation != null)
				((GeodeticInformationImpl) this.geodeticInformation).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_geodeticInformation);
			
			if (this.currentLocationRetrieved != null && this.currentLocationRetrieved) {
				try {
					asnOs.writeNull(Tag.CLASS_CONTEXT_SPECIFIC, _ID_currentLocationRetrieved);
				} catch (IOException e) {
					throw new MAPException("Error while encoding LocationInformation the optional parameter currentLocationRetrieved encoding failed ", e);
				} catch (AsnException e) {
					throw new MAPException("Error while encoding LocationInformation the optional parameter currentLocationRetrieved encoding failed ", e);
				}
			}
			
			if (ageOfLocationInformation != null)
				asnOs.writeInteger((int) ageOfLocationInformation);
			
			if(this.mmeName != null){
				((DiameterIdentityImpl)this.mmeName).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_mme_Name);
			}

		} catch (IOException e) {
			throw new MAPException("IOException when encoding " + _PrimitiveName + ": " + e.getMessage(), e);
		} catch (AsnException e) {
			throw new MAPException("AsnException when encoding " + _PrimitiveName + ": " + e.getMessage(), e);
		}
	}

}
