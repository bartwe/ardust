package ardust.shared;

import java.io.*;
import java.net.URL;

public class Loader {
    public static InputStream getRequiredResourceAsStream(String ref) {
        try {
            if (Constants.DEVELOPER) {
                File file = new File(ref);
                if (file.exists()) {
                    System.err.println("Resource loaded from local file: " + file.toURI());
                    return new BufferedInputStream(new FileInputStream(file));
                }
            }
            URL url = Loader.class.getClassLoader().getResource(ref);
            if (url == null)
                throw new RuntimeException("No such resouce embedded: " + ref);
            return new BufferedInputStream(url.openStream());
        } catch (IOException e) {
            System.err.println("Resource loading failed: " + ref);
            throw new RuntimeException(e);
        }
    }
}
