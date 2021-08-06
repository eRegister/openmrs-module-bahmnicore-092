package org.bahmni.module.bahmnicore.util;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;

//import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.*;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;



public class MiscUtils {
    public static List<Concept> getConceptsForNames(List<String> conceptNames, ConceptService conceptService) {
        //Returning null for the sake of UTs
        if (CollectionUtils.isNotEmpty(conceptNames)) {
            List<Concept> rootConcepts = new ArrayList<>();
            for (String rootConceptName : conceptNames) {
                Concept concept = conceptService.getConceptByName(rootConceptName);
                if (concept != null) {
                    rootConcepts.add(concept);
                }
            }
            return rootConcepts;
        }
        return new ArrayList<>();
    }

    public static void setUuidsForObservations(Collection<BahmniObservation> bahmniObservations) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            if (org.apache.commons.lang3.StringUtils.isBlank(bahmniObservation.getUuid())) {
                bahmniObservation.setUuid(UUID.randomUUID().toString());
            }
        }
    }

    public static BahmniObservation getFollowUpDateObservation(Collection<BahmniObservation> bahmniObservations) {
		BahmniObservation observation = null;
        String app = Context.getAdministrationService().getGlobalProperty("appointments.followups");

        for (BahmniObservation bahmniObservation : bahmniObservations) {
            for(BahmniObservation groupMember : bahmniObservation.getGroupMembers()) {
                if(app.contains(groupMember.getConceptUuid())) {
                    if (groupMember.getValue() != null) {
                        observation = groupMember;
                       
                    }
                } else {
                    for (BahmniObservation groupMember2 : groupMember.getGroupMembers()) {

                        if(app.contains(groupMember2.getConceptUuid())) {
                            if (groupMember2.getValue() != null) {
                                observation = groupMember2;

                            }
                        }
                    }
                 }
			}
        }
		return observation;
    }

    public static String getAppointmentService(Collection<BahmniObservation> bahmniObservations) {
        String s = null;
        String appointmentService = null;
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            appointmentService = bahmniObservation.getConceptUuid();
            for (BahmniObservation groupMember : bahmniObservation.getGroupMembers()) {
                    s = groupMember.getConceptUuid();
            }

        }
        return appointmentService;
    }

    public static String getObs(Collection<BahmniObservation> bahmniObservations) {
        String followup = null;
        String app = Context.getAdministrationService().getGlobalProperty("appointments.followups");

        for (BahmniObservation bahmniObservation : bahmniObservations) {
            for (BahmniObservation groupMember : bahmniObservation.getGroupMembers()) {
                    if(app.contains(groupMember.getConceptUuid()))
                        followup = groupMember.getConceptUuid();
                for (BahmniObservation groupMember2 : groupMember.getGroupMembers()) {
                    if(app.contains(groupMember2.getConceptUuid()))
                        followup = groupMember2.getConceptUuid();
              }
            }

        }
        return followup;
    }



    public static BahmniObservation getFollowUpDateObservationRecursive(Collection<BahmniObservation> bahmniObservations, String conceptName){
        for(BahmniObservation bahmniObservation : bahmniObservations) {
            if (bahmniObservation.getConcept().getName().equals(conceptName)) {
                if (bahmniObservation.getValue() != null) {
                    return bahmniObservation;
                }
            } else {
                if (CollectionUtils.isNotEmpty(bahmniObservation.getGroupMembers())) {
                    return getFollowUpDateObservationRecursive(bahmniObservation.getGroupMembers(), conceptName);
                }
            }
       }
        return null;
    }
}
