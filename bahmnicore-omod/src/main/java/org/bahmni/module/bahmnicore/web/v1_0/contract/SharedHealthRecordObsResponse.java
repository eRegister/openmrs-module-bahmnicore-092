package org.bahmni.module.bahmnicore.web.v1_0.contract;

import java.util.Date;
import java.util.List;

/**
 * Created by Teboho on 2021-10-18.
 */
public class SharedHealthRecordObsResponse {

	private String locationName;
	private String providerName;
	private String encounterDate;
	private String observationName;
	private String observationValue;

	public SharedHealthRecordObsResponse(){}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getEncounterDate() {
		return encounterDate;
	}

	public void setEncounterDate(String encounterDate) {
		this.encounterDate = encounterDate;
	}

	public String getObservationName() {
		return observationName;
	}

	public void setObservationName(String observationName) {
		this.observationName = observationName;
	}

	public String getObservationValue() {
		return observationValue;
	}

	public void setObservationValue(String observationValue) {
		this.observationValue = observationValue;
	}
}
