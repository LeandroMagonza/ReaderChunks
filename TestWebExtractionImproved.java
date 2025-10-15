import java.io.*;
import java.net.*;

public class TestWebExtractionImproved {
    public static void main(String[] args) {
        String wikipediaUrl = "https://es.wikipedia.org/wiki/Inteligencia_artificial";
        System.out.println("Testing IMPROVED web content extraction...");
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

                // Enhanced cleaning for Wikipedia
                String cleanText = cleanWikipediaContent(html);

                // Save to file
                FileWriter writer = new FileWriter("web_extraction_improved_result.txt");
                writer.write(cleanText);
                writer.close();

                System.out.println("SUCCESS: Extracted text saved to web_extraction_improved_result.txt");
                System.out.println("Final text length: " + cleanText.length() + " characters");
                System.out.println("Preview: " + cleanText.substring(0, Math.min(300, cleanText.length())) + "...");

            } else {
                System.err.println("HTTP Error: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String cleanWikipediaContent(String html) {
        System.out.println("Cleaning Wikipedia content...");

        // Extract title
        String title = extractTitle(html);

        // Extract main content from Wikipedia's specific structure
        String content = extractWikipediaMainContent(html);

        if (content == null || content.trim().isEmpty()) {
            // Fallback to basic cleaning
            content = basicClean(html);
        }

        // Combine title and content
        String result = title + "\n\n" + content;

        System.out.println("Cleaned content length: " + result.length());
        return result;
    }

    private static String extractTitle(String html) {
        int titleStart = html.indexOf("<title>");
        int titleEnd = html.indexOf("</title>");
        if (titleStart != -1 && titleEnd != -1) {
            String title = html.substring(titleStart + 7, titleEnd);
            title = title.replaceAll(" - Wikipedia.*", "");
            title = title.replaceAll(" — Wikipedia.*", "");
            return title.trim();
        }
        return "Contenido Web";
    }

    private static String extractWikipediaMainContent(String html) {
        // Look for the main Wikipedia content
        String content = null;

        // Try to find the main parser output
        int startIdx = html.indexOf("<div class=\"mw-parser-output\">");
        if (startIdx != -1) {
            // Find the corresponding closing div - this is tricky, let's use a simpler approach
            // Extract a reasonable chunk after the parser output starts
            int endIdx = html.indexOf("<div id=\"catlinks\"", startIdx);
            if (endIdx == -1) {
                endIdx = html.indexOf("<div class=\"navbox\"", startIdx);
            }
            if (endIdx == -1) {
                endIdx = Math.min(html.length(), startIdx + 50000); // Limit to 50k chars
            }

            content = html.substring(startIdx, endIdx);
        }

        if (content == null) {
            // Fallback: look for content between body tags
            int bodyStart = html.indexOf("<body");
            int bodyEnd = html.indexOf("</body>");
            if (bodyStart != -1 && bodyEnd != -1) {
                content = html.substring(bodyStart, bodyEnd);
            }
        }

        if (content != null) {
            content = cleanHtmlContent(content);
        }

        return content;
    }

    private static String cleanHtmlContent(String content) {
        if (content == null) return "";

        // Remove JavaScript and CSS
        content = content.replaceAll("(?i)<script[^>]*>.*?</script>", " ");
        content = content.replaceAll("(?i)<style[^>]*>.*?</style>", " ");
        content = content.replaceAll("(?i)<noscript[^>]*>.*?</noscript>", " ");

        // Remove Wikipedia-specific elements
        content = content.replaceAll("(?i)<div[^>]*class=\"[^\"]*navbox[^\"]*\"[^>]*>.*?</div>", " ");
        content = content.replaceAll("(?i)<div[^>]*class=\"[^\"]*infobox[^\"]*\"[^>]*>.*?</div>", " ");
        content = content.replaceAll("(?i)<div[^>]*class=\"[^\"]*hatnote[^\"]*\"[^>]*>.*?</div>", " ");
        content = content.replaceAll("(?i)<table[^>]*class=\"[^\"]*navbox[^\"]*\"[^>]*>.*?</table>", " ");

        // Remove reference links and edit links
        content = content.replaceAll("\\[edit\\]", "");
        content = content.replaceAll("\\[\\d+\\]", "");
        content = content.replaceAll("\\[citation needed\\]", "");

        // Remove all HTML tags
        content = content.replaceAll("<[^>]+>", " ");

        // Decode HTML entities
        content = content.replace("&amp;", "&");
        content = content.replace("&lt;", "<");
        content = content.replace("&gt;", ">");
        content = content.replace("&quot;", "\"");
        content = content.replace("&#39;", "'");
        content = content.replace("&nbsp;", " ");

        // Clean up whitespace
        content = content.replaceAll("\\s+", " ");
        content = content.trim();

        return content;
    }

    private static String basicClean(String html) {
        // Remove scripts and styles
        html = html.replaceAll("(?i)<(script|style)[^>]*>.*?</\\1>", " ");

        // Remove all HTML tags
        html = html.replaceAll("<[^>]+>", " ");

        // Clean whitespace
        html = html.replaceAll("\\s+", " ");

        return html.trim();
    }
}