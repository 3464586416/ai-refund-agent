package com.gec.config;

import com.gec.service.ToolsService;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import org.springframework.core.io.ClassPathResource;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class AiConfig {

    // 定义AI助手的公共接口
    public interface Assistant{
        // 普通聊天方法（阻塞式，返回完整响应）
        public abstract String chat(String message);

        // 流式响应方法（实时返回生成的token）
        public abstract TokenStream stream(String message);

        //定义角色
        @SystemMessage("""
                您是"12345"铁路公司的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
                您正在通过在线聊天系统与客户互动。
                在提供有关预订或取消预订的信息之前，您必须始终从用户处获取以下信息:预订号、客户姓名。
                请讲中文。
                今天的日期是 {{current_date}}.
                """)
        TokenStream stream(@UserMessage String userMessage,
                           @V("current_date") String currentDate);
    }

    public interface AssistantUnique{
        // 普通聊天方法（阻塞式，返回完整响应）
        String chat(@MemoryId int memoryId, @UserMessage String message);

        /**
         * 流式聊天方法（实时返回token）
         *
         *@param memoryId 对话记忆ID（用于区分不同会话）
         *@param userMessage 用户输入的消息
         *@return TokenStream 流式响应对象
         */
        TokenStream stream(@MemoryId int memoryId,@UserMessage String userMessage);
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }

    // 定义Spring Bean来创建助手实例
    @Bean
    public Assistant getAssistant(
            ChatLanguageModel chatLanguageModel,
            StreamingChatLanguageModel streamingChatLanguageModel,
            ToolsService toolsService){

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(chatMemory)
                .tools(toolsService)
                .build();

        return assistant;
    }

    @Bean
    public Assistant qwenAssistant(ChatLanguageModel qwenChatModel,
                               StreamingChatLanguageModel qwenStreamingChatModel,
                               ToolsService toolsService,
                               EmbeddingStore embeddingStore,
                               QwenEmbeddingModel qwenEmbeddingModel
    ) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .tools(toolsService)
                .contentRetriever(contentRetriever)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .build();

        return  assistant;
    }


    /**
     * 创建AssistantUnique实例的方法
     */
    @Bean
    public AssistantUnique getAssistantUnique(ChatLanguageModel chatLanguageModel,
                                              StreamingChatLanguageModel streamingChatLanguageModel){

        AssistantUnique assistantUnique = AiServices.builder(AssistantUnique.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId->
                        MessageWindowChatMemory.builder().maxMessages(10)
                                .id(memoryId)
                                .build())
                .build();

        return assistantUnique;
    }

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(
            QwenEmbeddingModel qwenEmbeddingModel,
            EmbeddingStore embeddingStore
    ) {
        System.out.println("--------------------ingestTermOfServiceToVectorStore--------------------------");

        return args -> {
            try {
                ClassPathResource resource = new ClassPathResource("rag/terms-of-service.txt");
                String text;
                try (InputStream is = resource.getInputStream()) {
                    text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }

                Document document = Document.from(text);
                DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(200, 50);
                List<TextSegment> segments = splitter.split(document);
                List<Embedding> embeddings = qwenEmbeddingModel.embedAll(segments).content();
                embeddingStore.addAll(embeddings, segments);
                System.out.println("成功将服务条款文档加载到向量存储中");
            } catch (Exception e) {
                System.err.println("警告: RAG文档加载失败，将使用不带RAG的AI对话模式。原因: " + e.getMessage());
            }
        };
    }
}
