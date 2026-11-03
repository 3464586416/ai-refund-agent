package com.gec.controller;

import com.gec.config.AiConfig;
import com.gec.config.AssistantConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.TokenStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    ChatLanguageModel chatLanguageModel;

    @Autowired
    @Qualifier("getAssistant")
    AiConfig.Assistant assistant;

    @Autowired
    AiConfig.AssistantUnique assistantUnique;

    @Autowired
    AssistantConfig.AssistantUniqueStore assistantUniqueStore;


    @RequestMapping(value = "/memory_steam_chat", produces = "text/stream;charset=utf-8")
    public Flux<String> memoryStreamChat(
            @RequestParam(defaultValue = "我是谁") String message,
            HttpServletResponse response) {

        TokenStream stream = assistant.stream(message, LocalDate.now().toString());

        return Flux.create(sink -> {
            stream.onPartialResponse(s -> sink.next(s))
                    .onCompleteResponse(c -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }
}
