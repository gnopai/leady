package com.gnopai.leady;

import lombok.Value;

import java.util.List;

@Value
public class LeadList {
    List<Lead> leads;

    public LeadList(List<Lead> leads) {
        this.leads = leads;
    }

    public LeadList(Lead... leads) {
        this.leads = List.of(leads);
    }
}
