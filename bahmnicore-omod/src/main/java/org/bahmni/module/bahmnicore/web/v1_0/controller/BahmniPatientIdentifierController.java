package org.bahmni.module.bahmnicore.web.v1_0.controller;

import java.util.List;
import org.bahmni.module.bahmnicore.web.v1_0.contract.AssignIdentifierRequest;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
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

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/patientassignid")
public class BahmniPatientIdentifierController extends BaseRestController {

    @Autowired
    PatientService patientService;

    public BahmniPatientIdentifierController() {
    }

    @Autowired
    public BahmniPatientIdentifierController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> assign(@RequestBody AssignIdentifierRequest identifier) throws Exception {
        try {
            Patient patient = patientService.getPatientByUuid(identifier.getPatientUuid());
            PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByName(identifier.getPatientIdentifierType());
            PatientIdentifier identifierObj = new PatientIdentifier();
            identifierObj.setIdentifier(identifier.getIdentifier());
            identifierObj.setIdentifierType(patientIdentifierType);
            patient.addIdentifier(identifierObj);
            patientService.savePatient(patient);
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(identifier.getIdentifier()), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
