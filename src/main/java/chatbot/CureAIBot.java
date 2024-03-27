package chatbot;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

@SpringBootApplication
@CrossOrigin(origins = "*")
@RestController
public class CureAIBot {
    private static final String API_KEY = "api_key_here";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/engines/gpt-3.5-turbo-instruct/completions";

    public static void main(String[] args) {
        SpringApplication.run(CureAIBot.class, args);
    }
    @GetMapping("/get-curie-response")
    private ResponseEntity<String> getCurieResponse(@RequestParam String input, @RequestParam int tokens) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String prompt = "CureAI: You are a medical chat bot named Curie designed to provide information and answer questions related to various medical conditions. Please provide advice based on the user's input.\nUser: " + input;

        List<String> responses = new ArrayList<>();
        List<Integer> tokenCounts = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            JSONObject requestBody = new JSONObject();
            requestBody.put("prompt", prompt);
            requestBody.put("max_tokens", tokens);

            RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(OPENAI_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int statusCode = response.code();
                if (statusCode == 200) {
                    String responseBody = response.body().string();
                    Object obj = JSONValue.parse(responseBody);
                    JSONObject jsonResponse = (JSONObject) obj;
                    JSONArray choicesArray = (JSONArray) jsonResponse.get("choices");
                    JSONObject firstChoice = (JSONObject) choicesArray.get(0);
                    String output = (String) firstChoice.get("text");
                    JSONObject usageResponse = (JSONObject) jsonResponse.get("usage");
                    int tokenCount = ((Long) usageResponse.get("completion_tokens")).intValue();

                    if (!output.isBlank()) {
                        responses.add(output);
                        tokenCounts.add(tokenCount);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (responses.isEmpty()) {
            return ResponseEntity.ok("CureAI did not provide a valid response.");
        }

        // Calculate the average token count
        double averageTokenCount = tokenCounts.stream().mapToDouble(Integer::doubleValue).average().orElse(0);

        // Find the token count closest to the average
        int closestTokenCount = tokenCounts.stream().min(Comparator.comparingDouble(count -> Math.abs(count - averageTokenCount))).orElse(0);

        // Get the response with the closest token count to the average
        int closestTokenCountIndex = tokenCounts.indexOf(closestTokenCount);
        String closestTokenCountResponse = responses.get(closestTokenCountIndex);

        // Remove leading and trailing whitespace, and any identifier
        closestTokenCountResponse = closestTokenCountResponse.trim().replaceAll("^[A-Za-z]+:", "").trim();

        return ResponseEntity.ok(closestTokenCountResponse);
    }

    @GetMapping("/get-diagnosis")
    private ResponseEntity<String> getDiagnosis(@RequestParam int age, @RequestParam String symptoms) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String prompt = String.format("CureAI: You are a medical chat bot designed to diagnose possible medical conditions. In one or two words, based on the information provided, the most likely condition for the user, a " + age + "-year-old experiencing " + symptoms + " is:");

        List<String> responses = new ArrayList<>();
        List<Integer> tokenCounts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            JSONObject requestBody = new JSONObject();
            requestBody.put("prompt", prompt);
            requestBody.put("max_tokens", 4);

            RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(OPENAI_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int statusCode = response.code();
                if (statusCode == 200) {
                    String responseBody = response.body().string();
                    Object obj = JSONValue.parse(responseBody);
                    JSONObject jsonResponse = (JSONObject) obj;
                    JSONArray choicesArray = (JSONArray) jsonResponse.get("choices");
                    JSONObject firstChoice = (JSONObject) choicesArray.get(0);
                    String output = (String) firstChoice.get("text");
                    JSONObject usageResponse = (JSONObject) jsonResponse.get("usage");
                    int tokenCount = ((Long) usageResponse.get("completion_tokens")).intValue();

                    if (!output.isBlank()) {
                        responses.add(output);
                        tokenCounts.add(tokenCount);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (responses.isEmpty()) {
            return ResponseEntity.ok("CureAI did not provide a valid response.");
        }

        // Find the smallest token count
        int smallestTokenCount = Collections.min(tokenCounts);

// Get the index of the response with the smallest token count
        int smallestTokenCountIndex = tokenCounts.indexOf(smallestTokenCount);

// Get the response with the smallest token count
        String smallestTokenCountResponse = responses.get(smallestTokenCountIndex);

        // Remove leading and trailing whitespace, and any identifier
        smallestTokenCountResponse = smallestTokenCountResponse.trim().replaceAll("^[A-Za-z]+:", "").trim();

        // Remove ending punctuation
        smallestTokenCountResponse = smallestTokenCountResponse.replaceAll("[.,;!?]+$", "").toUpperCase();

        return ResponseEntity.ok(smallestTokenCountResponse);
    }
}
