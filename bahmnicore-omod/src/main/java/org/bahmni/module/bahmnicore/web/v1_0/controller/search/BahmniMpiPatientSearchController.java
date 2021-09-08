package org.bahmni.module.bahmnicore.web.v1_0.controller.search;

import static java.util.stream.Collectors.toList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.mapper.PatientResponseMapper;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.web.v1_0.contract.PatientContract;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationServiceImpl;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for REST web service access to
 * the Search resource.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/search/mpipatient")
public class BahmniMpiPatientSearchController extends BaseRestController {

    public static final int MAX_RESULTS = 10;
    public static final double CUTOFF = 2.0;
    private BahmniPatientService bahmniPatientService;


    // The localIdentifierType in Bahmni will always be the same for all sites, the difference is with the MPI Identifier Type Map
    // which will be different for different sites. The type mapping is configured in OpenMRS global properties
    private static final String localIdentifierTypeUuid = "81433852-3f10-11e4-adec-0800271c1b75";

    private static Map<String, String> remoteIdentifierTypeUuidMap =  new HashMap<>();

    // TODO: CREATE A HASH MAP OF FACILITY CODES TO RELEVANT OPENEMPI IDENTIFIER DOMAINS FOR MONDAY'S DEMONSTATION
    // USE THE HASMAP TO DETERMINE THE IDENTIFIER DOMAIN OID

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    PatientService patientService;

