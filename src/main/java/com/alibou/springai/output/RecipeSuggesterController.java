package com.alibou.springai.output;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recipes/suggester")
public class RecipeSuggesterController {

    private final ChatClient chatClient;
    private final DefaultConversionService conversionService;

    public RecipeSuggesterController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.conversionService = new DefaultConversionService();
    }

    @GetMapping
    public List<String> suggestRecipeFromIngredient(
            @RequestParam(name = "ingredient", defaultValue = "shrimp") String ingredient) {
        return createAndProcessPrompt(
                """
                Please suggest me the best 10 dishes containing the ingredient {ingredient}.
                Just say "I don't know" if you don't know the answer.
                {format}
                """,
                Map.of("ingredient", ingredient),
                new ListOutputConverter(conversionService)
        );
    }

    @GetMapping("/country")
    public Map<String, Object> suggestRecipeFromIngredientByCountry(
            @RequestParam(name = "ingredient", defaultValue = "shrimp") String ingredient) {
        return createAndProcessPrompt(
                """
                Please suggest me the best dishes containing the ingredient {ingredient}.
                Include the country of origin as a key and the dish name as a value.
                Just say "I don't know" if you don't know the answer.
                {format}
                """,
                Map.of("ingredient", ingredient),
                new MapOutputConverter()
        );
    }

    @GetMapping("/best")
    public Recipe suggestBestRecipeFromIngredient(
            @RequestParam(name = "ingredient", defaultValue = "shrimp") String ingredient) {
        return createAndProcessPrompt(
                """
                Please suggest me the best dish containing the ingredient {ingredient}.
                Include dish name, country of origin, and the number of calories in that dish.
                Just say "I don't know" if you don't know the answer.
                {format}
                """,
                Map.of("ingredient", ingredient),
                new BeanOutputConverter<>(Recipe.class)
        );
    }

    @GetMapping("/best-list")
    public List<Recipe> suggestBestRecipeFromIngredientAsList(
            @RequestParam(name = "ingredient", defaultValue = "shrimp") String ingredient) {
        return createAndProcessPrompt(
                """
                Please suggest me the best 10 dishes containing the ingredient {ingredient}.
                Include dish name, country of origin, and the number of calories in that dish.
                Just say "I don't know" if you don't know the answer.
                {format}
                """,
                Map.of("ingredient", ingredient),
                new BeanOutputConverter<>(new ParameterizedTypeReference<List<Recipe>>() {})
        );
    }

    private <T> T createAndProcessPrompt(String message, Map<String, Object> values, BeanOutputConverter<T> converter) {
        final PromptTemplate promptTemplate = new PromptTemplate(message, populateTemplateValues(values, converter.getFormat()));
        String response = this.chatClient.prompt(promptTemplate.create()).call().content();
        return converter.convert(response);
    }

    private List<String> createAndProcessPrompt(String message, Map<String, Object> values, ListOutputConverter converter) {
        final PromptTemplate promptTemplate = new PromptTemplate(message, populateTemplateValues(values, converter.getFormat()));
        String response = this.chatClient.prompt(promptTemplate.create()).call().content();
        return converter.convert(response);
    }

    private Map<String, Object> createAndProcessPrompt(String message, Map<String, Object> values, MapOutputConverter converter) {
        final PromptTemplate promptTemplate = new PromptTemplate(message, populateTemplateValues(values, converter.getFormat()));
        String response = this.chatClient.prompt(promptTemplate.create()).call().content();
        return converter.convert(response);
    }

    private Map<String, Object> populateTemplateValues(Map<String, Object> originalValues, String format) {
        originalValues.put("format", format);
        return originalValues;
    }

    public record Recipe(String name, String country, int calories) {}
}