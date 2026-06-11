package org.com.salesagent.agent;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.com.salesagent.tool.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class SalesAgentConfig {

    private final ChatModel chatLanguageModel;
    private final SalesQueryTool salesQueryTool;
    private final SalesSummaryTool salesSummaryTool;
    private final SalesTrendTool salesTrendTool;
    private final ChartGeneratorTool chartGeneratorTool;
    private final AnomalyDetectionTool anomalyDetectionTool;

    @Bean
    public SalesAgent salesAgent() {
        return AiServices.builder(SalesAgent.class)
                .chatModel(chatLanguageModel)
                .tools(salesQueryTool,
                        salesSummaryTool,
                        salesTrendTool,
                        chartGeneratorTool,
                        anomalyDetectionTool)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}