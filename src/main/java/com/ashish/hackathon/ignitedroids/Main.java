package com.ashish.hackathon.ignitedroids;

import com.ashish.hackathon.ignitedroids.jdbc.HackathonJDBCConnection;
import com.ashish.hackathon.ignitedroids.jdbc.TableCreator;
import com.ashish.hackathon.ignitedroids.model.Project;
import com.ashish.hackathon.ignitedroids.model.ProjectConfig;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, GitAPIException, URISyntaxException {
        ClassLoader classLoader = Main.class.getClassLoader();
        File file = new File(classLoader.getResource("config.json").getFile());
        ProjectConfig projectConfig = ConfigReader.readConfigFile(file);
        Project project = projectConfig.getProject();
        GitHelper.setJGit(project.getWorkDir());
        HackathonCsvWriter.writeHeader();
        GitHelper.cloneRepository(project.getBitBucketLink(), project.getWorkDir());
        HackathonCsvReader.readDump("dump.csv");
        HackathonJDBCConnection.createConnection();
        TableCreator.createTable();
        GitHelper.getReleaseTags();
        GitHelper.calculateMetrics();
        HackathonCsvWriter.writer().close();
        HackathonJDBCConnection.closeConnection();
    }
}