package org.bahmni.module.bahmnicore.web.v1_0.contract;

/**
 * Created by Teboho on 2021-01-27.
 */
public class AssignIdentifierRequest {
	private String patientUuid;

	private String identifier;

	private String patientIdentifierType;

	public AssignIdentifierRequest() {
	}

	public AssignIdentifierRequest(String patientUuid, String identifier, String patientIdentifierType ) {
		this.patientUuid = patientUuid;
		this.identifier = identifier;
		this.patientIdentifierType = patientIdentifierType;
	}

	public String getPatientUuid() {
		return patientUuid;
	}

	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getPatientIdentifierType() { return patientIdentifierType; }

	public void setPatientIdentifierType(String patientIdentifierType ) { this.patientIdentifierType = patientIdentifierType; }
}
