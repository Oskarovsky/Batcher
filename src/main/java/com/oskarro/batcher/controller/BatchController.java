package com.oskarro.batcher.controller;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api")
public class BatchController {

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    JobLauncher jobLauncher;

    @Qualifier("csvToDatabaseJob")
    Job csvToDatabaseJob;

    @Qualifier("requestToDatabaseJob")
    Job requestToDatabaseJob;

    public BatchController(JobLauncher jobLauncher, Job csvToDatabaseJob, Job requestToDatabaseJob) {
        this.jobLauncher = jobLauncher;
        this.csvToDatabaseJob = csvToDatabaseJob;
        this.requestToDatabaseJob = requestToDatabaseJob;
    }

    @RequestMapping(value = "/batch", method = RequestMethod.POST)
    @ResponseBody
    public String saveTracksFromRequestBodyToDatabase(@RequestBody String content) {
        System.out.println("==== Encoded content ====\n" + content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.println("==== Decoded content ====\n" + decodedString);
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("currentDate", new Date())
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(requestToDatabaseJob, jobParameters);
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException
                | JobParametersInvalidException | JobRestartException e) {
            e.printStackTrace();
        }
        return "Request with batch has been sent";
    }

    @RequestMapping(value = "/job/{fileName}", method = RequestMethod.GET)
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
}
