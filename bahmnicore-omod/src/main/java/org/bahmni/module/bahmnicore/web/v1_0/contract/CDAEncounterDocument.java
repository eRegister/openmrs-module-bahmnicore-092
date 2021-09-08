package org.bahmni.module.bahmnicore.web.v1_0.contract;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.Double;

import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.xml.sax.SAXException;

/**
 * Created by Teboho on 2019-03-25.
 */
public class CDAEncounterDocument {

	private static final String PATIENT_ID_XPATH_EXPRESSION = "ClinicalDocument/recordTarget/patientRole";
	private static final String VISIT_AND_ENCOUNTER_UUID_XPATH_EXPRESSION = "ClinicalDocument/id";
	private static final String PROVIDER_ID_XPATH_EXPRESSION = "ClinicalDocument/author/assignedAuthor";
	private static final String LOCATION_NAME_XPATH_EXPRESSION = "ClinicalDocument/custodian/assignedCustodian/representedCustodianOrganization";
	private static final String ENCOUNTER_EFFECTIVE_DATETIME_XPATH_EXPRESSION = "ClinicalDocument/documentationOf/serviceEvent/effectiveTime";
	private static final String SECTIONS_XPATH_EXPRESSION = "ClinicalDocument/component/structuredBody/component/section";


	private static final String PATIENT_ID_XPATH_EXTENSION = "ClinicalDocument/recordTarget/patientRole/id[1][@extension]";
	private XPath xpath;

	/*public static final String PATIENT_ID_SLOT_TYPE = "$XDSDocumentEntryPatientId";
      private static final String BASE_XPATH_EXPRESSION = "//AdhocQueryRequest[1]/AdhocQuery/Slot[@name='%s']/ValueList[1]/Value";
      private static final String MESSAGEID_XPATH_EXPRESSION = "Envelope/Header/MessageID";
      /ClinicalDocument/recordTarget/patientRole/id*/

	private String cdaDocument;
	private Patient patient;
	private Encounter encounter;
	private List<String> obsNames;
	private Vitals vitals;
	private FlowSheetSummary flowSheetSummary;


	private String patientId;
	private String patientEcidId;
	private String providerId;
	private String visitUuid;
	private String encounterUuid;
	private String locationName;



	private Provider provider;
	private Location location;
	private String visitStartDatetime;
	private String visitStopDatetime;
	private String encounterDatetime;
	private Visit visit;

	public CDAEncounterDocument(){
		obsNames = new ArrayList<>();
		xpath = XPathFactory.newInstance().newXPath();
		vitals = new Vitals();
		flowSheetSummary = new FlowSheetSummary();
	}

	public CDAEncounterDocument(String cdaDocument){
		this.cdaDocument = cdaDocument;
		obsNames = new ArrayList<>();
		xpath = XPathFactory.newInstance().newXPath();
		vitals = new Vitals();
		flowSheetSummary = new FlowSheetSummary();
	}

	public CDAEncounterDocument(String cdaDocument, Patient patient, String patientId, String providerId,
			String visitUuid, String encounterUuid, String locationName,
			Encounter encounter, Provider provider, Location location,
			String encounterDatetime, Visit visit){
		this.cdaDocument = cdaDocument;
		this.patient = patient;

		this.patientId = patientId;
		this.providerId = providerId;
		this.visitUuid = visitUuid;
		this.encounterUuid = encounterUuid;
		this.locationName = locationName;

		this.encounter = encounter;
		this.provider = provider;
		this.location = location;
		this.encounterDatetime = encounterDatetime;
		this.visit = visit;

		// Instantiate the Obs array list, to populate from the CDADocument
		obsNames = new ArrayList<>();
		xpath = XPathFactory.newInstance().newXPath();
		vitals = new Vitals();
		flowSheetSummary = new FlowSheetSummary();
	}

	public String getCdaDocument(){
		return this.cdaDocument;
	}

	public void setCdaDocument(String cdaDocument){
		this.cdaDocument = cdaDocument;
	}

	public List<String> getObsNames(){ return obsNames; }

	public Patient getPatient(){
		return this.patient;
	}

	public void setPatient(Patient patient){
		this.patient = patient;
	}

	public String getPatientId(){
		return this.patientId;
	}

