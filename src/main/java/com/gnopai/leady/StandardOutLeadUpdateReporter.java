package com.gnopai.leady;

public class StandardOutLeadUpdateReporter implements LeadUpdateReporter {

    @Override
    public void reportLeadAdded(Lead lead) {
        System.out.printf("Lead %s/%s was added\n", lead.getId(), lead.getEmail());
    }

    @Override
    public void reportLeadChanged(LeadChange leadChange) {
        System.out.printf("Lead %s/%s was updated\n", leadChange.getOriginalId(), leadChange.getOriginalEmail());
        leadChange.getFieldDiffs().forEach(fieldChange ->
                System.out.printf("    field '%s' changed from '%s' to '%s'\n",
                        fieldChange.getFieldName(),
                        fieldChange.getOldValue(),
                        fieldChange.getNewValue()
                )
        );
    }

    @Override
    public void reportLeadIgnored(Lead lead) {
        System.out.printf("Lead update for %s/%s was ignored due to a newer entry\n", lead.getId(), lead.getEmail());
    }
}
