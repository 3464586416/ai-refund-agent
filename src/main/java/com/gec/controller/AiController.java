package com.gec.controller;

import com.gec.config.AiConfig;
import dev.langchain4j.service.TokenStream;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    @Qualifier("qwenAssistant")
    AiConfig.Assistant assistant;

    @RequestMapping(value = "/memory_steam_chat", produces = "text/stream;charset=utf-8")
    public Flux<String> memoryStreamChat(
            @RequestParam(defaultValue = "我是谁") String message,
            HttpServletRequest request) {

        // 以浏览器会话为界隔离对话记忆：不同访客互不可见
        String sessionId = request.getSession(true).getId();

        TokenStream stream = assistant.stream(sessionId, message, LocalDate.now().toString());

        return Flux.create(sink -> {
            stream.onPartialResponse(s -> sink.next(s))
                    .onCompleteResponse(c -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }
}