	public void setPatientId(String patientId){
		this.patientId = patientId;
	}

	public String getPatientEcidId(){
		return this.patientEcidId;
	}

	public void setPatientEcidId(String patientEcidId){
		this.patientEcidId = patientEcidId;
	}

	public String getProviderId(){
		return this.providerId;
	}

	public void setProviderId(String providerId){
		this.providerId = providerId;
	}

	public String getVisitUuid(){
		return this.visitUuid;
	}

	public void setVisitUuid(String visitUuid){
		this.visitUuid = visitUuid;
	}

	public String getEncounterUuid(){
		return this.encounterUuid;
	}

	public void setEncounterUuid(String encounterUuid){
		this.encounterUuid = encounterUuid;
	}

	public String getLocationName(){
		return this.locationName;
	}

	public void setLocationName(String locationName){
		this.locationName = locationName;
	}

	public Encounter getEncounter() { return this.encounter; }

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	public Provider getProvider(){
		return this.provider;
	}

	public void setProvider(Provider provider){
		this.provider = provider;
	}

	public Location getLocation(){
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getEncounterDatetime(){
		return this.encounterDatetime;
	}

	public void setEncounterDatetime(String encounterDatetime) {
		this.encounterDatetime = encounterDatetime;
	}

	public String getVisitStartDatetime(){
		return this.visitStartDatetime;
	}

	public void setVisitStartDatetime(String visitStartDatetime) {
		this.visitStartDatetime = visitStartDatetime;
	}

	public String getVisitStopDatetime(){
		return this.visitStopDatetime;
	}

	public void setVisitStopDatetime(String visitStopDatetime) {
		this.visitStopDatetime = visitStopDatetime;
	}

	public Visit getVisit(){
		return this.visit;
	}

	public void setVisit(Visit visit) {
		this.visit = visit;
	}

	public Vitals getVitals() { return this.vitals; }
	public void setVitals(Vitals vitals) { this.vitals = vitals; }

	public FlowSheetSummary getFlowSheetSummaryValues() { return this.flowSheetSummary; }
	public void setFlowSheetSummaryValues (FlowSheetSummary flowSheetSummaryValues) { this.flowSheetSummary = flowSheetSummaryValues; }

	public void populateEncounterFromCDADocument() {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(IOUtils.toInputStream(this.cdaDocument));

			// 1. Get the patient from the document
			getPatientIdentifiersFromCDADoc(doc);
			// 2. Get the provider from the document
			getProvidersFromCDADoc(doc);
			// 3. Get the location from the document
			getLocationFromCDADoc(doc);
			// 4. Get the visitUUID and the encounterUUID from the document
			getVisitandEncounterUuidFromCDADoc(doc);
			// 5. Get the Observations from the document
			getObservationsFromCDADoc(doc);
			// 6. Get encounter date
			getVisitDateTimeFromCDADoc(doc);

		}catch (SAXException e){

		}catch (ParserConfigurationException | IOException ex) {

		}
	}

