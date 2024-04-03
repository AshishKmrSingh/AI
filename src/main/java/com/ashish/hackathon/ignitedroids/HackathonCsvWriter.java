package com.ashish.hackathon.ignitedroids;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

public class HackathonCsvWriter {

    private static final Logger logger = LoggerFactory.getLogger(HackathonCsvWriter.class);
    private static final FileWriter csvWriter = setCsvWriter();

    public static FileWriter writer() {
        return csvWriter;
    }

    private static FileWriter setCsvWriter() {
        try {
            return new FileWriter(ConfigReader.getProjectConfig().getProject().getResultFilePath());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeHeader() throws IOException {
        String header = "RELEASE,NFILES,ALOC,DLOC,STAGE,BUGS";
        if(csvWriter != null) {
            logger.info("Writing header in the csv file");
            csvWriter.write(header);
            csvWriter.write(String.format("%n"));
            csvWriter.write(header);
            csvWriter.flush();
        }
    }

    public static void writeRow(String row) throws IOException {
        if(csvWriter != null) {
            csvWriter.write(row);
            csvWriter.write(String.format("%n"));
            csvWriter.flush();
        }
    }
}
