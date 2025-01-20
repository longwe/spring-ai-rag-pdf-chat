package com.ezcloud.spring.ai.springaipdfragchat.controller;

import com.ezcloud.spring.ai.springaipdfragchat.model.Answer;
import com.ezcloud.spring.ai.springaipdfragchat.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tax-certificate")
public class MalawiHistoryController {

    private final ChatClient aiClient;

    private final VectorStore vectorStore;

    @Value("classpath:/Tax_Cert.pdf")
    private Resource systemPrompt ;

    private static final String userPrompt = """
            You are a helpful assistant, conversing with a user about the subjects contained in a set of documents.
            Use the information from the DOCUMENTS section to provide accurate answers. If unsure or if the answer
            isn't found in the DOCUMENTS section, simply state that you don't know the answer.
            
            QUESTION:
            {input}
            
            DOCUMENTS:
            {documents}
            
            """;

    public MalawiHistoryController(ChatClient.Builder aiClient, VectorStore vectorStore) {
        this.aiClient = aiClient.build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/ask")
    public String researchMalawiHistory(@RequestParam(value = "question", defaultValue = "Summarize the contents in this document") String question) {
        var promptTemplate = new PromptTemplate(systemPrompt);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", question);
        promptParameters.put("documents", findSimilarData(question));

        return aiClient
                .prompt(promptTemplate.create(promptParameters))
                .call()
                .content();
    }
    String findSimilarData(String question) {
        return vectorStore.similaritySearch(SearchRequest.query(question)
                        .withTopK(5))

                .stream()
                .map(Document::getContent)
                .reduce("", (a, b) -> a + System.lineSeparator() + b);
    }

    @GetMapping("/ask-tax-info")
    public String getConstitutionInformation(@RequestParam(defaultValue = "Can you summarize the" +
            " contents of the document in not more than 100 words") String question) {
        return aiClient.prompt()
                .user(question)
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.query(question)))
                .call()
                .content();
    }
    @PostMapping("/chat")
    public Answer ask(@RequestBody Question question) {
        var promptTemplate = new PromptTemplate(userPrompt);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", question);
        promptParameters.put("documents", findSimilarData(question.question()));

    var answer= aiClient
                .prompt(promptTemplate.create(promptParameters))
                .call()
                .content();

        return new Answer(answer);
    }



    String getSimilarData(String question) {
        return vectorStore.similaritySearch(SearchRequest.query(question)
                        .withTopK(5))

                .stream()
                .map(Document::getContent)
                .reduce("", (a, b) -> a + System.lineSeparator() + b);
    }
}