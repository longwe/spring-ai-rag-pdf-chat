package com.ezcloud.spring.ai.springaipdfragchat.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OpenAiChatClientConfig {
    private final PgVectorStore pgVectorStore;
    private final ChatClient openAiChatClient;

    public OpenAiChatClientConfig(PgVectorStore pgVectorStore, ChatClient.Builder openAiChatClient) {
        this.pgVectorStore = pgVectorStore;
        this.openAiChatClient = openAiChatClient.build();
    }

    public String chat(String message, Resource template) {
        List<Document> documents = this.pgVectorStore.similaritySearch(message);
        var collect =
                documents.stream().map(Document::getContent).collect(Collectors.joining(System.lineSeparator()));
        var createdMessage = new SystemPromptTemplate(template).createMessage(Map.of("documents", collect));
        var userMessage = new UserMessage(message);
        var prompt = new Prompt(List.of(createdMessage, userMessage));
        return openAiChatClient.prompt(prompt).call().chatResponse().getResults().stream().map(generation -> {
            return generation.getOutput().getContent();
        }).collect(Collectors.joining("/n"));
    }

}
