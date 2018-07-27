package com.ef.config;

import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.ef.LogRegistration;

@Configuration
public class LogJob {

    private static final String INSERT_REGISTRATION_QUERY =
            "insert into LOG_REGISTRATION (REQ_TM, IP_ADDR, REQ_TYPE, RESP_CODE, USER_AGENT)" +
            " values " +
            "(:reqTm,:ipAddress,:reqType,:respCode,:userAgent)";

    @Value("yyyy-MM-dd HH:mm:ss.SSS")
    private String dateFormat;
    
    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private DataSource dataSource;
    
    private static final String WILL_BE_INJECTED = null;

    @Bean
    public Job insertIntoDbFromCsvJob() {
        return jobs.get("Log Registration Import Job")
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return steps.get("Log Registration Log To DB Step")
                .<LogRegistration,LogRegistration>chunk(10000)
                .faultTolerant()
                .reader(csvFileReader(WILL_BE_INJECTED))
                .writer(jdbcItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<LogRegistration> csvFileReader(@Value("${user.home}/batches/#{jobParameters[fileName]}") String pathToFile) {
        FlatFileItemReader<LogRegistration> itemReader = new FlatFileItemReader<>();
        itemReader.setLineMapper(lineMapper());
        itemReader.setResource(new FileSystemResource(pathToFile));
        return itemReader;
    }

    @Bean
    public JdbcBatchItemWriter<LogRegistration> jdbcItemWriter() {
        JdbcBatchItemWriter<LogRegistration> itemWriter = new JdbcBatchItemWriter<>();
        itemWriter.setDataSource(dataSource);
        itemWriter.setSql(INSERT_REGISTRATION_QUERY);
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return itemWriter;
    }

    @Bean
    public DefaultLineMapper<LogRegistration> lineMapper() {
        DefaultLineMapper<LogRegistration> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer());
        lineMapper.setFieldSetMapper(fieldSetMapper());
        return lineMapper;
    }

    @Bean
    public BeanWrapperFieldSetMapper<LogRegistration> fieldSetMapper() {
        BeanWrapperFieldSetMapper<LogRegistration> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(LogRegistration.class);
        fieldSetMapper.setCustomEditors(Collections.singletonMap(Date.class, 
                customDateEditor()));
        return fieldSetMapper;
    }
    
	@Bean
	public PropertyEditor customDateEditor() {
		return new CustomDateEditor(new SimpleDateFormat(dateFormat) , false);
	}

    @Bean
    public DelimitedLineTokenizer tokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter("|");
        tokenizer.setNames("reqTm","ipAddress","reqType","respCode","userAgent");
        return tokenizer;
    }
}
