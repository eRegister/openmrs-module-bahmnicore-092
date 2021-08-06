package org.bahmni.module.bahmnicore.model;

import javax.xml.bind.JAXBElement;

import java.util.List;

import org.dcm4chee.xds2.infoset.rim.ExtrinsicObjectType;
import org.dcm4chee.xds2.infoset.rim.RegistryObjectListType;

/**
 * Created by Teboho on 2019-03-17.
 */
public class XdsAdhocQueryData {

	// Registry Object List
	private List<JAXBElement<ExtrinsicObjectType>> registryObjectListType;

	// Response status
	private String status;

	public XdsAdhocQueryData(List<JAXBElement<ExtrinsicObjectType>> registryObjectListType, String status) {
		this.registryObjectListType = registryObjectListType;
		this.status = status;
	}

	/**
	 * Gets the registry object list
	 *
	 * @return RegistryObjectListType
	 */
	public List<JAXBElement<ExtrinsicObjectType>> getRegistryObjectList() {
		return this.registryObjectListType;
	}

	/**
	 * Sets the registry object list
	 *
	 * @param registryObjectListType
	 */
	public void setRegistryObjectListType(List<JAXBElement<ExtrinsicObjectType>> registryObjectListType) {
		this.registryObjectListType = registryObjectListType;
	}

	/**
	 * Gets the adhoc query response status
	 *
	 * @return String
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * Sets the adhoc query response status
	 *
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
