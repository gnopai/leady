package com.gnopai.leady;

public interface LeadUpdateReporter {

    void reportLeadAdded(Lead lead);

    void reportLeadChanged(LeadChange leadChange);

    void reportLeadIgnored(Lead lead);
}
