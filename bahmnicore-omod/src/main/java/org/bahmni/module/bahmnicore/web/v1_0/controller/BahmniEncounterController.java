package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.xpath.operations.Bool;
import org.bahmni.module.bahmnicore.web.v1_0.VisitClosedException;

import org.joda.time.DateTime;
import org.openmrs.Encounter;
import org.openmrs.Visit;


import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterSearchParameters;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;


//import org.openmrs.module.appointments.model.Appointment;
//import org.openmrs.module.appointments.model.AppointmentService;
//import org.openmrs.module.appointments.model.AppointmentServiceType;
//import org.openmrs.module.appointments.model.AppointmentStatus;
//import org.openmrs.module.appointments.model.Appointment;

import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.stream.Collectors;

import static org.bahmni.module.bahmnicore.util.MiscUtils.getFollowUpDateObservationRecursive;
import static org.bahmni.module.bahmnicore.util.MiscUtils.setUuidsForObservations;
import static org.bahmni.module.bahmnicore.util.MiscUtils.getFollowUpDateObservation;
import static org.bahmni.module.bahmnicore.util.MiscUtils.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniencounter")
public class BahmniEncounterController extends BaseRestController {
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;
	
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";
	
	private static final String TIME_PATTERN = "HH:mm";
	
	private static final String DATE_PATTERN = "yyyy-MM-dd";
		
    @Autowired
    LocationService locationService;

    @Autowired
    ProviderService providerService;

    @Autowired
    PatientService patientService;

    @Autowired
    AppointmentServiceService appointmentServiceService;

    @Autowired
    AppointmentsService appointmentsService;


    public BahmniEncounterController() {
    }

