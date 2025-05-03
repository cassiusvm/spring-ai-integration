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

    public RecipeSuggesterController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping
    public List<String> suggestRecipeFromIngredient(
            @RequestParam(
                    name = "ingredient",
                    defaultValue = "shrimp"
            ) String ingredient
    ) {
        String message = """
                Please suggest me the best 10 dishes containing the ingredient {ingredient}.
                Just say "I don't know" if you don't know the answer.
                {format}
                """;

        final ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());

        final PromptTemplate promptTemplate = new PromptTemplate(
                message,
                Map.of(
                        "ingredient", ingredient,
                       "format", listOutputConverter.getFormat()
                )
        );
        String response = this.chatClient.prompt(promptTemplate.create())
                .call()
                .content();

        return listOutputConverter.convert(response);

    }

    @GetMapping("/country")
    public Map<String, Object> suggestRecipeFromIngredientByCountry(
            @RequestParam(
                    name = "ingredient",
                    defaultValue = "shrimp"
            ) String ingredient
    ) {
        String message = """
                Please suggest me the best dishes containing the ingredient {ingredient}.
                Include the country of origin as a key and the dish name as a value.
                Just say "I don't know" if you don't know the answer.
                {format}
                """;

        final MapOutputConverter mapOutputConverter = new MapOutputConverter();

        final PromptTemplate promptTemplate = new PromptTemplate(
                message,
                Map.of(
                        "ingredient", ingredient,
                       "format", mapOutputConverter.getFormat()
                )
        );
        String response = this.chatClient.prompt(promptTemplate.create())
                .call()
                .content();

        return mapOutputConverter.convert(response);

    }

    @GetMapping("/best")
    public Recipe suggestBestRecipeFromIngredient(
            @RequestParam(
                    name = "ingredient",
                    defaultValue = "shrimp"
            ) String ingredient
    ) {
        String message = """
                Please suggest me the best dish containing the ingredient {ingredient}.
                Include dish name, country of origin, and the number of calories in that dish.
                Just say "I don't know" if you don't know the answer.
                {format}
                """;

        final BeanOutputConverter<Recipe> beanOutputConverter = new BeanOutputConverter<>(Recipe.class);

        final PromptTemplate promptTemplate = new PromptTemplate(
                message,
                Map.of(
                        "ingredient", ingredient,
                       "format", beanOutputConverter.getFormat()
                )
        );
        String response = this.chatClient.prompt(promptTemplate.create())
                .call()
                .content();

        return beanOutputConverter.convert(response);

    }

    @GetMapping("/best-list")
    public List<Recipe> suggestBestRecipeFromIngredientAsList(
            @RequestParam(
                    name = "ingredient",
                    defaultValue = "shrimp"
            ) String ingredient
    ) {
        String message = """
                Please suggest me the best 10 dishes containing the ingredient {ingredient}.
                Include dish name, country of origin, and the number of calories in that dish.
                Just say "I don't know" if you don't know the answer.
                {format}
                """;

        ParameterizedTypeReference<List<Recipe>> typeReference = new ParameterizedTypeReference<>() {};
        final BeanOutputConverter<List<Recipe>> beanOutputConverter = new BeanOutputConverter<>(typeReference);

        final PromptTemplate promptTemplate = new PromptTemplate(
                message,
                Map.of(
                        "ingredient", ingredient,
                       "format", beanOutputConverter.getFormat()
                )
        );
        String response = this.chatClient.prompt(promptTemplate.create())
                .call()
                .content();

        return beanOutputConverter.convert(response);

    }

    public record Recipe(String name, String country, int calories) {}
}
