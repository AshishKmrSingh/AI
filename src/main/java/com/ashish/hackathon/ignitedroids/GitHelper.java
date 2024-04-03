package com.ashish.hackathon.ignitedroids;

import com.ashish.hackathon.ignitedroids.jdbc.TableOperations;
import com.ashish.hackathon.ignitedroids.model.CsvRow;
import com.ashish.hackathon.ignitedroids.model.DataTable;
import com.ashish.hackathon.ignitedroids.model.Project;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GitHelper {
    private static final Logger logger = LoggerFactory.getLogger(GitHelper.class);
    private static final Map<Ref, List<String>> mapOfTagAndCommitsInEach = new ConcurrentHashMap<>();
    private static final List<Ref> refReleaseTagsInRepo = new ArrayList<>();
    private static final Map<String, CsvRow> releaseDataMap = new ConcurrentHashMap<>();
    private static final String tagPrefix = ConfigReader.getProjectConfig().getProject().getTag_prefix();
    private static Git jGit;

    public static void setJGit(String workDir) throws IOException {
        jGit = Git.open(new File(workDir));
    }

    public static void cloneRepository(String gitLink, String workDir) throws GitAPIException, IOException {
        boolean isRepoWorkDir = isRepo(workDir);
        if (!isRepoWorkDir) {
            logger.info("Cloning remote repository: {}", gitLink);
            Git.cloneRepository()
                    .setURI(gitLink)
                    .setDirectory(new File(workDir))
                    .call();
        } else {
            logger.info("The directory: {} is already a git repo. Would not clone", workDir);
        }
    }

    public static void getReleaseTags() throws GitAPIException {
        List<Ref> tagList = jGit.tagList().call();

        if (tagList.isEmpty()) {
            logger.error("Cannot create training data set. There are no releases for this repository");
        }
        refReleaseTagsInRepo.addAll(tagList);
        refReleaseTagsInRepo.sort(new RefComparator());
    }

    private static boolean isRepo(String workDir) {
       logger.info("Checking directory: {} to be a git repository", workDir);

        try {
            Repository repo = jGit.getRepository();
            for (Ref ref : repo.getAllRefs().values()) {
                if (ref.getObjectId() == null)
                    continue;
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void calculateMetrics() throws IOException {
        Set<Ref> persistedRefs = checkAndGetPersistentReleases();

        Ref firstRef;
        int i;
        if(!refReleaseTagsInRepo.isEmpty()) {
            if(refReleaseTagsInRepo.size() >= 2 ) {
                firstRef = refReleaseTagsInRepo.get(0);
                i = 1;
            }
            else {
                firstRef = persistedRefs.stream().reduce((prev, next) -> next).orElse(null);
                i = 0;
            }
            for (; i < refReleaseTagsInRepo.size() && firstRef != null; i++) {
                findCommitsAndCalculateMetrics(firstRef, refReleaseTagsInRepo.get(i));
                firstRef = refReleaseTagsInRepo.get(i);
            }

            writeFromFinalDataToCSV();
        }
        else {
            logger.info("No more releases found for finding history data");
        }
    }

    private static void writeFromFinalDataToCSV() throws IOException {

        if(!mapOfTagAndCommitsInEach.isEmpty()) {
            mapOfTagAndCommitsInEach.keySet().forEach(ref -> calculateMetricsForCommits(mapOfTagAndCommitsInEach.get(ref), ref.getName()));

            logger.info("Final data to write into CSV and database: {}", releaseDataMap);
            for(String key: releaseDataMap.keySet()) {
                CsvRow row = releaseDataMap.get(key);
                String filesChanged = String.valueOf(row.getFilesChanged());
                String linesAdded = String.valueOf(row.getAddedLines());
                String linesDeleted = String.valueOf(row.getDeletedLines());
                long designBugs = row.getDesignBugs();
                long devBugs = row.getDevBugs();
                long testBugs = row.getTestBugs();
                long qualBugs = row.getQualityBugs();
                long prodBugs = row.getProdBugs();
                String release = row.getRelease();

                StringJoiner rowForDesign = new StringJoiner(",").add(release).add("0").add("0").add("0").add("Design").add(String.valueOf(designBugs));
                StringJoiner rowForDev = new StringJoiner(",").add(release).add(filesChanged).add(linesAdded).add(linesDeleted).add("Development").add(String.valueOf(devBugs));
                StringJoiner rowForTest = new StringJoiner(",").add(release).add("0").add("0").add("0").add("Testing").add(String.valueOf(testBugs));
                StringJoiner rowForQuality = new StringJoiner(",").add(release).add("0").add("0").add("0").add("Audit").add(String.valueOf(qualBugs));
                StringJoiner rowForProduction = new StringJoiner(",").add(release).add(filesChanged).add(linesAdded).add(linesDeleted).add("Production").add(String.valueOf(prodBugs));
                HackathonCsvWriter.writeRow(rowForDesign.toString());
                HackathonCsvWriter.writeRow(rowForDev.toString());
                HackathonCsvWriter.writeRow(rowForTest.toString());
                HackathonCsvWriter.writeRow(rowForQuality.toString());
                HackathonCsvWriter.writeRow(rowForProduction.toString());

                TableOperations.writeIntoDB(rowForDesign.toString());
                TableOperations.writeIntoDB(rowForDev.toString());
                TableOperations.writeIntoDB(rowForTest.toString());
                TableOperations.writeIntoDB(rowForQuality.toString());
                TableOperations.writeIntoDB(rowForProduction.toString());
            }
        }
    }

    private static Set<Ref> checkAndGetPersistentReleases() throws IOException {

        List<DataTable> persistedReleases = TableOperations.getPersistedReleases();
        Set<Ref> persistedRefs = new LinkedHashSet<>();
        if(!persistedReleases.isEmpty()) {
            logger.info("{} available persistent rows found", persistedReleases.size());
            for (Ref ref : refReleaseTagsInRepo) {
                String refName = ref.getName();
                for (DataTable release : persistedReleases) {
                    if (refName.contains(release.getPast_release())) {
                        persistedRefs.add(ref);
                        String releaseName = release.getPast_release();
                        String filesCh = String.valueOf(release.getFilesChanged());
                        String linesAdd = String.valueOf(release.getLinesAdded());
                        String linesDeleted = String.valueOf(release.getLinesDeleted());
                        String stage = release.getStage();
                        String bugs = String.valueOf(release.getBugs());

                        StringJoiner row = new StringJoiner(",").add(releaseName).add(filesCh).add(linesAdd).add(linesDeleted).add(stage).add(bugs);

                        HackathonCsvWriter.writeRow(row.toString());
                    }
                }
            }
        }
        else {
            logger.info("No persistent releases found in the database");
        }
        logger.info("Original release tags: {}", refReleaseTagsInRepo);
        refReleaseTagsInRepo.removeAll(persistedRefs);
        logger.info("After deleting the persisted tags: {}", refReleaseTagsInRepo);

        return persistedRefs;
    }

    private static void calculateMetricsForCommits(List<String> commits, String name) {
        long linesAdded = 0;
        long linesDeleted = 0;
        long filesChanged = 0;

        logger.info("Calculating metrics for release tag: " + name);
        for (String commit : commits) {
            try {
                String line = getShortStat(commit);

                if (line.contains("files changed")) {
                    String changed = line.substring(line.indexOf("files changed") - 2, line.indexOf("files changed") - 1).trim();
                    filesChanged = filesChanged + Integer.parseInt(changed);
                }
                if (line.contains("insertions(+)")) {
                    String insertions = line.substring(line.indexOf("insertions(+)") - 2, line.indexOf("insertions(+)") - 1).trim();
                    linesAdded = linesAdded + Integer.parseInt(insertions);
                }
                if (line.contains("deletions(-)")) {
                    String deletions = line.substring(line.indexOf("deletions(-)") - 2, line.indexOf("deletions(-)") - 1).trim();
                    linesDeleted = linesDeleted + Integer.parseInt(deletions);
                }
            } catch (Exception e) {
                logger.error("Error getting shortstat for commitID: {} in tag: {}", commit, name);
            }
        }

        logger.info("There are {} files changed, {} lines added and {} lines deleted in all commits released in release: {}", filesChanged, linesAdded, linesDeleted, name);

        createReleaseDataMap(filesChanged, linesAdded, linesDeleted, name);
    }

    private static void createReleaseDataMap(long filesChanged, long linesAdded, long linesDeleted, String name) {
        Project.Releases releases = ConfigReader.getProjectConfig().getProject().getReleases();
        List<String> released = releases.getReleased();
        boolean bugsAlreadyAssociated = false;

        for (String release : released) {
            long devBugs;
            long testingBugs;
            long prodBugs;
            long qualityBugs;
            long designBugs;
            CsvRow savedData = releaseDataMap.get(release);
            if (savedData == null) {
                savedData = new CsvRow();
            }
            else {
                bugsAlreadyAssociated = true;
            }

            if (name.contains(release)) {
                if(!bugsAlreadyAssociated) {
                    Map<String, Integer> map = HackathonCsvReader.releaseToDefectsMap.get(release);
                    if (map != null && !map.isEmpty()) {
                        for (String key : map.keySet()) {
                            devBugs = savedData.getDevBugs();
                            testingBugs = savedData.getTestBugs();
                            prodBugs = savedData.getProdBugs();
                            designBugs = savedData.getDesignBugs();
                            qualityBugs = savedData.getQualityBugs();

                            if (key.contains("Dev")) {
                                devBugs = devBugs + map.get(key);
                                savedData.setDevBugs(devBugs);
                            }
                            if (key.contains("QA") || key.contains("Test") || key.contains("test")) {
                                testingBugs = testingBugs + map.get(key);
                                savedData.setTestBugs(testingBugs);
                            }
                            if (key.contains("Quality") || key.contains("ASOC") || key.contains("Blackduck")) {
                                qualityBugs = qualityBugs + map.get(key);
                                savedData.setQualityBugs(qualityBugs);
                            }
                            if (key.contains("Scope") || key.contains("Groom")) {
                                designBugs = designBugs + map.get(key);
                                savedData.setDesignBugs(designBugs);
                            } else {
                                prodBugs = prodBugs + map.get(key);
                                savedData.setProdBugs(prodBugs);
                            }
                        }
                    }
                }

                filesChanged = filesChanged + savedData.getFilesChanged();
                linesAdded = linesAdded + savedData.getAddedLines();
                linesDeleted = linesDeleted + savedData.getDeletedLines();
                savedData.setFilesChanged(filesChanged);
                savedData.setAddedLines(linesAdded);
                savedData.setDeletedLines(linesDeleted);
                savedData.setRelease(release);
                bugsAlreadyAssociated = true;

                releaseDataMap.put(release, savedData);
            }
        }
    }

    private static String getShortStat(String commitId) {
        String lineread = "";
        boolean status = false;
        String[] commandLine = new String[]{
                "C:\\Program Files\\Git\\bin\\sh.exe",
                "--login",
                "-i",
                "-c",
                "git show --shortstat " + commitId
        };
        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.directory(new File(ConfigReader.getProjectConfig().getProject().getWorkDir()));
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            status = p.waitFor() == 0;
            try (BufferedReader reader = p.inputReader()) {
                while (reader.ready()) {
                    lineread = reader.readLine();
                    if (lineread.contains("files changed") ||
                            lineread.contains("insertions(+)") ||
                            lineread.contains("deletions(-)")) {
                        break;
                    }
                }
            }
            p.destroy();
            return lineread;
        } catch (IOException e) {
            logger.error("Git show process cannot successfully start. ", e);
        } catch (InterruptedException e) {
            logger.error("Something went wrong waiting for Git show to complete", e);
        }
        return lineread;
    }

    private static void findCommitsAndCalculateMetrics(Ref firstRef, Ref secondRef) {
        List<String> commits = new ArrayList<>();
        String[] commandLine = new String[]{
                "C:\\Program Files\\Git\\bin\\sh.exe",
                "--login",
                "-i",
                "-c",
                "git log --pretty=format:\"%h\" " + firstRef.getName() + "..." + secondRef.getName()
        };
        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.directory(new File(ConfigReader.getProjectConfig().getProject().getWorkDir()));
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            boolean b = p.waitFor() == 0;
            try (BufferedReader reader = p.inputReader()) {
                while (reader.ready()) {
                    commits.add(reader.readLine());
                }
            }
            logger.info("Found {} commits released in release: {} on top of release: {}", commits.size(), secondRef.getName(), firstRef.getName());

            mapOfTagAndCommitsInEach.putIfAbsent(secondRef, commits);
            p.destroy();
        } catch (IOException e) {
            logger.error("Git show process cannot successfully start. ", e);
        } catch (InterruptedException e) {
            logger.error("Something went wrong waiting for Git show to complete", e);
        }
    }

    private static class RefComparator implements Comparator<Ref> {

        @Override
        public int compare(Ref thisRef, Ref thatRef) {
            String first = thisRef.getName().replaceAll("refs/tags/"+tagPrefix, "");
            String second = thatRef.getName().replaceAll("refs/tags/"+tagPrefix, "");

            //return extractInt(thisRef.getName()) - extractInt(thatRef.getName());
            int res = extractInt(first) - extractInt(second);

            if(res > 0) {
                String firstLeft = null;
                String secondLeft = null;
                if(first.contains("-")) {
                    firstLeft = first.substring(0, first.indexOf("-"));
                }
                if(second.contains("-")) {
                    secondLeft = second.substring(0, second.indexOf("-"));
                }

                res = extractInt(firstLeft) - extractInt(secondLeft);

                if(res == 0) {
                    String firstRight = first.substring(first.indexOf("-")+1);
                    String secondRight = second.substring(second.indexOf("-")+1);

                    res = extractInt(firstRight) - extractInt(secondRight);
                }
            }

            return res;
        }

        int extractInt(String s) {
            if(s != null) {
                String num = s.replaceAll("\\D", "");
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
            return 0;
        }
    }
}
