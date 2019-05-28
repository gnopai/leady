package com.gnopai.leady;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LeadChangeFinderTest {
    private final ZonedDateTime now = ZonedDateTime.now(UTC);

    @Test
    void testNoFieldsChanged() {
        Lead lead = Lead.builder()
                .id("1")
                .email("one@foo.com")
                .firstName("bob")
                .lastName("bobson")
                .address("123 main st")
                .entryDate(now.minusHours(5))
                .build();

        LeadChangeFinder testClass = new LeadChangeFinder();

        LeadChange leadChange = testClass.findLeadChanges(lead, lead);

        LeadChange expectedLeadChange = new LeadChange("1", "one@foo.com", Set.of());
        assertEquals(expectedLeadChange, leadChange);
    }

    @Test
    void testSingleFieldChanged() {
        Lead oldLead = Lead.builder()
                .id("1")
                .email("one@foo.com")
                .firstName("bob")
                .lastName("bobson")
                .address("123 main st")
                .entryDate(now.minusHours(5))
                .build();

        Lead newLead = oldLead.withEmail("new@foo.com");

        LeadChangeFinder testClass = new LeadChangeFinder();

        LeadChange leadChange = testClass.findLeadChanges(oldLead, newLead);

        LeadChange expectedLeadChange = new LeadChange("1", "one@foo.com", Set.of(
                new LeadChange.FieldDiff("email", "one@foo.com", "new@foo.com")
        ));
        assertEquals(expectedLeadChange, leadChange);
    }

    @Test
    void testAllFieldsChanged() {
        Lead oldLead = Lead.builder()
                .id("1")
                .email("one@foo.com")
                .firstName("bob")
                .lastName("bobson")
                .address("123 main st")
                .entryDate(now.minusHours(5))
                .build();

        Lead newLead = Lead.builder()
                .id("2")
                .email("two@foo.com")
                .firstName("michael")
                .lastName("michaelson")
                .address("456 shady st")
                .entryDate(now.minusHours(2))
                .build();

        LeadChangeFinder testClass = new LeadChangeFinder();

        LeadChange leadChange = testClass.findLeadChanges(oldLead, newLead);

        LeadChange expectedLeadChange = new LeadChange("1", "one@foo.com", Set.of(
                new LeadChange.FieldDiff("id", "1", "2"),
                new LeadChange.FieldDiff("email", "one@foo.com", "two@foo.com"),
                new LeadChange.FieldDiff("firstName", "bob", "michael"),
                new LeadChange.FieldDiff("lastName", "bobson", "michaelson"),
                new LeadChange.FieldDiff("address", "123 main st", "456 shady st"),
                new LeadChange.FieldDiff("entryDate", now.minusHours(5), now.minusHours(2))
        ));
        assertEquals(expectedLeadChange, leadChange);
    }

}