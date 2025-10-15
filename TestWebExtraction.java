import java.io.*;
import java.net.*;

public class TestWebExtraction {
    public static void main(String[] args) {
        String wikipediaUrl = "https://es.wikipedia.org/wiki/Inteligencia_artificial";
        System.out.println("Testing web content extraction...");
        System.out.println("URL: " + wikipediaUrl);
        
        try {
            URL urlObj = new URL(wikipediaUrl);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Bot/1.0)");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("HTTP Response Code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                
                String html = content.toString();
                System.out.println("Downloaded: " + html.length() + " characters");
                
                // Extract title
                String title = "Sin título";
                int titleStart = html.indexOf("<title>");
                int titleEnd = html.indexOf("</title>");
                if (titleStart != -1 && titleEnd != -1) {
                    title = html.substring(titleStart + 7, titleEnd);
                    title = title.replaceAll(" - Wikipedia.*", "");
                }
                
                // Basic HTML cleaning
                String cleanText = html.replaceAll("<[^>]+>", " ");
                cleanText = cleanText.replaceAll("\s+", " ");
                cleanText = title + "\n\n" + cleanText.substring(0, Math.min(5000, cleanText.length()));
                
                // Save to file
                FileWriter writer = new FileWriter("web_extraction_result.txt");
                writer.write(cleanText);
                writer.close();
                
                System.out.println("SUCCESS: Extracted text saved to web_extraction_result.txt");
                System.out.println("Title: " + title);
                System.out.println("Preview: " + cleanText.substring(0, Math.min(200, cleanText.length())) + "...");
                
            } else {
                System.err.println("HTTP Error: " + responseCode);
            }
            
            connection.disconnect();
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
