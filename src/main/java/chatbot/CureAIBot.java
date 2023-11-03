package chatbot;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;



public class CureAIBot {
    OkHttpClient client;
    private static final String API_KEY = "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";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/engines/gpt-3.5-turbo-instruct/completions";

    private Patient patient;

    private final String RESET = "\u001B[0m";
    private final String RED = "\u001B[31m";
    private final String CYAN = "\u001B[36m";
    private final int RESPONSE_TIME_API = 10;

    public CureAIBot() throws IOException {
        this.client = new OkHttpClient();
        this.patient = new Patient();
    }

    public void startChat() throws Exception {
        Scanner chatInput = new Scanner(System.in);

        System.out.println(CYAN + "INITIALIZING CUREAI VERSION 1.0.0..." + RESET);

        System.out.println(CYAN + "CureAI: " + RESET + getChatResponse("Give me a three sentence welcome message to CureAI (a health/wellbeing AI service), where the helper's name (you) is Curie, in a professional manner.", true) + "\n");
        System.out.print("Enter 1 for a medical diagnosis or 2 for general assistance: ");
        String option = chatInput.nextLine();
        boolean needsDiagnosis = false;

        if (option.equals("1")) {
            needsDiagnosis = true;
            System.out.print("Please provide your name, age, and symptoms so I can better assist you: ");
            String input = chatInput.nextLine();
            List<String> temp = identifyPatientDetails(input);
            patient.setName(temp.get(0));
            patient.setAge(temp.get(1));
            patient.setSymptoms(temp.get(2));
        } else {
            needsDiagnosis = false;
        }

        boolean active = true;
        while (active) {
            System.out.print("Send a message (type x to end session, g to get diagnosis, and p to show profile): ");
            String userInput = chatInput.nextLine();

            if (userInput.equals("x")) {
                System.exit(0);
            } else if (userInput.equals("p")) {
                System.out.println("Patient Profile\n" + patient.toString());
            } else if (userInput.equals("g")) {
                String diagnosis = getDiagnosis(this.patient);
                this.patient.setDiagnosis(diagnosis);
                System.out.println(RED + "Diagnosis: " + diagnosis + RESET);
            }else {
                String output = getChatResponse(userInput, needsDiagnosis);
                System.out.println(CYAN + "CureAI: " + RESET + output);
            }
            System.out.println();
        }
    }

    private String getChatResponse(String input, boolean needsDiagnosis) throws Exception {

        String prompt = "I am not greatly knowledgeable in medicine/health. " + input ;

        JSONObject requestBody = new JSONObject();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 2000);  // Adjust the max_tokens as needed for the desired length of the summary

        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        int attempts = 0;
        while (attempts < 5) {
            try (Response response = client.newCall(request).execute()) {
                int i = 0;
                int statusCode = response.code();
                while (i < RESPONSE_TIME_API) {
                    if (statusCode == 200) {
                        i = RESPONSE_TIME_API;
                    } else {
                        TimeUnit.SECONDS.sleep(1);
                        i++;
                    }
                }

                if (statusCode == 200) {
                    //System.out.println("json: " + response.toString());
                    String responseBody = response.body().string();
                    //System.out.println("response: " + responseBody);
                    Object obj = JSONValue.parse(responseBody);
                    JSONObject jsonResponse = (JSONObject) obj;
                    JSONArray choicesArray = (JSONArray) jsonResponse.get("choices");
                    JSONObject firstChoice = (JSONObject) choicesArray.get(0);
                    String output = (String) firstChoice.get("text");

                    if (!output.equals("")) {
                        return output.replaceAll("^\\s+", "");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            attempts++;
        }
        return "Error: CureAI timed out";
    }

    private List<String> identifyPatientDetails(String input) {
        List<String> data = new ArrayList<>();

        String prompt = input + ". Give me a numbered list in the following order 1. name, 2. age, and 3. symptoms (a single, concatenated string). If you can't identify some information, put NONE in the associated numbered item.";

        JSONObject requestBody = new JSONObject();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 500);  // Adjust the max_tokens as needed for the desired length of the summary

        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        int attempts = 0;
        while (attempts < 3) {
            try (Response response = client.newCall(request).execute()) {
                int i = 0;
                int statusCode = response.code();
                while (i < RESPONSE_TIME_API) {
                    if (statusCode == 200) {
                        i = RESPONSE_TIME_API;
                    } else {
                        TimeUnit.SECONDS.sleep(1);
                        i++;
                    }
                }

                if (statusCode == 200) {
                    //System.out.println("json: " + response.toString());
                    String responseBody = response.body().string();
                    //System.out.println("response: " + responseBody);
                    Object obj = JSONValue.parse(responseBody);
                    JSONObject jsonResponse = (JSONObject) obj;
                    JSONArray choicesArray = (JSONArray) jsonResponse.get("choices");
                    JSONObject firstChoice = (JSONObject) choicesArray.get(0);
                    String output = (String) firstChoice.get("text");

                    if (!output.equals("")) {
                        int beginName = output.indexOf("1.");
                        int beginAge = output.indexOf("2.");
                        int beginSymptoms = output.indexOf("3.");
                        data.add(output.substring(beginName + 2, beginAge - 1));
                        data.add(output.substring(beginAge + 2, beginSymptoms - 1));
                        data.add(output.substring(beginSymptoms + 2, output.length() - 1));
                        return data;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            attempts++;
        }
        return data;
    }

    private String getDiagnosis(Patient patient) {
        String prompt = "I am " + patient.getAge() + "and I am experiencing " + patient.getSymptoms() + ". What could this mean? How urgent is this issue and should I see a doctor? Can you give some possible treatment options as well?";
        JSONObject requestBody = new JSONObject();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 500);  // Adjust the max_tokens as needed for the desired length of the summary

        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        int attempts = 0;
        while (attempts < 3) {
            try (Response response = client.newCall(request).execute()) {
                int i = 0;
                int statusCode = response.code();
                while (i < RESPONSE_TIME_API) {
                    if (statusCode == 200) {
                        i = RESPONSE_TIME_API;
                    } else {
                        TimeUnit.SECONDS.sleep(1);
                        i++;
                    }
                }

                if (statusCode == 200) {
                    String responseBody = response.body().string();
                    Object obj = JSONValue.parse(responseBody);
                    JSONObject jsonResponse = (JSONObject) obj;
                    JSONArray choicesArray = (JSONArray) jsonResponse.get("choices");
                    JSONObject firstChoice = (JSONObject) choicesArray.get(0);
                    String output = (String) firstChoice.get("text");

                    if (!output.equals("")) {
                        return output.trim();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            attempts++;
        }
        return "N/A";
    }
}
