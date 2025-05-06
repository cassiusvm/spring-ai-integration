package com.alibou.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipes/sea-food")
public class SeaFoodRecipeController {

    private static final String DEFAULT_MESSAGE = "Suggest a recipe for dinner";
    private static final String SYSTEM_MESSAGE = """
        Suggest sea food recipe.
        If someone asks about something else, just say I don't know.
        """;

    private final ChatClient chatClient;

    public SeaFoodRecipeController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/suggest-recipe")
    public String suggestRecipe(
            @RequestParam(
                    name = "message",
                    defaultValue = DEFAULT_MESSAGE
            ) String message
    ) {
        return getSeaFoodRecipeSuggestions(message);
    }

    private String getSeaFoodRecipeSuggestions(String userMessage) {
        return this.chatClient.prompt()
                .system(c -> c.text(SYSTEM_MESSAGE))
                .user(userMessage)
                .call()
                .content();
    }
}