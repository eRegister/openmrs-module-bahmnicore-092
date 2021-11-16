package org.bahmni.module.bahmnicore.service.impl;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.bahmni.module.bahmnicore.contract.encounter.data.AdhocQueryDocumentModel;
import org.bahmni.module.bahmnicore.model.XdsAdhocQueryData;
import org.bahmni.module.bahmnicore.service.BahmniXDSService;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.common.exception.XDSException;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryRequest;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryResponse;
import org.dcm4chee.xds2.infoset.rim.ExtrinsicObjectType;
import org.dcm4chee.xds2.infoset.rim.RegistryResponseType;
import org.dcm4chee.xds2.infoset.rim.SlotType1;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryType;
import org.dcm4chee.xds2.infoset.rim.ObjectFactory;
import org.dcm4chee.xds2.infoset.rim.ResponseOptionType;
import org.dcm4chee.xds2.infoset.rim.ValueListType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.xdssender.api.model.AdhocQueryDocumentData;
import org.openmrs.module.xdssender.api.model.AdhocQueryInfo;
import org.openmrs.module.xdssender.api.service.XdsExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.openmrs.module.xdssender.api.service.XdsAdhocQueryService;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class BahmniXDSServiceImpl implements BahmniXDSService {

	private static ObjectFactory factory = new ObjectFactory();

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public List<AdhocQueryDocumentData> FindDocumentsQuery(String patientIdentifier, Date fromDate, Date toDate) {
		List<AdhocQueryDocumentData> response = queryXdsRegistry(patientIdentifier, fromDate, toDate);
		return response;
	}

	private List<AdhocQueryDocumentData> queryXdsRegistry(String patientIdentifier, Date fromDate, Date toDate){
		// Using reflection as a quick hack to get around module dependencies
		// No time to do anything else currently
		Object adhocQueryService = applicationContext.getBean("xdssender.XdsAdhocQueryService");
		try {
			Object xdsresponse = MethodUtils.invokeMethod(adhocQueryService, "queryXdsRegistry", patientIdentifier, fromDate, toDate);
			ArrayList<AdhocQueryDocumentData> response = (ArrayList<AdhocQueryDocumentData>) xdsresponse;
			return response;
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException("Unable to process the document search query on the Health Information Exchange. Please check you internet connection and try again.", e);
		}
	}

	@Override
	public String RetrieveDocumentSet(String documentId) {
		// Using reflection as a quick hack to get around module dependencies
		// No time to do anything else currently
		Object retrieveDocumentSetService = applicationContext.getBean("xdsSender.XdsRetrieveDocumentSetService");
		try {
			Object xdsresponse = MethodUtils.invokeMethod(retrieveDocumentSetService, "RetrieveDocument", documentId);
			String response = (String) xdsresponse;
			return response;
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException("Unable to retrieve the document from the Health Information Exchange. Please check you internet connection and try again.", e);
		}
	}

	public String querySHRObs(String patient_identifier) {
		Object adhocQueryService = this.applicationContext.getBean("xdsSender.XdsImportService");
		try {
			return (String)MethodUtils.invokeMethod(adhocQueryService, "retrieveStrSHRObs", new Object[] { patient_identifier });
		} catch (NoSuchMethodException|IllegalAccessException|java.lang.reflect.InvocationTargetException e) {
			throw new IllegalStateException("Unable to invoke REST query on shared health record.", e);
		}
	}

	private String queryXdsRegistryString(){
		// Using reflection as a quick hack to get around module dependencies
		// No time to do anything else currently

		Object adhocQueryService = applicationContext.getBean("xdssender.XdsAdhocQueryService");

		try {
			return (String) MethodUtils.invokeMethod(adhocQueryService, "queryXdsRegistryString", "fff679a9-84a6-4ead-9675-7d05d60635d3");
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException("Unable to invoke adhoc query on XDS Registry", e);
		}
	}
}
