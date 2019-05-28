package com.gnopai.leady;

import lombok.Value;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class LeadDeduplicator {
    private final LeadChangeFinder leadChangeFinder;
    private final LeadUpdateReporter leadUpdateReporter;

    public LeadDeduplicator(LeadChangeFinder leadChangeFinder, LeadUpdateReporter leadUpdateReporter) {
        this.leadChangeFinder = leadChangeFinder;
        this.leadUpdateReporter = leadUpdateReporter;
    }

    public LeadList deduplicateLeads(LeadList leadList) {
        LeadStore leadStore = new LeadStore();
        leadList.getLeads().forEach(lead -> processLead(leadStore, lead));
        return new LeadList(sortLeads(leadStore.getLeadRecords()));
    }

    private void processLead(LeadStore leadStore, Lead lead) {
        leadStore.getExistingLeadRecord(lead).ifPresentOrElse(
                existingRecord -> processExistingLead(leadStore, lead, existingRecord),
                () -> processNewLead(leadStore, lead)
        );
    }

    private void processNewLead(LeadStore leadStore, Lead lead) {
        leadUpdateReporter.reportLeadAdded(lead);
        leadStore.addLead(lead);
    }

    private void processExistingLead(LeadStore leadStore, Lead lead, LeadRecord existingRecord) {
        if (!shouldReplaceLead(existingRecord.getLead(), lead)) {
            leadUpdateReporter.reportLeadIgnored(lead);
            return;
        }

        LeadChange leadChange = leadChangeFinder.findLeadChanges(existingRecord.getLead(), lead);
        leadUpdateReporter.reportLeadChanged(leadChange);
        leadStore.updateLead(existingRecord, lead);
    }

    private boolean shouldReplaceLead(Lead existingLead, Lead newLead) {
        boolean newLeadIsOlder = newLead.getEntryDate().isBefore(existingLead.getEntryDate());
        return !newLeadIsOlder; // new lead is equal-to-or-newer-than old lead
    }

    private List<Lead> sortLeads(Collection<LeadRecord> leads) {
        return leads.stream()
                .map(LeadRecord::getLead)
                .sorted(comparing(Lead::getEntryDate).thenComparing(Lead::getId))
                .collect(toList());
    }

    private static class LeadStore {
        private final Map<String, String> leadKeysById = new HashMap<>();
        private final Map<String, String> leadKeysByEmail = new HashMap<>();
        private final Map<String, LeadRecord> leadRecords = new HashMap<>();

        public Optional<LeadRecord> getExistingLeadRecord(Lead lead) {
            return getLeadRecordById(lead.getId())
                    .or(() -> getLeadRecordByEmail(lead.getEmail()));
        }

        private Optional<LeadRecord> getLeadRecordById(String id) {
            return Optional.ofNullable(leadKeysById.get(id))
                    .map(leadRecords::get);
        }

        private Optional<LeadRecord> getLeadRecordByEmail(String email) {
            return Optional.ofNullable(leadKeysByEmail.get(email))
                    .map(leadRecords::get);
        }

        public void addLead(Lead lead) {
            String key = lead.getId(); // we'll just use the original lead's ID as the ongoing primary key
            saveRecord(key, lead);
        }

        public void updateLead(LeadRecord existingRecord, Lead updatedLead) {
            saveRecord(existingRecord.getKey(), updatedLead);
        }

        private void saveRecord(String key, Lead lead) {
            LeadRecord leadRecord = new LeadRecord(key, lead);
            leadRecords.put(key, leadRecord);
            leadKeysById.put(lead.getId(), key);
            leadKeysByEmail.put(lead.getEmail(), key);
        }

        public Collection<LeadRecord> getLeadRecords() {
            return leadRecords.values();
        }
    }

    @Value
    private static class LeadRecord {
        String key;
        Lead lead;
    }
}
