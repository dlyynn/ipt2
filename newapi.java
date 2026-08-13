import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class newapi {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("                     JOKES???                 ");
        System.out.println("==============================================");
        System.out.print("Enter a joke type (general, programming, knock-knock) or press Enter for random: ");

        String userInput = scanner.nextLine().trim().toLowerCase();

        String apiUrl;
        if (userInput.isEmpty()) {
            apiUrl = "https://official-joke-api.appspot.com/jokes/random";
        } else {
            apiUrl = "https://official-joke-api.appspot.com/jokes/" + userInput + "/random";
        }

        try {
            URL url = URI.create(apiUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            InputStream stream = (responseCode >= 200 && responseCode < 300) 
                                 ? connection.getInputStream() 
                                 : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder responseBuffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseBuffer.append(line);
            }
            reader.close();
            connection.disconnect();

            String rawJson = responseBuffer.toString();

            System.out.println("\n--- RAW JSON RESPONSE ---");
            System.out.println(rawJson);

            if (responseCode != 200) {
                System.out.println("\n[Error] Request failed with HTTP Status Code: " + responseCode);
                return;
            }

            System.out.println("\n--- EXTRACTED INFORMATION ---");
            String jokeId = extractRegexValue(rawJson, "\"id\":\\s*([0-9]+)");
            String type = extractJsonString(rawJson, "type");
            String setup = extractJsonString(rawJson, "setup");
            String punchline = extractJsonString(rawJson, "punchline");

            System.out.println("Joke ID:   " + jokeId);
            System.out.println("Type:      " + type);
            System.out.println("Setup:     " + cleanString(setup));
            System.out.println("Punchline: " + cleanString(punchline));

        } catch (Exception e) {
            System.out.println("\n[Error] An unexpected error occurred: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static String extractJsonString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*\"(.*?)(?<!\\\\)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }

    private static String extractRegexValue(String json, String regexPattern) {
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }

    private static String cleanString(String text) {
        return text.replace("\\\"", "\"").replace("\\n", "\n");
    }
}