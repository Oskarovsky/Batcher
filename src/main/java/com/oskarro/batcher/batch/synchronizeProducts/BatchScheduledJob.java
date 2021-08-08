package com.oskarro.batcher.batch.synchronizeProducts;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduledJob extends QuartzJobBean {

    @Autowired
    ApplicationContext applicationContext;

    @Qualifier("synchronizeProductsJob")
    private final Job synchronizeProductsJob;

    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;


    public BatchScheduledJob(@Qualifier("synchronizeProductsJob") Job synchronizeProductsJob,
                             JobExplorer jobExplorer,
                             JobLauncher jobLauncher) {
        this.synchronizeProductsJob = synchronizeProductsJob;
        this.jobExplorer = jobExplorer;
        this.jobLauncher = jobLauncher;
    }

    /**
     * executeInternal method will be called once each time the scheduled event fires
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobParameters jobParameters = new JobParametersBuilder(this.jobExplorer)
                .getNextJobParameters(synchronizeProductsJob)
                .toJobParameters();
        try {
            jobLauncher.run(synchronizeProductsJob, jobParameters);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
