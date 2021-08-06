package org.bahmni.module.bahmnicore.web.v1_0.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;

import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.bahmni.module.bahmnicore.contract.encounter.data.AdhocQueryDocumentModel;
import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.contract.encounter.data.PersonObservationData;
import org.bahmni.module.bahmnicore.model.XdsAdhocQueryData;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.bahmni.module.bahmnicore.service.BahmniXDSService;
import org.bahmni.module.bahmnicore.web.v1_0.contract.CDADummyDoc;
import org.bahmni.module.bahmnicore.web.v1_0.contract.CDAEncounterDocument;
import org.bahmni.module.bahmnicore.web.v1_0.contract.SharedHealthRecordSearchParams;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryResponse;
import org.dcm4chee.xds2.infoset.rim.ExternalIdentifierType;
import org.dcm4chee.xds2.infoset.rim.ExtrinsicObjectType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Encounter;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.VisitType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.EncounterService;

import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.xdssender.api.model.AdhocQueryDocumentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/sharedhealthrecord")
public class BahmniSharedHealthRecordController extends BaseRestController {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private static final String TIME_PATTERN = "HH:mm";

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final String DATE_PATTERN_LONG = "yyyyMMddHHmm";

    private static final String DATE_PATTERN_LONG_TEST = "yyyyMMddHHmmssSSS";

    private EncounterTransactionMapper encounterTransactionMapper;
    private EmrEncounterService emrEncounterService;
    private BahmniXDSService xdsService;

    //private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    /*@Autowired
    private BahmniXDSService xdsService;*/

    @Autowired
    LocationService locationService;

    @Autowired
    BahmniPatientService bahmniPatientService;

    @Autowired
    ProviderService providerService;

    @Autowired
    PersonService personService;

    @Autowired
    PatientService patientService;

    @Autowired
    EncounterService encounterService;

    @Autowired
    VisitService visitService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    ObsService obsService;

    @Autowired
    public BahmniSharedHealthRecordController(BahmniXDSService xdsService, EncounterTransactionMapper encounterTransactionMapper,
            EmrEncounterService emrEncounterService) {
        this.xdsService = xdsService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.emrEncounterService = emrEncounterService;
    }

    public BahmniSharedHealthRecordController() {
    }