    @Autowired
    public BahmniEncounterController(EncounterService encounterService,
                                     EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper,
                                     BahmniEncounterTransactionService bahmniEncounterTransactionService,
                                     BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper) {
        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.bahmniEncounterTransactionService = bahmniEncounterTransactionService;
        this.bahmniEncounterTransactionMapper = bahmniEncounterTransactionMapper;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
    @ResponseBody
    public BahmniEncounterTransaction get(@PathVariable("uuid") String uuid, @RequestParam(value = "includeAll", required = false) Boolean includeAll) {
        EncounterTransaction encounterTransaction = emrEncounterService.getEncounterTransaction(uuid, includeAll);
        return bahmniEncounterTransactionMapper.map(encounterTransaction, includeAll);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/find")
    @ResponseBody
    public BahmniEncounterTransaction find(@RequestBody BahmniEncounterSearchParameters encounterSearchParameters) {
        EncounterTransaction encounterTransaction = bahmniEncounterTransactionService.find(encounterSearchParameters);

        if (encounterTransaction != null) {
            return bahmniEncounterTransactionMapper.map(encounterTransaction, encounterSearchParameters.getIncludeAll());
        } else {
            return bahmniEncounterTransactionMapper.map(new EncounterTransaction(), false);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{uuid}")
    @ResponseBody
    public void delete(@PathVariable("uuid") String uuid, @RequestParam(value = "reason", defaultValue = "web service call") String reason){
        String errorMessage = "Visit for this patient is closed. You cannot do an 'Undo Discharge' for the patient.";
        Visit visit = encounterService.getEncounterByUuid(uuid).getVisit();
        Date stopDate = visit.getStopDatetime();
        if(stopDate != null && stopDate.before(new Date())){
            throw new VisitClosedException(errorMessage);
        }
        else{
            BahmniEncounterTransaction bahmniEncounterTransaction = get(uuid,false);
            bahmniEncounterTransaction.setReason(reason);
            bahmniEncounterTransactionService.delete(bahmniEncounterTransaction);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public BahmniEncounterTransaction update(@RequestBody BahmniEncounterTransaction bahmniEncounterTransaction) {
        setUuidsForObservations(bahmniEncounterTransaction.getObservations());
		//BahmniObservation artFollowUpObservation = getFollowUpDateObservationRecursive(
        //        bahmniEncounterTransaction.getObservations()
        //        , "ART, Follow-up date");

        BahmniObservation artFollowUpObservation = getFollowUpDateObservation(
                bahmniEncounterTransaction.getObservations());

        if(artFollowUpObservation != null){
            createAppointmentForARTPatient(artFollowUpObservation, bahmniEncounterTransaction);
        }
        return bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
    }

    public BahmniEncounterTransaction get(String encounterUuid) {
        Encounter encounter = encounterService.getEncounterByUuid(encounterUuid);
        boolean includeAll = false;
        EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, includeAll);
        return bahmniEncounterTransactionMapper.map(encounterTransaction, includeAll);
    }

    private AppointmentServiceType getServiceTypeByUuid(Set<AppointmentServiceType> serviceTypes, String serviceTypeUuid) {
        return serviceTypes.stream()
                .filter(avb -> avb.getUuid().equals(serviceTypeUuid)).findAny().get();
    }

    private void createAppointmentForARTPatient(BahmniObservation bahmniObs, BahmniEncounterTransaction bahmniEncounterTransaction){
        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Date startDate = new Date();
        Date endDate = new Date();

        if(bahmniObs != null){
            // The current encounter has a follow up date
            try {
                startDate = dateFormat.parse(bahmniObs.getValueAsString());
                endDate = dateFormat.parse(bahmniObs.getValueAsString());

                Appointment appointment = new Appointment();

                // Get the current location
                Location location = locationService.getLocationByUuid(bahmniEncounterTransaction.getLocationUuid());

                // For a particular patient
                appointment.setPatient(patientService.getPatientByUuid(bahmniEncounterTransaction.getPatientUuid()));

                AppointmentService appointmentService = null;
                AppointmentServiceType appointmentServiceType = null;

                String appService = getAppointmentService(
                        bahmniEncounterTransaction.getObservations());
                String FollowUp = getObs(
                        bahmniEncounterTransaction.getObservations());

                // all follow up dates concepts in the forms
                Map<String, String> followUp = new HashMap<>();
                followUp.put("artfollowupUuid", "88489023-783b-4021-b7a9-05ca9877bf67");
                followUp.put("tbfollowupUuid", "0850f585-be36-4458-b53f-f5520910b343");
//              followUp.put("ancfollowupUuid", "96b7bcbb-a6c9-4648-bce3-c662411dab7b");
                followUp.put("pncfollowupUuid", "7ccb96a9-f6bb-426b-ae59-9746ccd8681e");
                Iterator<Map.Entry<String,String>>  followUpIterator = followUp.entrySet().iterator();

                Map<String, String> service = new HashMap<>();
                //the type of service determined by the form being field  service.put
                service.put("03a7cac1-2562-4151-9f3e-8f07c6c94731", "0c8dfd62-776a-4ddd-bcee-f2570c0721fa");
                service.put("746818ac-65a0-4d74-9609-ddb2c330a31b", "0c8dfd62-776a-4ddd-bcee-f2570c0721fa");
                service.put("4276dcf6-0f21-4910-bac3-87ddc94f88c9", "886757b6-52fd-45e6-bb55-f401176acd0c");
                service.put("059ff360-2089-4e3e-87f8-4a23c334ebbc", "e3d08a87-3e42-42cd-bf71-5e685b34e4a4");
//              service.put("e3d08a87-3e42-42cd-bf71-5e685b34e4a4", "38ac3c12-f575-42b5-9d7c-0d03d154befc");
                Iterator<Map.Entry<String,String>>  serviceIterator = service.entrySet().iterator();

                Map<String, String> serviceType = new HashMap<>();
                serviceType.put("03a7cac1-2562-4151-9f3e-8f07c6c94731", "257dcd02-e539-46fb-b61c-b23e413935c2");
                serviceType.put("746818ac-65a0-4d74-9609-ddb2c330a31b", "257dcd02-e539-46fb-b61c-b23e413935c2"); //hiv service point
                serviceType.put("4276dcf6-0f21-4910-bac3-87ddc94f88c9", "9586e036-d9b3-42f0-a724-95eb95c91897");
                serviceType.put("e142a2be-c4f3-4ac7-8a9a-2f49e139530c", "059ff360-2089-4e3e-87f8-4a23c334ebbc");
//              serviceType.put("e142a2be-c4f3-4ac7-8a9a-2f49e139530c", "38ac3c12-f575-42b5-9d7c-0d03d154befc");
                Iterator<Map.Entry<String,String>>  serviceTypeIterator = serviceType.entrySet().iterator();

                    while (followUpIterator.hasNext()) {
                        Map.Entry followUpEntry = followUpIterator.next();

                        if(followUpEntry.getValue().equals(FollowUp)){
                            while (serviceIterator.hasNext()) {
                                Map.Entry serviceEntry = serviceIterator.next();
                                if(serviceEntry.getKey().equals(appService)){
                                    appointmentService = appointmentServiceService
                                        .getAppointmentServiceByUuid(serviceEntry.getValue().toString());

                                    while (serviceTypeIterator.hasNext()) {
                                        Map.Entry serviceTypeEntry = serviceTypeIterator.next();
                                        if(serviceTypeEntry.getKey().equals(appService)){
                                            appointmentServiceType = getServiceTypeByUuid(appointmentService.getServiceTypes(true)
                                                    , serviceTypeEntry.getValue().toString());
                                        }
                                    }
                                }
                            }
                        }
                    }

                // Set the appointment service type
                appointment.setServiceType(appointmentServiceType);

                // Set the appointment Location
                appointment.setLocation(location);

                if(bahmniEncounterTransaction.getEncounterUuid() == null){
                    // New encounter, first time an HIV Intake or Follow up is saved
                    appointment.setService(appointmentService);

                    if(!bahmniEncounterTransaction.getProviders().isEmpty())
                    {
                        appointment.setProvider(providerService.getProviderByUuid(bahmniEncounterTransaction
                                .getProviders().iterator().next().getUuid()));
                    }

                    appointment.setStartDateTime(startDate);
                    appointment.setEndDateTime(endDate);
                    appointment.setAppointmentKind(AppointmentKind.valueOf("Scheduled"));
                    appointment.setComments("");

                    appointmentsService.validateAndSave(appointment);

                } else if(bahmniEncounterTransaction.getEncounterUuid() != null){
                    if(new DateTime(bahmniEncounterTransaction.getEncounterDateTime()).toDateMidnight()
                            .equals(new DateTime(new Date()).toDateMidnight())){

                        // Search for the appointment using the parameters, patient, service type and location
                        List<Appointment> foundAppointments = appointmentsService.search(appointment);

                        if (foundAppointments.iterator().hasNext()) {
                            // Can only have one and only one future appointment for the patient
                            // Sort the list in descending order first
                            List<Appointment> sortedAppointments = foundAppointments.stream()
                                    .sorted(Comparator.comparing(Appointment::getStartDateTime).reversed())
                                    .collect(Collectors.toList());

                            Appointment searchedAppointment = sortedAppointments.iterator().next();

                            // Check to see if the Follow Up date is in the future compared to today's date, only update
                            // an appointment in the future
                            if(new DateTime(searchedAppointment.getEndDateTime()).toDateMidnight()
                                    .isAfter(new DateTime(new Date()).toDateMidnight())) {
                                // Update/Edit appointment with the new Follow up date if set in the future for the patient
                                // , in same location, for same service
                                searchedAppointment.setStartDateTime(startDate);
                                searchedAppointment.setEndDateTime(endDate);
                                appointmentsService.validateAndSave(searchedAppointment);
                            } else {
                                // Create a new future appointment for patient, for service in location
                                appointment.setService(appointmentService);

                                if(!bahmniEncounterTransaction.getProviders().isEmpty())
                                {
                                    appointment.setProvider(providerService.getProviderByUuid(bahmniEncounterTransaction
                                            .getProviders().iterator().next().getUuid()));
                                }

                                appointment.setStartDateTime(startDate);
                                appointment.setEndDateTime(endDate);
                                appointment.setAppointmentKind(AppointmentKind.valueOf("Scheduled"));
                                appointment.setComments("");

                                appointmentsService.validateAndSave(appointment);
                            }
                        } else {
                            // New encounter, first time an HIV Intake or Follow up is saved
                            appointment.setService(appointmentService);

                            if(!bahmniEncounterTransaction.getProviders().isEmpty())
                            {
                                appointment.setProvider(providerService.getProviderByUuid(bahmniEncounterTransaction
                                        .getProviders().iterator().next().getUuid()));
                            }

                            appointment.setStartDateTime(startDate);
                            appointment.setEndDateTime(endDate);
                            appointment.setAppointmentKind(AppointmentKind.valueOf("Scheduled"));
                            appointment.setComments("");

                            appointmentsService.validateAndSave(appointment);
                        }
                    }
                } else if(new DateTime(bahmniEncounterTransaction.getEncounterDateTime()).toDateMidnight()
                        .isBefore(new DateTime(new Date()).toDateMidnight())) {
                    // Happened before today and has a follow up date, therefore an EDIT of a retrospective visit

                    // Do nothing, determine how to tie a retrospective encounter with a particular appointment date
                    // The first date in the future as compared to the retrospective visit date
                    // getAllAppointmentsInDateRange

                    // Get all appointments in the future compared to the retrospective encounter date
                    List<Appointment> foundAppointments = appointmentsService.getAllAppointmentsInDateRange(
                            bahmniEncounterTransaction.getEncounterDateTime(), new Date());

                    if (foundAppointments.iterator().hasNext()) {
                        // Can only have one and only one future appointment for the patient
                        // Sort the list in ascending order, smallest date in future compared to retrospective
                        // encounter date first
                        List<Appointment> sortedAppointments = foundAppointments.stream()
                                .sorted(Comparator.comparing(Appointment::getStartDateTime))
                                .collect(Collectors.toList());

                        Appointment searchedAppointment = sortedAppointments.iterator().next();

                        // Check to see if the Follow Up date is in the future compared to the retrospective
                        // encounter date, only update the first appointment in the future relative to encounter date
                        if(new DateTime(searchedAppointment.getEndDateTime()).toDateMidnight()
                                .isAfter(new DateTime(bahmniEncounterTransaction.getEncounterDateTime())
                                        .toDateMidnight().plusDays(10))) {
                            // Update/Edit appointment with the new Follow up date if set in the future for the patient
                            // , in same location, for same service
                            searchedAppointment.setStartDateTime(startDate);
                            searchedAppointment.setEndDateTime(endDate);
                            appointmentsService.validateAndSave(searchedAppointment);
                        }
                    }
                }
            } catch (ParseException e) {
                // Use the openmrs logger to log the exception
            }
        }
    }
}
