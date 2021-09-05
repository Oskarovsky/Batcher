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

    @Qualifier("encodedCsvToDatabaseJob")
    Job encodedCsvToDatabaseJob;

    @Qualifier("productUpsertInMainDatabaseJob")
    Job productUpsertInMainDatabaseJob;

    @Qualifier("readCustomersFromFileJob")
    Job readCustomersFromFileJob;

    @Qualifier("readMultipleRecordsFromFileJob")
    Job readMultipleRecordsFromFileJob;

    @Qualifier("readXmlFileJob")
    Job readXmlFileJob;

    @Qualifier("validationClientFileJob")
    Job validationClientFileJob;

    @Qualifier("formatPlayerJob")
    Job formatPlayerJob;

    public BatchController(JobLauncher jobLauncher,
                           JobExplorer jobExplorer,
                           Job csvToDatabaseJob,
                           Job requestToDatabaseJob,
                           Job synchronizeDatabaseJob,
                           Job computerUpdateJob,
                           Job formatPlayerJob,
                           Job productUpsertInMainDatabaseJob,
                           Job readCustomersFromFileJob,
                           Job readXmlFileJob,
                           Job validationClientFileJob,
                           Job readMultipleRecordsFromFileJob,
                           Job encodedCsvToDatabaseJob) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.csvToDatabaseJob = csvToDatabaseJob;
        this.readXmlFileJob = readXmlFileJob;
        this.formatPlayerJob = formatPlayerJob;
        this.validationClientFileJob = validationClientFileJob;
        this.readCustomersFromFileJob = readCustomersFromFileJob;
        this.readMultipleRecordsFromFileJob = readMultipleRecordsFromFileJob;
        this.requestToDatabaseJob = requestToDatabaseJob;
        this.synchronizeDatabaseJob = synchronizeDatabaseJob;
        this.computerUpdateJob = computerUpdateJob;
        this.productUpsertInMainDatabaseJob = productUpsertInMainDatabaseJob;
        this.encodedCsvToDatabaseJob = encodedCsvToDatabaseJob;
    }

    /** Next steps of saveTracksFromRequestBodyToDatabase function:
     * 1. get encoded data from request body,
     * 2. decode data from Base64 to CSV format,
     * 3. display CSV content file in console,
     * 4. save records from CSV file to database
     */
    @RequestMapping(value = "/batch", method = RequestMethod.POST)
    @ResponseBody
    public String saveTracksFromRequestBodyToDatabase(@RequestBody String content) {
        System.out.println("==== Encoded content ====\n" + content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.println("==== Decoded content ====\n" + decodedString);
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("decodedContent", decodedString)
                    .addDate("currentDate", new Date())
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(encodedCsvToDatabaseJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException
                | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
        return "Request with batch has been sent";
    }

    /** Next steps of saveTracksFromCsvToDatabase function:
     * 1. get filename from url,
     * 2. fetch CSV file content from resources directory,
     * 3. save records from CSV file to database
     */
    @RequestMapping(value = "/job/file/{fileName}", method = RequestMethod.GET)
    public void saveTracksFromCsvToDatabase(@PathVariable String fileName) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("uniqueName", System.nanoTime())
                    .addString("fileName", fileName + ".csv")
                    .toJobParameters();
            jobLauncher.run(csvToDatabaseJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException
                | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }

    /** Next steps of synchronizeDatabase function:
     * 1. check main database,
     * 2. check backup database,
     * 3. save elements from main database to back up database
     */
    @RequestMapping(value = "/job/synchronize", method = RequestMethod.GET)
    public void synchronizeDatabase() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("uniqueName", System.nanoTime())
                    .toJobParameters();
            jobLauncher.run(synchronizeDatabaseJob, jobParameters);
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

    /** Next steps of upsertComputersFromRequestBodyInDatabase function:
     * 1. get encoded data from request body,
     * 2. decode data from Base64 to CSV format,
     * 3. display CSV content file in console,
     * 3. upsert elements from CSV file to main database
     */
    @RequestMapping(value = "/computers/update", method = RequestMethod.POST)
    @ResponseBody
    public String upsertComputersFromRequestBodyInDatabase(@RequestBody String content) throws Exception {
        System.out.println("==== Encoded content with Computers ====\n" + content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.println("==== Decoded content with Computers ====\n" + decodedString);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileContent", decodedString)
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(computerUpdateJob, jobParameters);
        return "Request with batch has been sent";
    }

    /** Next steps of upsertProductsFromRequestBodyInMainDatabase function:
     * 1. get encoded data from request body,
     * 2. decode data from Base64 to CSV format,
     * 3. display CSV content file in console,
     * 3. upsert elements from CSV file to main database
     */
    @RequestMapping(value = "/product/{productType}/update", method = RequestMethod.POST)
    @ResponseBody
    public String upsertProductsFromRequestBodyInMainDatabase(@PathVariable String productType,
                                                              @RequestBody String content) throws Exception {
        System.out.printf("==== Encoded content with product item: %s ====\n %s\n", productType, content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.printf("==== Decoded content with product item: %s ====\n %s\n", productType, decodedString);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("productType", productType)
                .addString("fileContent", decodedString)
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(productUpsertInMainDatabaseJob, jobParameters);
        return "Request with batch has been sent";
    }

    /** Next steps of readFileWithCustomers function:
     * 1. get encoded data from request body,
     * 2. decode data from Base64 to CSV format,
     * 3. display CSV content file in console,
     */
    @RequestMapping(value = "/customer/read", method = RequestMethod.POST)
    @ResponseBody
    public String readFileWithCustomers(@RequestBody String content) throws Exception {
        System.out.printf("==== Encoded content with product item: Customer ====\n %s\n", content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.printf("==== Decoded content with product item: Customer ====\n %s\n", decodedString);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileContent", decodedString)
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(readCustomersFromFileJob, jobParameters);
        return "Request with batch has been sent";
    }

    /** Next steps of readFileWithMultipleRecords function:
     * 1. get encoded data from request body,
     * 2. decode data from Base64 to CSV format,
     * 3. split record types based on prefixes in lines
     * 4. create the appropriate objects
     * 5. display CSV content file in console
     */
    @RequestMapping(value = "/records/read", method = RequestMethod.POST)
    @ResponseBody
    public String readFileWithMultipleRecords(@RequestBody String content) throws Exception {
        System.out.printf("==== Encoded content with multiple products ====\n %s\n", content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.printf("==== Decoded content with multiple products ====\n %s\n", decodedString);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileContent", decodedString)
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(readMultipleRecordsFromFileJob, jobParameters);
        return "Request with batch has been sent";
    }

    /** Next steps of readXmlFile function:
     * 1. get encoded data from request body,
     * 2. decode data from Base64 to XML format
     */
    @RequestMapping(value = "/xml/read", method = RequestMethod.POST)
    @ResponseBody
    public String readXmlFile(@RequestBody String content) throws Exception {
        System.out.printf("==== Encoded content with XML structure ====\n %s\n", content);
        Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(content));
        System.out.printf("==== Decoded content with XML structure ====\n %s\n", decodedString);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileContent", decodedString)
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(readXmlFileJob, jobParameters);
        return "Request with batch has been sent";
    }

    /** Next steps of itemProcessorValidator function:
     * 1. get data from csv file from resources,
     * 2.
     */
    @RequestMapping(value = "/client/csv", method = RequestMethod.GET)
    @ResponseBody
    public String itemProcessorValidator() throws Exception {
        System.out.println("==== Fetching CSV file from resource ====");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("clientFile", "input/client.csv")
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(validationClientFileJob, jobParameters);
        return "Request with batch has been sent";
    }

    /** Next steps of formatPlayerFromFile function:
     * 1. get data from csv file from resources,
     * 2.
     */
    @RequestMapping(value = "/player/csv", method = RequestMethod.GET)
    @ResponseBody
    public String formatPlayerFromFile() throws Exception {
        System.out.println("==== Fetching CSV file from resource ====");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("playerFile", "src/main/resources/input/player.csv")
                .addString("outputFile", "src/main/resources/input/output.csv")
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(formatPlayerJob, jobParameters);
        return "Request with batch has been sent";
    }
}