    /*@RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<AdhocQueryDocumentData> get(@RequestParam(value = "patientUUID", required = true) String patientUUID) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Date startDate = new Date();
        Date endDate = new Date();
        List<AdhocQueryDocumentData> response = xdsService.FindDcoumentsQuery(patientUUID, startDate, endDate);
        return response;
    }*/

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AlreadyPaged<AdhocQueryDocumentData>> search(HttpServletRequest request,
            HttpServletResponse response) throws ResponseException {
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        SharedHealthRecordSearchParams searchParams = new SharedHealthRecordSearchParams(requestContext);

        try {
            List<AdhocQueryDocumentData> documents = xdsService.FindDocumentsQuery(searchParams.getPatientIdentifier(),
                    searchParams.getFromDate(), searchParams.getToDate());

            AlreadyPaged alreadyPaged = new AlreadyPaged(requestContext, documents, false);
            return new ResponseEntity(alreadyPaged, HttpStatus.OK);
        }catch (IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ResponseEntity<CDADummyDoc> retriveAndImportDocument(@RequestParam(value = "documentId", required = true) String documentId) {
        String docID = documentId;
        String response = xdsService.RetrieveDocumentSet(documentId);
        CDADummyDoc document = new CDADummyDoc(String.format("Document %s imported from the HIE successfully!", documentId), documentId, true);

        CDAEncounterDocument cdaEncounterDocument = new CDAEncounterDocument(response);
        cdaEncounterDocument.populateEncounterFromCDADocument();

        // 6. Use the CDA Document output to create Visits, Encounters and Obervations for storage into OpenMRS DB

        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN_LONG);
        DateFormat encounterDateFormart = new SimpleDateFormat(DATE_PATTERN_LONG_TEST);

        // TODO: REMEMBER TO PUT IN CONTROLS TO CHECK WHETHER THE ENCOUNTER HAS ALREADY BEEN SAVED!!!

        try {
            List<Patient> patients = bahmniPatientService.get(cdaEncounterDocument.getPatientId(), true);
            Provider provider = providerService.getProviderByIdentifier("37-2");
            EncounterType encounterType = encounterService.getEncounterTypeByUuid("81852aee-3f10-11e4-adec-0800271c1b75");
            List<Location> locations = locationService.getLocations(cdaEncounterDocument.getLocationName());

            Visit visit = new Visit();
            Patient patient = null;
            Location location = null;

            if(patients.iterator().hasNext()) {
                patient = patients.iterator().next();
            }

            if(locations.iterator().hasNext()) {
                location = locations.iterator().next();
            }

            if(patient != null && location != null){

                // Check if the current visit and encounter have already been saved to the database
                Visit existingVisit = visitService.getVisitByUuid(cdaEncounterDocument.getVisitUuid());
                Encounter existingEncounter = encounterService.getEncounterByUuid(cdaEncounterDocument.getEncounterUuid());

                if(existingVisit == null && existingEncounter == null) {
                    visit.setLocation(location);
                    visit.setPatient(patient);
                    visit.setStartDatetime(dateFormat.parse(cdaEncounterDocument.getVisitStartDatetime().toString().substring(0, 12)));
                    visit.setStopDatetime(dateFormat.parse(cdaEncounterDocument.getVisitStopDatetime().toString().substring(0, 12)));
                    visit.setEncounters(new HashSet());
                    visit.setUuid(cdaEncounterDocument.getVisitUuid());
                    visit.setVisitType(visitService.getVisitTypeByUuid("cda0acfe-07f2-41f3-ab54-cdad1d3d452a"));

                    Encounter encounter = new Encounter();
                    //encounter.setEncounterDatetime(encounterDateFormart.parse(cdaEncounterDocument.getEncounterDatetime()));
                    String encounterDateTrim = cdaEncounterDocument.getEncounterDatetime().toString().substring(0, 12);
                    //encounter.setEncounterDatetime(getDateFromString(cdaEncounterDocument.getEncounterDatetime()));
                    encounter.setEncounterDatetime(dateFormat.parse(encounterDateTrim));
                    encounter.setLocation(location);
                    encounter.setPatient(patient);
                    encounter.setEncounterType(encounterType);
                    encounter.setUuid(cdaEncounterDocument.getEncounterUuid());
                    encounter.setObs(new HashSet());

                    Encounter savedEncounter = encounterService.saveEncounter(encounter);
                    visit.addEncounter(savedEncounter);
                    visitService.saveVisit(visit);

                    for (String obsName : cdaEncounterDocument.getObsNames()) {
                        Concept concept = new Concept();
                        Obs obs = new Obs();

                        if (obsName.equalsIgnoreCase("height")) {
                            concept = conceptService.getConcept("HEIGHT");
                            obs = generateObs(concept, location, patient, cdaEncounterDocument.getVitals().getHeight(),
                                    cdaEncounterDocument.getEncounterDatetime(), savedEncounter);
                        } else if (obsName.equalsIgnoreCase("weight")) {
                            concept = conceptService.getConcept("WEIGHT");
                            obs = generateObs(concept, location, patient, cdaEncounterDocument.getVitals().getWeight(),
                                    cdaEncounterDocument.getEncounterDatetime(), savedEncounter);
                        } else if (obsName.equalsIgnoreCase("temperature")) {
                            concept = conceptService.getConcept("Temperature");
                            obs = generateObs(concept, location, patient, cdaEncounterDocument.getVitals().getTemperature(),
                                    cdaEncounterDocument.getEncounterDatetime(), savedEncounter);
                        } else if (obsName.equalsIgnoreCase("systolic")) {
                            concept = conceptService.getConcept("Systolic");
                            obs = generateObs(concept, location, patient, cdaEncounterDocument.getVitals().getSystolicBP(),
                                    cdaEncounterDocument.getEncounterDatetime(), savedEncounter);
                        } else if (obsName.equalsIgnoreCase("diastolic")) {
                            concept = conceptService.getConcept("Diastolic");
                            obs = generateObs(concept, location, patient, cdaEncounterDocument.getVitals().getDiastolicBP(),
                                    cdaEncounterDocument.getEncounterDatetime(), savedEncounter);
                        }
                        obsService.saveObs(obs, "From HIE");
                    }
                    // Create the BMI Obs
                    Concept concept = conceptService.getConcept("BMI");
                    Obs bmiObs = generateObs(concept, location, patient,
                            cdaEncounterDocument.getVitals().getBMI().toString(),
                            cdaEncounterDocument.getEncounterDatetime(), savedEncounter);
                    obsService.saveObs(bmiObs, "From HIE");
                } else {
                    document.setMessage(String.format("Document %s not imported. Related encounter and visit already exist.", documentId));
                    document.setIsSuccesful(false);
                }
            } else {
                document.setMessage(String.format("Document %s not imported. Unknown location.", documentId));
                document.setIsSuccesful(false);
            }
            // TODO: Also persist orignial document in DB as-is OR DO THIS IN THE XDS SENDER MODULE
            return new ResponseEntity<CDADummyDoc>(document, HttpStatus.OK);
        } catch (ParseException ex){
            return null;
        }
    }

    private Obs generateObs(Concept concept, Location location, Patient patient
            , String value, String encounterDatetime, Encounter savedEncounter) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN_LONG);
        Obs obs = new Obs();
        obs.setConcept(concept);
        obs.setUuid(UUID.randomUUID().toString());
        obs.setLocation(location);
        obs.setPerson(patient);
        obs.setObsDatetime(dateFormat.parse(encounterDatetime));
        obs.setValueNumeric(Double.parseDouble(value));
        obs.setEncounter(savedEncounter);

        return obs;
    }

    private Date getDateFromString(String birthdate) {
        if (StringUtils.isNotBlank(birthdate)) {
            IllegalArgumentException pex = null;
            String[] supportedFormats = { "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyyMMddHHmm" };
            for (int i = 0; i < supportedFormats.length; i++) {
                try {
                    Date date = DateTime.parse(birthdate, DateTimeFormat.forPattern(supportedFormats[i])).toDate();
                    return date;
                }
                catch (IllegalArgumentException ex) {
                    pex = ex;
                }
            }

            throw new ConversionException(
                    "Error converting date - correct format (ISO8601 Long): yyyy-MM-dd'T'HH:mm:ss.SSSZ", pex);
        }
        return null;
    }
}