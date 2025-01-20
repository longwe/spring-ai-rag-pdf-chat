package com.ezcloud.spring.ai.springaipdfragchat.persitence;

import com.ezcloud.spring.ai.springaipdfragchat.config.PdfFileReaderConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoader {
    private final PdfFileReaderConfig pdfFileReaderConfig;

    private final JdbcClient jdbcClient;

    @Value("classpath:/Property_3362135-1.pdf")
    private Resource resourceFile;

    public DatabaseLoader(PdfFileReaderConfig pdfFileReaderConfig, JdbcClient jdbcClient) {
        this.pdfFileReaderConfig = pdfFileReaderConfig;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    private void loadDatabase() {
        System.out.println("Loading data from the database...");

        var recordCount = jdbcClient.sql("select COUNT(*) from vector_store")
                .query(Integer.class)
                .single();
        System.out.println("Current record count: " + recordCount);
        if (recordCount == 0) {
            pdfFileReaderConfig.addResource(resourceFile);
        }
    }
}
