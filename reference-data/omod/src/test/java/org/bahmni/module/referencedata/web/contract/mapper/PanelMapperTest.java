package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.contract.Department;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.bahmni.module.referencedata.labconcepts.contract.Sample;
import org.bahmni.module.referencedata.labconcepts.mapper.PanelMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.advice.ConceptOperationEventInterceptorTest.getConceptSet;
import static org.bahmni.module.referencedata.labconcepts.advice.ConceptOperationEventInterceptorTest.getConceptSets;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

//TODO: Mihir : write a test for empty tests list
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class PanelMapperTest {
    private PanelMapper panelMapper;
    private Concept sampleConcept;
    private Date dateCreated;
    private Date dateChanged;
    private Concept laboratoryConcept;
    @Mock
    private ConceptService conceptService;
    private Concept departmentConcept;
    private Concept labDepartmentConcept;
    private Concept panelConcept;
    private Concept testAndPanelsConcept;
    private List<ConceptSet> sampleConceptSets;
    private List<ConceptSet> departmentConceptSets;
    private List<ConceptSet> testConceptSets;
    private ConceptSet testDepartmentConceptSet;
    private ConceptSet panelSampleConceptSet;
    private Concept testConcept;
    private ConceptSet testPanelConceptSet;
    private ConceptSet testSampleConceptSet;
    private List<ConceptSet> panelConceptSets;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        panelMapper = new PanelMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        testConcept = new ConceptBuilder().withUUID("Test UUID").withDateCreated(dateCreated).withClass(LabTest.LAB_TEST_CONCEPT_CLASS).withDescription("SomeDescription")
                .withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name Here").withDataType(ConceptDatatype.NUMERIC).build();
        panelConcept = new ConceptBuilder().withUUID("Panel UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.LABSET_UUID).withDescription("SomeDescription")
                .withSetMember(testConcept).withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name Here").withDataType(ConceptDatatype.NUMERIC).build();
        testAndPanelsConcept = new ConceptBuilder().withUUID("Test and Panels UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.CONVSET_UUID)
                .withDateChanged(dateChanged).withShortName("ShortName").withName(AllTestsAndPanels.ALL_TESTS_AND_PANELS).withSetMember(panelConcept).build();
        sampleConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).withClass(Sample.SAMPLE_CONCEPT_CLASS).
                withDateChanged(dateChanged).withSetMember(panelConcept).withShortName("ShortName").withName("SampleName").build();
        laboratoryConcept = new ConceptBuilder().withUUID("Laboratory UUID")
                .withName(AllSamples.ALL_SAMPLES).withClassUUID(ConceptClass.LABSET_UUID)
                .withSetMember(sampleConcept).build();
        departmentConcept = new ConceptBuilder().withUUID("Department UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withClass("Department").withClassUUID(ConceptClass.CONVSET_UUID).withSetMember(panelConcept).withDescription("Some Description").withName("Department Name").build();
        labDepartmentConcept = new ConceptBuilder().withUUID("Laboratory Department UUID")
                .withName(Department.DEPARTMENT_PARENT_CONCEPT_NAME).withClass(Department.DEPARTMENT_CONCEPT_CLASS)
                .withSetMember(departmentConcept).build();
        ConceptSet panelConceptSet = getConceptSet(testAndPanelsConcept, panelConcept);
        ConceptSet testConceptSet = getConceptSet(testAndPanelsConcept, testConcept);
        testPanelConceptSet = getConceptSet(testConcept, panelConcept);
        panelSampleConceptSet = getConceptSet(sampleConcept, panelConcept);
        testSampleConceptSet = getConceptSet(sampleConcept, testConcept);
        testDepartmentConceptSet = getConceptSet(departmentConcept, testConcept);

        testConceptSets = getConceptSets(testConceptSet);
        testConceptSets.add(testSampleConceptSet);
        testConceptSets.add(testDepartmentConceptSet);

        panelConceptSets = getConceptSets(panelConceptSet);
        panelConceptSets.add(panelSampleConceptSet);
        panelConceptSets.add(testPanelConceptSet);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenAnswer(new Answer<List<ConceptSet>>() {
            @Override
            public List<ConceptSet> answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                Concept concept = (Concept) arguments[0];
                if (concept.getUuid().equals("Test UUID"))
                    return testConceptSets;
                else if (concept.getUuid().equals("Panel UUID"))
                    return panelConceptSets;
                return null;
            }
        });
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void map_all_panel_fields_from_concept() throws Exception {
        Panel panelData = panelMapper.map(panelConcept);
        assertEquals("Panel UUID", panelData.getId());
        assertEquals("Panel Name Here", panelData.getName());
        assertEquals(dateCreated, panelData.getDateCreated());
        assertEquals(dateChanged, panelData.getLastUpdated());
        assertEquals("Sample UUID", panelData.getSampleUuid());
        assertEquals(1, panelData.getTests().size());
        assertEquals("Sample UUID", panelData.getTests().get(0).getSampleUuid());
        assertEquals("Department UUID", panelData.getTests().get(0).getDepartment().getId());
        assertEquals("Test UUID", panelData.getTests().get(0).getId());
        assertTrue(panelData.getSortOrder().equals(999.0));
    }

    @Test
    public void is_active_true_by_default() throws Exception {
        Panel panelData = panelMapper.map(panelConcept);
        assertTrue(panelData.getIsActive());
    }

    @Test
    public void null_if_sample_not_specified() throws Exception {
        panelConceptSets.remove(panelSampleConceptSet);
        Panel panelData = panelMapper.map(panelConcept);
        assertNull(panelData.getSampleUuid());
    }

    @Test
    public void should_set_name_if_description_is_null() throws Exception {
        Concept panelConceptWitoutDescription = new ConceptBuilder().withDateCreated(dateCreated).withClassUUID(ConceptClass.LABSET_UUID)
                .withSetMember(testConcept).withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name Here").withDataType(ConceptDatatype.NUMERIC).build();

        Panel panelData = panelMapper.map(panelConceptWitoutDescription);
        assertEquals("Panel Name Here", panelData.getDescription());
    }
}