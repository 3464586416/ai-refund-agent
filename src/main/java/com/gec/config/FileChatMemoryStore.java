package com.gec.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileChatMemoryStore implements ChatMemoryStore {

    private static final String MEMORY_DIR = "chat-memory";

    public FileChatMemoryStore() {
        try {
            Files.createDirectories(Paths.get(MEMORY_DIR));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getMemoryFilePath(Object memoryId) {
        return Paths.get(MEMORY_DIR, "memory-" + memoryId + ".json");
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Path path = getMemoryFilePath(memoryId);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(path);
            // Simple JSON parsing - in production you'd use a proper JSON library
            return new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // For now, store a simple representation
        Path path = getMemoryFilePath(memoryId);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[\n");
            for (int i = 0; i < messages.size(); i++) {
                ChatMessage msg = messages.get(i);
                sb.append("  {\"type\": \"").append(msg.getClass().getSimpleName())
                  .append("\", \"text\": \"")
                  .append(msg.toString().replace("\"", "\\\""))
                  .append("\"}");
                if (i < messages.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(path, sb.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Path path = getMemoryFilePath(memoryId);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
