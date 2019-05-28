package com.gnopai.leady;

import com.fatboyindustrial.gsonjavatime.ZonedDateTimeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fatboyindustrial.gsonjavatime.Converters.ZONED_DATE_TIME_TYPE;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class LeadyMain {
    private final LeadDeduplicator leadDeduplicator;
    private final Gson gson;

    public LeadyMain(LeadDeduplicator leadDeduplicator, Gson gson) {
        this.leadDeduplicator = leadDeduplicator;
        this.gson = gson;
    }

    public void processLeads(String inputFile, String outputFile) {
        LeadList leadList = readLeadsFromFile(inputFile);
        LeadList dedupedLeadList = leadDeduplicator.deduplicateLeads(leadList);
        writeLeadsToFile(dedupedLeadList, outputFile);
    }

    private LeadList readLeadsFromFile(String fileName) {
        try {
            String json = String.join(" ", Files.readAllLines(Paths.get(fileName)));
            return gson.fromJson(json, LeadList.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read leads from input file " + fileName, e);
        }
    }

    private void writeLeadsToFile(LeadList leads, String fileName) {
        try {
            String json = gson.toJson(leads);
            Path filePath = Paths.get(fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, json.getBytes(defaultCharset()), CREATE, TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write deduplicated leads to output file " + fileName, e);
        }
    }

    public static void main(String[] args) {
        LeadyMain leadyMain = new LeadyMain(
                new LeadDeduplicator(new LeadChangeFinder(), new StandardOutLeadUpdateReporter()),
                new GsonBuilder()
                        .registerTypeAdapter(ZONED_DATE_TIME_TYPE, new ZonedDateTimeConverter())
                        .create()
        );

        String inputFileName = args.length >= 1 ? args[0] : "src/main/resources/sample_leads.json";
        String outputFileName = args.length >= 2 ? args[1] : "out/deduped_leads.json";
        leadyMain.processLeads(inputFileName, outputFileName);
        System.out.println("Results written to " + outputFileName);
    }
}
