package org.openmrs.module.bahmnicore.web.v1_0.mapper;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.mapper.builder.DrugOrderBuilder;
import org.bahmni.module.bahmnicore.mapper.builder.EncounterBuilder;
import org.bahmni.module.bahmnicore.mapper.builder.PersonBuilder;
import org.bahmni.module.bahmnicore.mapper.builder.VisitBuilder;
import org.bahmni.module.bahmnicore.util.CustomDateSerializer;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.FreeTextDosingInstructions;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(LocaleUtility.class)
@RunWith(PowerMockRunner.class)
public class BahmniDrugOrderMapperTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private BahmniProviderMapper providerMapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(LocaleUtility.class);
        when(LocaleUtility.getLocalesInOrder()).thenReturn(new HashSet<Locale>(Arrays.asList(Locale.getDefault())));
        when(providerMapper.map(null)).thenReturn(null);
    }

    @Test
    public void shouldMapToResponseForFreeTextOrderDetails() throws Exception {
        DrugOrderBuilder drugBuilder = new DrugOrderBuilder();
        Date visitDate, dateActivated;
        visitDate = dateActivated = new Date();
        Date dateScheduled = DateUtils.addDays(dateActivated, 2);
        Date expireDate = DateUtils.addDays(dateActivated, 20);


        Person person = new PersonBuilder().withUUID("puuid").build();
        Encounter encounter = new EncounterBuilder().build();
        Visit visit = new VisitBuilder().withPerson(person).withUUID("vuuid").withStartDatetime(visitDate).withEncounter(encounter).build();

        DrugOrder drugOrder1 = drugBuilder.withDrugName("Paracetamol 120mg/5ml 60ml")
                .withDosingType(FreeTextDosingInstructions.class)
                .withDrugForm("Capsule")
                .withScheduledDate(dateScheduled)
                .withDateActivated(dateActivated)
                .withDurationUnits("Week")
                .withDosingInstructions("2.0 Tablet")
                .withVisit(visit)
                .withAutoExpireDate(expireDate).build();

        List<DrugOrder> drugOrderList = new ArrayList<>();
        drugOrderList.add(drugOrder1);

        List<BahmniDrugOrder> mappedDrugOrders = new BahmniDrugOrderMapper(providerMapper).mapToResponse(drugOrderList);
        assertEquals(1, mappedDrugOrders.size());
        BahmniDrugOrder mappedOrder = mappedDrugOrders.get(0);
        EncounterTransaction.DosingInstructions dosingInstructions = mappedOrder.getDosingInstructions();

        assertEquals("Paracetamol 120mg/5ml 60ml", mappedOrder.getDrug().getName());
        assertEquals("Capsule", mappedOrder.getDrug().getForm());
        assertEquals(2.0, dosingInstructions.getDose(), 0);
        assertEquals("Tablet", dosingInstructions.getDoseUnits());
        assertEquals(dateScheduled, mappedOrder.getEffectiveStartDate());
        assertEquals(expireDate, mappedOrder.getEffectiveStopDate());
        assertEquals(18, mappedOrder.getDuration(), 0);
        assertEquals("Week", mappedOrder.getDurationUnits());
        assertEquals("vuuid", mappedOrder.getVisit().getUuid());
        assertEquals(visitDate, mappedOrder.getVisit().getStartDateTime());
        verify(providerMapper);
    }

    @Test
    public void shouldMapToResponseForSimpleOrderDetails() throws Exception {
        DrugOrderBuilder drugBuilder = new DrugOrderBuilder();

        Date dateActivated, visitDate;
        dateActivated = visitDate = new Date();
        Date dateScheduled = DateUtils.addDays(dateActivated, 2);
        Date expireDate = DateUtils.addDays(dateActivated, 20);
        Person person = new PersonBuilder().withUUID("puuid").build();
        Encounter encounter = new EncounterBuilder().build();
        Visit visit = new VisitBuilder().withPerson(person).withUUID("vuuid").withStartDatetime(visitDate).withEncounter(encounter).build();

        int duration = 2;
        String dosingInstructions = "{\"instructions\": \"Before meals\", \"additionalInstructions\": \"Take before waking up\"}";
        DrugOrder drugOrder1 = drugBuilder.withDrugName("Paracetamol 120mg/5ml 60ml")
                .withDosingType(SimpleDosingInstructions.class)
                .withDosingInstructions(dosingInstructions)
                .withDrugForm("Tablet")
                .withDateActivated(dateActivated)
                .withDuration(duration)
                .withDurationUnits("Week")
                .withDose(2.0)
                .withVisit(visit)
                .withFrequency("Once a day")
                .withRoute("Orally")
                .withAutoExpireDate(expireDate)
                .withDoseUnits("Capsule").build();

        List<DrugOrder> drugOrderList = new ArrayList<>();
        drugOrderList.add(drugOrder1);

        List<BahmniDrugOrder> mappedDrugOrders = new BahmniDrugOrderMapper(providerMapper).mapToResponse(drugOrderList);
        assertEquals(1, mappedDrugOrders.size());
        BahmniDrugOrder mappedOrder = mappedDrugOrders.get(0);

        assertEquals("Paracetamol 120mg/5ml 60ml", mappedOrder.getDrug().getName());
        assertEquals("Tablet", mappedOrder.getDrug().getForm());
        assertEquals(2.0, mappedOrder.getDosingInstructions().getDose(), 0);
        assertEquals("Capsule", mappedOrder.getDosingInstructions().getDoseUnits());
        assertEquals(dateActivated, mappedOrder.getEffectiveStartDate());
        assertEquals(expireDate, mappedOrder.getEffectiveStopDate());
        assertEquals(duration, mappedOrder.getDuration(), 0);
        assertEquals("Week", mappedOrder.getDurationUnits());
        assertEquals(dosingInstructions, mappedOrder.getDosingInstructions().getAdministrationInstructions());
        assertEquals("Once a day", mappedOrder.getDosingInstructions().getFrequency());
        assertEquals("Orally", mappedOrder.getDosingInstructions().getRoute());
        assertEquals("vuuid", mappedOrder.getVisit().getUuid());
        assertEquals(visitDate, mappedOrder.getVisit().getStartDateTime());
        verify(providerMapper);
    }
}