package com.gec.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@Configuration
public class AssistantConfig {
    // 1. 注册 FileChatMemoryStore
    @Bean
    public ChatMemoryStore fileChatMemoryStore() {
        return new FileChatMemoryStore();
    }

    // 2. 为每个会话创建 ChatMemory (需配合 @Scope)
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ChatMemory chatMemory(ChatMemoryStore memoryStore) {
        int userId = 1;
        List<ChatMessage> messages = memoryStore.getMessages(userId);

        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .id(userId)
                .chatMemoryStore(memoryStore)
                .build();
    }

    public interface AssistantUniqueStore{
        //聊天方法 常规阻塞模式
        String chat(@MemoryId int memoryId, @UserMessage String message);
        //聊天方法 数据流的模式
        TokenStream stream(@MemoryId int memoryId, @UserMessage String message);
    }

    // 3. 创建 Assistant 并绑定 ChatMemory
    @Bean
    public AssistantUniqueStore assistantStore(ChatLanguageModel chatLanguageModel,
                                               ChatMemoryStore fileChatMemoryStore,
                                               StreamingChatLanguageModel streamingChatLanguageModel) {
        return AiServices.builder(AssistantUniqueStore.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                //配置对话的提供者 为每个memoryId创建独立的记忆窗口
                .chatMemoryProvider(
                        //设置最多保存10条消息
                        memoryId->MessageWindowChatMemory.builder().maxMessages(10)
                                .id(memoryId)
                                .chatMemoryStore(fileChatMemoryStore)
                                .build()
                )
                .build();
    }
}
