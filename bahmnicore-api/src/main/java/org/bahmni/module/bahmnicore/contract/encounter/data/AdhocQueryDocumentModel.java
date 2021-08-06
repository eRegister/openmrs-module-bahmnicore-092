package org.bahmni.module.bahmnicore.contract.encounter.data;

import java.util.Date;

/**
 * Created by Teboho on 2019-03-17.
 */
public class AdhocQueryDocumentModel
{
	// Patient ID for the patient this document belongs to
	private String patientID;

	// Provider ID for the clinician who provided care during this patient for this document encounter
	private String providerID;

	// Document ID to use for Retrieve Document Set transaction
	private String documentID;

	// Document Name to use for display purposes
	private String documentName;

	//Document Type to use for display purposes (Not necessary)
	private String documentType;

	//Encounter Date to display the date the clinical encounter occured
	private String encounterDate;

	//Location to display the location the encounter occured
	private String location;

	public AdhocQueryDocumentModel (){

	}

	public AdhocQueryDocumentModel (String patientID, String providerID, String documentID, String documentName, String documentType, String encounterDate, String location){
		this.patientID = patientID;
		this.providerID = providerID;
		this.documentID = documentID;
		this.documentName = documentName;
		this.documentType = documentType;
		this.encounterDate = encounterDate;
		this.location = location;
	}

	public String getPatientID() {
		return this.patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getProviderID() {
		return this.providerID;
	}

	public void setProviderID(String providerID) {
		this.providerID = providerID;
	}

	public String getDocumentID() {
		return this.documentID;
	}

	public void setDocumentID(String documentID) {
		this.documentID = documentID;
	}

	public String getDocumentName() {
		return this.documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getEncounterDate() {
		return this.encounterDate;
	}

	public void setEncounterDate(String encounterDate) {
		this.encounterDate = encounterDate;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}