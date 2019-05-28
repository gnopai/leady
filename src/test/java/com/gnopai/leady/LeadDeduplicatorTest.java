package com.gnopai.leady;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LeadDeduplicatorTest {
    private final LeadChangeFinder leadChangeFinder = mock(LeadChangeFinder.class);
    private final LeadUpdateReporter leadUpdateReporter = mock(LeadUpdateReporter.class);
    private final ZonedDateTime now = ZonedDateTime.now(UTC);

    @Test
    void testNoDuplicates() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead2B = Lead.builder()
                .id("2")
                .email("B")
                .entryDate(now.minusMinutes(15))
                .build();
        Lead lead3C = Lead.builder()
                .id("3")
                .email("C")
                .entryDate(now.minusMinutes(10))
                .build();
        LeadList leadList = new LeadList(lead1A, lead2B, lead3C);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        assertEquals(leadList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verify(leadUpdateReporter).reportLeadAdded(lead2B);
        verify(leadUpdateReporter).reportLeadAdded(lead3C);
        verifyNoMoreInteractions(leadUpdateReporter);
    }

    @Test
    void testNoDuplicatesAndOutOfOrder() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead2B = Lead.builder()
                .id("2")
                .email("B")
                .entryDate(now.minusMinutes(15))
                .build();
        Lead lead3C = Lead.builder()
                .id("3")
                .email("C")
                .entryDate(now.minusMinutes(10))
                .build();
        LeadList leadList = new LeadList(lead2B, lead3C, lead1A);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        LeadList expectedDedupedList = new LeadList(lead1A, lead2B, lead3C);
        assertEquals(expectedDedupedList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead2B);
        verify(leadUpdateReporter).reportLeadAdded(lead3C);
        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verifyNoMoreInteractions(leadUpdateReporter);
    }
    
    @Test
    void testDuplicateId() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead1B = Lead.builder()
                .id("1")
                .email("B")
                .entryDate(now.minusMinutes(15))
                .build();
        LeadList leadList = new LeadList(lead1A, lead1B);

        LeadChange change1Ato1B = mockLeadChange("1A to 1B", lead1A, lead1B);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        LeadList expectedDedupedList = new LeadList(lead1B);
        assertEquals(expectedDedupedList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verify(leadUpdateReporter).reportLeadChanged(change1Ato1B);
        verifyNoMoreInteractions(leadUpdateReporter);
    }

    @Test
    void testDuplicateEmail() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead2A = Lead.builder()
                .id("2")
                .email("A")
                .entryDate(now.minusMinutes(15))
                .build();
        LeadList leadList = new LeadList(lead1A, lead2A);

        LeadChange change1Ato1B = mockLeadChange("1A to 2A", lead1A, lead2A);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        LeadList expectedDedupedList = new LeadList(lead2A);
        assertEquals(expectedDedupedList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verify(leadUpdateReporter).reportLeadChanged(change1Ato1B);
        verifyNoMoreInteractions(leadUpdateReporter);
    }

    @Test
    void testOlderDuplicateIgnored() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead1B = Lead.builder()
                .id("1")
                .email("B")
                .entryDate(now.minusMinutes(777))
                .build();
        LeadList leadList = new LeadList(lead1A, lead1B);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        LeadList expectedDedupedList = new LeadList(lead1A);
        assertEquals(expectedDedupedList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verify(leadUpdateReporter).reportLeadIgnored(lead1B);
        verifyNoMoreInteractions(leadUpdateReporter);
    }

    @Test
    void testDuplicateWithSameDate() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead2A = Lead.builder()
                .id("2")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        LeadList leadList = new LeadList(lead1A, lead2A);

        LeadChange change1Ato1B = mockLeadChange("1A to 2A", lead1A, lead2A);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        LeadList expectedDedupedList = new LeadList(lead2A);
        assertEquals(expectedDedupedList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verify(leadUpdateReporter).reportLeadChanged(change1Ato1B);
        verifyNoMoreInteractions(leadUpdateReporter);
    }

    @Test
    void testReplacedEmailThatComesBackAroundAgain() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead1B = Lead.builder()
                .id("1")
                .email("B")
                .entryDate(now.minusMinutes(15))
                .build();
        Lead lead2A = Lead.builder()
                .id("2")
                .email("A")
                .entryDate(now.minusMinutes(10))
                .build();
        LeadList leadList = new LeadList(lead1A, lead1B, lead2A);

        LeadChange change1Ato1B = mockLeadChange("1A to 1B", lead1A, lead1B);
        LeadChange change1Bto2A = mockLeadChange("1B to 2A", lead1B, lead2A);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        LeadList expectedDedupedList = new LeadList(lead2A);
        assertEquals(expectedDedupedList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verify(leadUpdateReporter).reportLeadChanged(change1Ato1B);
        verify(leadUpdateReporter).reportLeadChanged(change1Bto2A);
        verifyNoMoreInteractions(leadUpdateReporter);
    }

    @Test
    void testReplacedIdThatComesBackAroundAgain() {
        // given
        Lead lead1A = Lead.builder()
                .id("1")
                .email("A")
                .entryDate(now.minusMinutes(20))
                .build();
        Lead lead2A = Lead.builder()
                .id("2")
                .email("A")
                .entryDate(now.minusMinutes(15))
                .build();
        Lead lead1B = Lead.builder()
                .id("1")
                .email("B")
                .entryDate(now.minusMinutes(10))
                .build();
        LeadList leadList = new LeadList(lead1A, lead2A, lead1B);

        LeadChange change1Ato2A = mockLeadChange("1A to 2A", lead1A, lead2A);
        LeadChange change2Ato1B = mockLeadChange("2A to 1B", lead2A, lead1B);

        LeadDeduplicator testClass = new LeadDeduplicator(leadChangeFinder, leadUpdateReporter);

        // when
        LeadList dedupedLeadList = testClass.deduplicateLeads(leadList);

        // then
        LeadList expectedDedupedList = new LeadList(lead1B);
        assertEquals(expectedDedupedList, dedupedLeadList);

        verify(leadUpdateReporter).reportLeadAdded(lead1A);
        verify(leadUpdateReporter).reportLeadChanged(change1Ato2A);
        verify(leadUpdateReporter).reportLeadChanged(change2Ato1B);
        verifyNoMoreInteractions(leadUpdateReporter);
    }

    private LeadChange mockLeadChange(String identifier, Lead oldLead, Lead newLead) {
        LeadChange leadChange = new LeadChange(identifier, identifier, Set.of());
        when(leadChangeFinder.findLeadChanges(oldLead, newLead)).thenReturn(leadChange);
        return leadChange;
    }
}