	private void getObservationsFromCDADoc(Document doc){
		try {
			NodeList nodeList = (NodeList) xpath.compile(SECTIONS_XPATH_EXPRESSION).evaluate(
					doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					if (eElement.getElementsByTagName("code").item(0).getAttributes()
							.getNamedItem("code").getNodeValue().equalsIgnoreCase("8716-3")){
						// Vital Signs Section Template
					    Node entryNode = eElement.getElementsByTagName("entry").item(0);

						if(entryNode.getNodeType() == Node.ELEMENT_NODE) {
							Element entryElement = (Element) entryNode;
							Element organizerElement = (Element) entryElement.getElementsByTagName("organizer").item(0);

							// Set the encounter date time
							this.setEncounterDatetime(organizerElement.getElementsByTagName("effectiveTime").item(0)
									.getAttributes().getNamedItem("value").getNodeValue());

							for(int j = 0; j < organizerElement.getElementsByTagName("component").getLength(); j++){
								Element componentElement = (Element) organizerElement.getElementsByTagName("component")
										.item(j);

								Element observationElement = (Element)componentElement.getElementsByTagName("observation")
										.item(0);

								if(observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("8480-6")){
									// Blood Pressure - Systolic in LOINC
									this.vitals.setSystolicBP(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("systolic");

								} else if(observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("8462-4")){
									// Blood Pressure - Diastolic in LOINC
									this.vitals.setDiastolicBP(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("diastolic");
								} else if(observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("3141-9")){
									// Body weight measured in LOINC
									this.vitals.setWeight(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("weight");
								} else if(observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("8302-2")){
									// Body height measured in LOINC
									this.vitals.setHeight(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("height");
								} else if(observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("8310-5")){
									// Body Temperature in LOINC
									this.vitals.setTemperature(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("temperature");
								} else {
									// NOTHING
								}
							}
						}
					} else if(eElement.getElementsByTagName("code").item(0).getAttributes()
							.getNamedItem("code").getNodeValue().equalsIgnoreCase("57059-8")) {
						// Visit Summary Section Template
						Node entryNode = eElement.getElementsByTagName("entry").item(0);

						if(entryNode.getNodeType() == Node.ELEMENT_NODE) {
							Element entryElement = (Element) entryNode;
							Element organizerElement = (Element) entryElement.getElementsByTagName("organizer").item(0);

							// Set the encounter date time
							this.setEncounterDatetime(organizerElement.getElementsByTagName("effectiveTime").item(0)
									.getAttributes().getNamedItem("value").getNodeValue());

							for(int j = 0; j < organizerElement.getElementsByTagName("component").getLength(); j++){
								Element componentElement = (Element) organizerElement.getElementsByTagName("component")
										.item(j);

								Element observationElement = (Element)componentElement.getElementsByTagName("observation")
										.item(0);

                                if (observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("856")) {
									// Viral Load in CIEL
									this.flowSheetSummary.setViralLoad(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("viralLoad");
								} else if (observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("1169")) {
									// Final HIV Status/Result in CIEL
									this.flowSheetSummary.setFinalHIVStatus(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("code").getNodeValue());
									this.obsNames.add("finalHIVResult");
								} else if (observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("159599")) {
									// ART Start Date in CIEL
									this.flowSheetSummary.setArtStartDate(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("artStartDate");
								} else if (observationElement.getElementsByTagName("code").item(0).getAttributes()
										.getNamedItem("code").getNodeValue().equals("5096")) {
									// ART Follow Up Date in CIEL
									this.flowSheetSummary.setFollowUpDate(observationElement.getElementsByTagName("value")
											.item(0).getAttributes().getNamedItem("value").getNodeValue());
									this.obsNames.add("artFollowUpDate");
								} else if (observationElement.getElementsByTagName("code").item(0).getAttributes()
		                                .getNamedItem("code").getNodeValue().equals("162240")) {
	                                // ARV Regimen in CIEL
	                                this.flowSheetSummary.setARTRegimen(observationElement.getElementsByTagName("value")
			                                .item(0).getAttributes().getNamedItem("code").getNodeValue());
	                                this.obsNames.add("arvRegimen");
                                }
							}
						}
					}
				}
			}
		} catch (XPathExpressionException ex) {}
	}

	private void getVisitDateTimeFromCDADoc(Document doc){
		try {
			NodeList nodeList = (NodeList) xpath.compile(ENCOUNTER_EFFECTIVE_DATETIME_XPATH_EXPRESSION).evaluate(
					doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					this.setVisitStartDatetime(eElement.getElementsByTagName("low").item(0).getAttributes()
							.getNamedItem("value").getNodeValue());
					this.setVisitStopDatetime(eElement.getElementsByTagName("high").item(0).getAttributes()
							.getNamedItem("value").getNodeValue());
				}
			}
		} catch (XPathExpressionException ex) {}
	}

	private void getLocationFromCDADoc(Document doc){
		try {
			NodeList nodeList = (NodeList) xpath.compile(LOCATION_NAME_XPATH_EXPRESSION).evaluate(
					doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					this.setLocationName(eElement.getElementsByTagName("name").item(0).getTextContent());
				}
			}
		} catch (XPathExpressionException ex) {}
	}

	private void getProvidersFromCDADoc(Document doc){
		try {
			NodeList nodeList = (NodeList) xpath.compile(PROVIDER_ID_XPATH_EXPRESSION).evaluate(
					doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					for(int j = 0; j < eElement.getElementsByTagName("id").getLength(); j++){
						if(eElement.getElementsByTagName("id").item(j).getAttributes()
								.getNamedItem("root").getNodeValue().equals("1.2.3.4.5.10")){
							this.setProviderId(eElement.getElementsByTagName("id").item(j).getAttributes()
									.getNamedItem("extension").getNodeValue());
						}
					}
				}
			}
		} catch (XPathExpressionException ex) {}
	}

	private void getVisitandEncounterUuidFromCDADoc(Document doc){
		try {
			NodeList nodeList = (NodeList) xpath.compile(VISIT_AND_ENCOUNTER_UUID_XPATH_EXPRESSION).evaluate(
					doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					String visitandencounterUuids = eElement.getAttributes().getNamedItem("extension").getNodeValue();
					String[] visitandencounterArray = visitandencounterUuids.split("/");
					this.setVisitUuid(visitandencounterArray[0]);
					this.setEncounterUuid(visitandencounterArray[1]);
				}
			}
		} catch (XPathExpressionException ex) {}
	}

	private void getPatientIdentifiersFromCDADoc(Document doc){
		try {
			NodeList nodeList = (NodeList) xpath.compile(PATIENT_ID_XPATH_EXPRESSION).evaluate(
					doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					for(int j = 0; j < eElement.getElementsByTagName("id").getLength(); j++) {
						if(eElement.getElementsByTagName("id").item(j).getAttributes().getNamedItem("root")
								.getNodeValue().equals("2.25.71280592878078638113873461180761116318")){
							this.setPatientId(
									eElement.getElementsByTagName("id").item(j).getAttributes()
											.getNamedItem("extension").getNodeValue());
						} else if(eElement.getElementsByTagName("id").item(j).getAttributes().getNamedItem("root")
								.getNodeValue().equals("1.3.6.1.4.1.21367.2010.1.2.300")) {
							this.setPatientEcidId(
									eElement.getElementsByTagName("id").item(j).getAttributes()
											.getNamedItem("extension").getNodeValue());
						}
					}
				}
			}
		} catch (XPathExpressionException ex) {}
	}

	public class Vitals {
		private String systolicBP;
		private String diastolicBP;
		private String temperature;
		private String height;
		private String weight;

		public Vitals(){

		}

		public String getSystolicBP() {return this.systolicBP; }
		public void setSystolicBP(String systolicBP){ this.systolicBP = systolicBP; }

		public String getDiastolicBP() { return this.diastolicBP; }
		public void setDiastolicBP(String diastolicBP) { this.diastolicBP = diastolicBP; }

		public String getTemperature() { return this.temperature; }
		public void setTemperature(String temperature) { this.temperature = temperature; }

		public String getHeight() { return this.height; }
		public void setHeight(String height) { this.height = height; }

		public String getWeight() { return this.weight; }
		public void setWeight(String weight) { this.weight = weight; }

		public Double getBMI() { return calculateBMI(); }

		private Double calculateBMI(){
			Double weightKg = Double.parseDouble(this.getWeight());
			Double heightCM = Double.parseDouble(this.getHeight());
			Double heightM = (Double) (heightCM)/100;

			Double patientBMI = weightKg/(heightM*heightM);

			return patientBMI;
		}
	}

	public class FlowSheetSummary {
		private String finalHIVStatus;
		private String viralLoad;
		private String artStartDate;
		private String artRegimen;
		private String followUpDate;

		public FlowSheetSummary(){

		}

		public String getFinalHIVStatus() {return this.finalHIVStatus; }
		public void setFinalHIVStatus(String finalHIVStatus){ this.finalHIVStatus = finalHIVStatus; }

		public String getViralLoad() { return this.viralLoad; }
		public void setViralLoad(String viralLoad) { this.viralLoad = viralLoad; }

		public String getArtStartDate() { return this.artStartDate; }
		public void setArtStartDate(String artStartDate) { this.artStartDate = artStartDate; }

		public String getARTRegimen() { return this.artRegimen; }
		public void setARTRegimen(String artRegimen) { this.artRegimen = artRegimen; }

		public String getFollowUpDate() { return this.followUpDate; }
		public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }

	}
}
