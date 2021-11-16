package org.bahmni.module.bahmnicore.service;

import java.util.Date;
import java.util.List;

import org.bahmni.module.bahmnicore.contract.encounter.data.AdhocQueryDocumentModel;
import org.bahmni.module.bahmnicore.model.XdsAdhocQueryData;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryResponse;
import org.openmrs.module.xdssender.api.model.AdhocQueryDocumentData;

public interface BahmniXDSService {
	public List<AdhocQueryDocumentData> FindDocumentsQuery(String patientIdentifier, Date fromDate, Date toDate);
	public String RetrieveDocumentSet(String documentId);
	public String querySHRObs(String patient_identifier);
}
