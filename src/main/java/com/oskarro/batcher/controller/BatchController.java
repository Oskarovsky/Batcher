package com.oskarro.batcher.controller;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BatchController {

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    JobLauncher jobLauncher;
    Job job;

    public BatchController(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @RequestMapping(value = "/batch", method = RequestMethod.POST)
    @ResponseBody
    public String sendBatch(@RequestBody String content)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("content", content)
//                .toJobParameters();
//        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        System.out.println("Encoded content: " + content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.println("Decoded content:" + decodedString);
        return "Request with batch has been sent";
    }

    @GetMapping("/job")
    public void startJob() {
        Map<String, JobParameter> parameters = new HashMap<>();
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("uniqueness", System.nanoTime()).toJobParameters();
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
}
