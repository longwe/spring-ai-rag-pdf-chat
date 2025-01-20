package com.ezcloud.spring.ai.springaipdfragchat.config;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PdfFileReaderConfig {
    private final PgVectorStore vectorStore;

    public PdfFileReaderConfig(PgVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void addResource(Resource pdfResource) {
        PdfDocumentReaderConfig pdfDocumentReaderConfig = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().build()).build();
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(pdfResource, pdfDocumentReaderConfig);
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        System.out.println("Processing: " + pdfResource.getFilename());
        System.out.println("Adding text to the vector store..." + pdfResource.getFilename());
        var splitText = textSplitter.apply(pagePdfDocumentReader.get());
        //System.out.println("Split text: " + splitText.stream().map(Object::toString).collect(Collectors.joining()));
        System.out.println("Split text: " + splitText);
        vectorStore.accept(textSplitter.apply(pagePdfDocumentReader.get()));
    }
}