package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsFormToServiceTypeMappingDao;
import org.bahmni.module.bahmnicore.service.ObsFormToServiceTypeMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObsFormToServiceTypeMappingServiceImpl implements ObsFormToServiceTypeMappingService {

    @Autowired
    ObsFormToServiceTypeMappingDao dao;

    @Override
    public String getServiceTypeUuid(String obsFormUuid) {
        return dao.getAppointmentServiceTypeFromObsFormUuid(obsFormUuid);
    }
}
