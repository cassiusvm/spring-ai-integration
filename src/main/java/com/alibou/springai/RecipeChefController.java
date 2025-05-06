package com.alibou.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recipes/chef")
public class RecipeChefController {

    private final ChatClient chatClient;
    private final List<Message> conversation;

    public RecipeChefController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.conversation = initializeConversation();
    }

    /**
     * Endpoint para sugestão de receitas baseado em mensagens do usuário.
     * @param message Mensagem enviada pelo usuário (padrão: "Suggest a recipe for dinner").
     * @return Resposta gerada pelo assistente.
     */
    @GetMapping("/suggest-recipe")
    public String suggestRecipe(
            @RequestParam(
                    name = "message",
                    defaultValue = "Suggest a recipe for dinner"
            ) String message
    ) {
        return processUserMessage(message);
    }

    /**
     * Inicializa a conversa com uma mensagem de sistema padrão.
     * @return Lista contendo a mensagem de sistema inicial.
     */
    private List<Message> initializeConversation() {
        List<Message> initialConversation = new ArrayList<>();
        String systemMessage = """
            Suggest sea food recipe.
            If someone asks about something else, just say I don't know.
            """;
        initialConversation.add(new SystemMessage(systemMessage));
        return initialConversation;
    }

    /**
     * Processa a mensagem do usuário, obtém a resposta do assistente e atualiza a conversa.
     * @param userInput Mensagem enviada pelo usuário.
     * @return Resposta gerada pelo modelo.
     */
    private String processUserMessage(String userInput) {
        Message userMessage = new UserMessage(userInput);
        this.conversation.add(userMessage);

        String modelResponse = this.chatClient.prompt()
                .messages(this.conversation)
                .call()
                .content();

        Message assistantMessage = new AssistantMessage(modelResponse);
        this.conversation.add(assistantMessage);

        return modelResponse;
    }
}