package com.oskarro.batcher.batch.csvToDatabase;

import com.oskarro.batcher.environment.main.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final String START_MESSAGE = "%s is beginning execution";
    private static final String END_MESSAGE = "=== Procedure %s has completed with the status %s === ";

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    public String taskInfo;


    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate, @Value("DefaultValue") String taskInfo) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskInfo = taskInfo;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("============ JOB STARTED [{}] ============", taskInfo);
        System.out.printf(START_MESSAGE, jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("============ JOB FINISHED [{}] ============\nVerifying the results:\n", taskInfo);

            List<Track> results = jdbcTemplate.query("SELECT id, title, artist, version, url, code FROM tracks",
                    (rs, row) -> new Track(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6))
            );

            for (Track track : results) {
                log.info("Discovered <" + track + "> in the database.");
            }
        }
        System.out.printf(END_MESSAGE, jobExecution.getJobInstance().getJobName(), jobExecution.getStatus());
    }
}
