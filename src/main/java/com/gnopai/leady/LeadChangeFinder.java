package com.gnopai.leady;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Set;

import static java.beans.Introspector.getBeanInfo;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class LeadChangeFinder {

    public LeadChange findLeadChanges(Lead oldLead, Lead newLead) {
        return new LeadChange(oldLead.getId(), oldLead.getEmail(), findFieldChanges(oldLead, newLead));
    }

    private Set<LeadChange.FieldDiff> findFieldChanges(Lead oldLead, Lead newLead) {
        return stream(getLeadProperties())
                .map(propertyDescriptor -> createFieldDiff(propertyDescriptor, oldLead, newLead))
                .filter(LeadChange.FieldDiff::hasChanged)
                .collect(toSet());
    }

    private PropertyDescriptor[] getLeadProperties() {
        try {
            return getBeanInfo(Lead.class).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private LeadChange.FieldDiff createFieldDiff(PropertyDescriptor propertyDescriptor, Lead oldObject, Lead newObject) {
        try {
            Method getter = propertyDescriptor.getReadMethod();
            return new LeadChange.FieldDiff(propertyDescriptor.getName(), getter.invoke(oldObject), getter.invoke(newObject));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
