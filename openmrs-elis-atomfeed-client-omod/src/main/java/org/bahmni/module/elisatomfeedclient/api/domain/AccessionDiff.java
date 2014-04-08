package org.bahmni.module.elisatomfeedclient.api.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccessionDiff {
    private Set<OpenElisTestDetail> addedTestDetails = new HashSet<>();
    private Set<OpenElisTestDetail> removedTestDetails = new HashSet<>();

    public List<String> getAccessionNotesToBeAdded() {
        return accessionNotesToBeAdded;
    }

    public void setAccessionNotesToBeAdded(List<String> accessionNotesToBeAdded) {
        this.accessionNotesToBeAdded = accessionNotesToBeAdded;
    }

    private List<String> accessionNotesToBeAdded = new ArrayList<>();

    public Set<OpenElisTestDetail> getAddedTestDetails() {
        return addedTestDetails;
    }

    public void addAddedTestDetail(OpenElisTestDetail testDetail) {
        addedTestDetails.add(testDetail);
    }

    public Set<OpenElisTestDetail> getRemovedTestDetails() {
        return removedTestDetails;
    }

    public void addRemovedTestDetails(OpenElisTestDetail testDetail) {
        removedTestDetails.add(testDetail);
    }

    public boolean hasDifference() {
        return (getRemovedTestDetails().size() > 0 || getAddedTestDetails().size() > 0);
    }
}
