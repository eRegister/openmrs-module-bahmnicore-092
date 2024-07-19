package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.ObsFormToServiceTypeMappingDao;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ObsFormToServiceTypeMappingDaoImpl implements ObsFormToServiceTypeMappingDao {

    @Autowired
    private DbSessionFactory sessionFactory;

    public DbSession getSession(){
        return sessionFactory.getCurrentSession();
    }

    @Override
    public String getAppointmentServiceTypeFromObsFormUuid(String obsFormConceptUuid) {
//        Transaction transaction = getSession().beginTransaction();
        SQLQuery sqlQuery = getSession().createSQLQuery("SELECT appointment_service_type_uuid "
                + " FROM obs_form_service_type_mapping where obs_form_concept_uuid=:obsFormUuid");
        sqlQuery.setString("obsFormUuid",obsFormConceptUuid);
        String serviceTypeUuid = (String) sqlQuery.uniqueResult();
//        if (!transaction.wasCommitted())
//            transaction.commit();

        return serviceTypeUuid;
    }
}
