package com.oskarro.batcher.controller;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api")
public class BatchController {

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    JobLauncher jobLauncher;

    JobExplorer jobExplorer;

    @Autowired
    ApplicationContext context;

    @Qualifier("csvToDatabaseJob")
    Job csvToDatabaseJob;

    @Qualifier("requestToDatabaseJob")
    Job requestToDatabaseJob;

    @Qualifier("synchronizeDatabaseJob")
    Job synchronizeDatabaseJob;

    @Qualifier("computerUpdateJob")
    Job computerUpdateJob;

    public BatchController(JobLauncher jobLauncher,
                           JobExplorer jobExplorer,
                           Job csvToDatabaseJob,
                           Job requestToDatabaseJob,
                           Job synchronizeDatabaseJob,
                           Job computerUpdateJob) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.csvToDatabaseJob = csvToDatabaseJob;
        this.requestToDatabaseJob = requestToDatabaseJob;
        this.synchronizeDatabaseJob = synchronizeDatabaseJob;
        this.computerUpdateJob = computerUpdateJob;
    }

    @RequestMapping(value = "/batch", method = RequestMethod.POST)
    @ResponseBody
    public String saveTracksFromRequestBodyToDatabase(@RequestBody String content) {
        System.out.println("==== Encoded content ====\n" + content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.println("==== Decoded content ====\n" + decodedString);
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("currentDate", new Date())
                .toJobParameters();
        return "Request with batch has been sent";
    }

    @RequestMapping(value = "/job/file/{fileName}", method = RequestMethod.GET)
    public void saveTracksFromCsvToDatabase(@PathVariable String fileName) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("uniqueName", System.nanoTime())
                    .addString("fileName", fileName + ".csv")
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(csvToDatabaseJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException
                | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/job/synchronize", method = RequestMethod.GET)
    public void synchronizeDatabase() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("uniqueName", System.nanoTime())
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(synchronizeDatabaseJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/job/synchronize/products", method = RequestMethod.GET)
    public ExitStatus synchronizeProducts() throws Exception {
        Job synchronizeProductsJob = this.context.getBean("synchronizeProductsJob", Job.class);
        JobParameters jobParameters = new JobParametersBuilder(this.jobExplorer)
                .getNextJobParameters(synchronizeProductsJob)
                .toJobParameters();
        return this.jobLauncher.run(synchronizeProductsJob, jobParameters).getExitStatus();
    }

    @RequestMapping(value = "/computers/update", method = RequestMethod.POST)
    @ResponseBody
    public String updateComputersFromRequestBodyInDatabase(@RequestBody String content) throws Exception {
        System.out.println("==== Encoded content with Computers ====\n" + content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.println("==== Decoded content with Computers ====\n" + decodedString);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileContent", decodedString)
                .addDate("date", new Date())
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(computerUpdateJob, jobParameters);
        return "Request with batch has been sent";
    }
}
