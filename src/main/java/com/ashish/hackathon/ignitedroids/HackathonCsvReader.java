package com.ashish.hackathon.ignitedroids;

import com.ashish.hackathon.ignitedroids.model.CLMCsvBean;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HackathonCsvReader {

    private static final Logger logger = LoggerFactory.getLogger(HackathonCsvReader.class);
    private static final Map<String, String> mappings = createMappingsMap();
    public static final Map<String, Map<String, Integer>> releaseToDefectsMap = new ConcurrentHashMap<>();

    private static Map<String, String> createMappingsMap() {
        // Hashmap to map CSV data to
        // Bean attributes.
        Map<String, String> mapping = new HashMap<>();
        mapping.put("Id", "Id");
        mapping.put("Item Notes", "Item_Notes");
        mapping.put("Primary Location", "Primary_Location");
        mapping.put("Summary", "Summary");
        mapping.put("Plan Parent ID", "Plan_Parent_ID");
        mapping.put("Group", "Group");
        mapping.put("Work Item Type", "Work_Item_Type");
        mapping.put("Effective Estimate", "Effective_Estimate");
        mapping.put("Progress:Completed Story Points", "Progress_Completed_Story_Points");
        mapping.put("Progress:Total Story Points", "Progress_Total_Story_Points");
        mapping.put("Progress:Completed Hours", "Progress_Completed_Hours");
        mapping.put("Progress:Total Hours", "Progress_Total_Hours");
        mapping.put("Planned For", "Planned_For");
        mapping.put("Status", "Status");
        mapping.put("Owned By", "Owned_By");
        mapping.put("Filed Against", "Filed_Against");
        mapping.put("Resolves", "Resolves");
        mapping.put("Implements Requirement", "Implements_Requirement");
        mapping.put("Estimate", "Estimate");
        mapping.put("Corrected Estimate", "Corrected_Estimate");

        return mapping;
    }

    public static void readDump(String fileName) {
        logger.info("Reading CLM dump");
        // HeaderColumnNameTranslateMappingStrategy
        // for Student class
        HeaderColumnNameTranslateMappingStrategy<CLMCsvBean> strategy =
                new HeaderColumnNameTranslateMappingStrategy<>();
        strategy.setType(CLMCsvBean.class);
        strategy.setColumnMapping(mappings);

        try {
            ClassLoader classLoader = Main.class.getClassLoader();
            Reader reader = new BufferedReader(new FileReader(classLoader.getResource(fileName).getFile()));
            CsvToBean<CLMCsvBean> csvReader = new CsvToBeanBuilder(reader)
                    .withType(CLMCsvBean.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .withMappingStrategy(strategy)
                    .build();

            List<CLMCsvBean> listOfBeans = csvReader.parse();

            List<CLMCsvBean> defectBeans = listOfBeans.
                    stream().
                    filter(bean -> bean.getWork_Item_Type().equals("Defect")).toList();

            calculateBugsFor(defectBeans);
            logger.info("CLM Dump successfully read and configured");
            logger.info(releaseToDefectsMap.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void calculateBugsFor(List<CLMCsvBean> defectBeans) {
        for(CLMCsvBean bean: defectBeans) {
            String release = getPlannedFor(bean.getPlanned_For());
            String stage = getStage(bean.getPlanned_For());

            if(releaseToDefectsMap.containsKey(release)) {
                Map<String, Integer> currMap = releaseToDefectsMap.get(release);
                Integer bugs = currMap.get(stage);
                if(bugs == null)
                    bugs = 0;
                currMap.put(stage, bugs + 1);
            }
            else {
                Map<String, Integer> currMap = new HashMap<>();
                currMap.put(stage, 1);
                releaseToDefectsMap.put(release, currMap);
            }
        }
    }

    private static String getStage(String planned_for) {
        if(planned_for.indexOf('[') == -1) {
            return planned_for;
        }

        return planned_for.substring(planned_for.indexOf('[')+1, planned_for.indexOf(']'));
    }

    private static String getPlannedFor(String planned_for) {
        String projectPrefix = ConfigReader.getProjectConfig().getProject().getPrefix();
        int last;
        if(planned_for.indexOf('-') == -1) {
            last = planned_for.length();
        }
        else {
            last = planned_for.indexOf('-');
        }
        planned_for = planned_for.substring(projectPrefix.length() + 1, last);
        return planned_for.trim();
    }
}