    @Autowired
    public BahmniMpiPatientSearchController(BahmniPatientService bahmniPatientService) {
        this.bahmniPatientService = bahmniPatientService;
        remoteIdentifierTypeUuidMap.put("B1011", "81433852-3f10-11e4-adec-0800271c1b75");
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AlreadyPaged<PatientContract>> search(HttpServletRequest request,
                                                  HttpServletResponse response) throws ResponseException{
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);

        List<PatientContract> patientContracts = new ArrayList<>();

        // Convert to Title case
        searchParameters.setName(WordUtils.capitalizeFully(searchParameters.getName()));

        String[] names = searchParameters.getName().split(" ");
        //Create a basic patient from the search parameters
        Patient patientToSearch = new Patient();

        if(names.length == 1) {
            patientToSearch.addName(new PersonName(names[0].toString().trim(), "", ""));
        } else if(names.length == 2){
            patientToSearch.addName(new PersonName(names[0].toString().trim(), "", names[1].toString().trim()));
        }

        PersonAddress personAddress = new PersonAddress();
        // Converted to UpperCase
        // personAddress.setAddress2(searchParameters.getAddressFieldValue().toUpperCase());
        personAddress.setAddress2(searchParameters.getAddressFieldValue());
        patientToSearch.addAddress(personAddress);

        // Set the Gender
        patientToSearch.setGender(searchParameters.getGender());

        // Create other data points
        Map<String, Object> otherDataPoints = createDataPoints(searchParameters.getNationalId());

        Object registrationCoreService = applicationContext.getBean("registrationCoreService");

        try {
            Object patientList = MethodUtils.invokeMethod(registrationCoreService, "findFastSimilarOMRSPatients",
                    patientToSearch, otherDataPoints, CUTOFF, MAX_RESULTS);

            List<Patient> patients = ((List<Patient>) patientList);

            for(Patient patient : patients) {
                List<String> patientSearchResultFields = new ArrayList<>();
                List<String> addressSearchResultFields = new ArrayList<>();

                PatientContract patientContract = new PatientContract();
                patientContract.setGender(patient.getGender());
                patientContract.setBirthDate(patient.getBirthdate());
                patientContract.setAge(patientContract.getAge());
                patientContract.setGivenName(patient.getGivenName());
                patientContract.setMiddleName(patient.getMiddleName());
                patientContract.setFamilyName(patient.getFamilyName());
                patientContract.setUuid(patient.getUuid());

                if(patient.getAddresses().iterator().hasNext()){
                    PersonAddress address = patient.getAddresses().iterator().next();
                    patientContract.setConstituency(address.getAddress1());
                    patientContract.setVillage(address.getCityVillage());
                    patientContract.setDistrict(address.getStateProvince());
                }

                PatientIdentifier primaryIdentifier = patient.getPatientIdentifier();
                patientContract.setIdentifier(primaryIdentifier.getIdentifier());

                mapExtraIdentifiers(patient, primaryIdentifier, patientContract);
                //mapPersonAttributes(patient, patientSearchResultFields, patientResponse);

                patientContracts.add(patientContract);
            }

            AlreadyPaged alreadyPaged = new AlreadyPaged(requestContext, patientContracts, false);
            return new ResponseEntity(alreadyPaged,HttpStatus.OK);
        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="exact", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AlreadyPaged<PatientContract>> exactSearch(HttpServletRequest request,
                                                  HttpServletResponse response) throws ResponseException {
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);

        List<PatientResponse> patientResponses = new ArrayList<>();
        Object registrationCoreService = applicationContext.getBean("registrationCoreService");


        String identifierTypeUuid = localIdentifierTypeUuid;

        if(searchParameters.getIdentifier().contains("B1011")){
            identifierTypeUuid = remoteIdentifierTypeUuidMap.get("B1011");
        }

        List<PatientContract> patientContracts = new ArrayList<>();

        try {
            Object mpiResponse = MethodUtils.invokeMethod(registrationCoreService, "findOMRSPatient",
                    searchParameters.getIdentifier(), identifierTypeUuid);

                Patient patient = (Patient)mpiResponse;

                //List<String> patientSearchResultFields = new ArrayList<>();
                //List<String> addressSearchResultFields = new ArrayList<>();

                PatientContract patientContract = new PatientContract();
                patientContract.setGender(patient.getGender());
                patientContract.setBirthDate(patient.getBirthDateTime());
                patientContract.setAge(patientContract.getAge());
                patientContract.setGivenName(patient.getGivenName());
                patientContract.setMiddleName(patient.getMiddleName());
                patientContract.setFamilyName(patient.getFamilyName());
                patientContract.setUuid(patient.getUuid());

                if(patient.getAddresses().iterator().hasNext()){
                    PersonAddress personAddress = patient.getAddresses().iterator().next();
                    patientContract.setConstituency(personAddress.getAddress1());
                    patientContract.setVillage(personAddress.getCityVillage());
                    patientContract.setDistrict(personAddress.getStateProvince());
                }

                PatientIdentifier primaryIdentifier = patient.getPatientIdentifier();
                patientContract.setIdentifier(primaryIdentifier.getIdentifier());

                mapExtraIdentifiers(patient, primaryIdentifier, patientContract);
                //mapPersonAttributes(patient, patientSearchResultFields, patientResponse);

                patientContracts.add(patientContract);

                AlreadyPaged alreadyPaged = new AlreadyPaged(requestContext, patientContracts, false);
                return new ResponseEntity(alreadyPaged,HttpStatus.OK);
        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ResponseEntity<PatientContract> importPatient(@RequestParam(value = "patientEcid", required = true) String patientEcid) {
        String ecid = patientEcid;
        Object registrationCoreService = applicationContext.getBean("registrationCoreService");

        List<PatientContract> patientContracts = new ArrayList<>();

        try {
            Object mpiResponse = MethodUtils.invokeMethod(registrationCoreService, "importMpiPatient", patientEcid);
            Patient savedPatient = (Patient) mpiResponse;

            PatientContract patientContract = new PatientContract();
            patientContract.setGender(savedPatient.getGender());
            patientContract.setBirthDate(savedPatient.getBirthDateTime());
            patientContract.setAge(patientContract.getAge());
            patientContract.setGivenName(savedPatient.getGivenName());
            patientContract.setMiddleName(savedPatient.getMiddleName());
            patientContract.setFamilyName(savedPatient.getFamilyName());
            patientContract.setUuid(savedPatient.getUuid());

            if(savedPatient.getAddresses().iterator().hasNext()){
                PersonAddress personAddress = savedPatient.getAddresses().iterator().next();
                patientContract.setConstituency(personAddress.getAddress1());
                patientContract.setVillage(personAddress.getCityVillage());
                patientContract.setDistrict(personAddress.getStateProvince());
            }

            PatientIdentifier primaryIdentifier = savedPatient.getPatientIdentifier();
            patientContract.setIdentifier(primaryIdentifier.getIdentifier());

            patientContracts.add(patientContract);

            return new ResponseEntity(patientContracts ,HttpStatus.OK);

        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, Object> createDataPoints(String nationalId) {
        Map<String, Object> otherDataPoints = new HashMap<String, Object>();
        if(StringUtils.isNotEmpty(nationalId)) {
            otherDataPoints.put("nationalID", nationalId);
        }
        return otherDataPoints;
    }

    private void mapExtraIdentifiers(Patient patient, PatientIdentifier primaryIdentifier, PatientContract patientResponse) {
        String extraIdentifiers = patient.getActiveIdentifiers().stream()
                .filter(patientIdentifier -> (patientIdentifier != primaryIdentifier))
                .map(patientIdentifier -> {
                    String identifier = patientIdentifier.getIdentifier();
                    return identifier == null ? ""
                            : formKeyPair(patientIdentifier.getIdentifierType().getName(), identifier);
                })
                .collect(Collectors.joining(","));
        patientResponse.setExtraIdentifiers(formJsonString(extraIdentifiers));
    }

    private void mapPersonAttributes(Patient patient, List<String> patientSearchResultFields, PatientResponse patientResponse) {
        String queriedPersonAttributes = patientSearchResultFields.stream()
                .map(attributeName -> {
                    PersonAttribute attribute = patient.getAttribute(attributeName);
                    return attribute == null ? null : formKeyPair(attributeName, attribute.getValue());
                }).filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        patientResponse.setCustomAttribute(formJsonString(queriedPersonAttributes));
    }

    private void mapPersonAddress(Patient patient, List<String> addressSearchResultFields, PatientResponse patientResponse) {
        String queriedAddressFields = addressSearchResultFields.stream()
                .map(addressField -> {
                    String address = getPersonAddressFieldValue(addressField, patient.getPersonAddress());
                    return address == null ? null : formKeyPair(addressField, address);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        patientResponse.setAddressFieldValue(formJsonString(queriedAddressFields));
    }

    private String formJsonString(String keyPairs) {
        return "".equals(keyPairs) ? null :"{" + keyPairs + "}";
    }

    private String formKeyPair(String Key, String value) {
        return (value== null) ? value : "\"" + Key + "\" : \"" + value.replace("\\","\\\\").replace("\"","\\\"") + "\"";
    }

    private String getPersonAddressFieldValue(String addressField, PersonAddress personAddress) {
        String address = "";
        try {
            String[] split = addressField.split("_");
            String propertyName = split.length > 1 ? split[0] + StringUtils.capitalize(split[1]) : addressField;
            address = (String) PropertyUtils.getProperty(personAddress, propertyName);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new APIException("cannot get value for address field" + addressField, e);
        }
        return address;
    }
}
