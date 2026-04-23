import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Fixer {
    public static void main(String[] args) throws Exception {
        Files.walk(Paths.get("src/main/java"))
             .filter(Files::isRegularFile)
             .filter(p -> p.toString().endsWith(".java"))
             .forEach(p -> {
                 try {
                     String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                     if (content.contains("SY.symbioticlaw")) {
                         content = content.replace("SY.symbioticlaw", "com.symbioticlaw");
                         Files.write(p, content.getBytes(StandardCharsets.UTF_8));
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             });
        
        System.out.println("Replacement done.");
    }
}
