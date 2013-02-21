package ardust.shared;

import java.io.*;
import java.net.URL;

public class Loader {
    private static final boolean DEVELOPER = true;

    public static InputStream getRequiredResourceAsStream(String ref) {
        try {
            if (DEVELOPER) {
                File file = new File(ref);
                if (file.exists()) {
                    System.err.println("Resource loaded from local file: " + file.toURI());
                    return new BufferedInputStream(new FileInputStream(file));
                }
            }
            URL url = Loader.class.getClassLoader().getResource(ref);
            return new BufferedInputStream(url.openStream());
        } catch (IOException e) {
            System.err.println("Resource loading failed: " + ref);
            throw new RuntimeException(e);
        }
    }
}
