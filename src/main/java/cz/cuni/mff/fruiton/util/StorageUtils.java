package cz.cuni.mff.fruiton.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class StorageUtils {

    private static final String APP_DIR = "Fruiton";

    private static final Path USER_HOME_PATH = Paths.get(System.getProperty("user.home"));

    /** Tomcat specific path. Needed because 'user.home' is not writable if deployed in tomcat. */
    private static final Path CATALINA_HOME_PATH = Paths.get(System.getProperty("catalina.home"));

    private static final String IMG_DIR = "img";

    private StorageUtils() {

    }

    private static Path getStoragePath() {
        if (Files.isWritable(USER_HOME_PATH)) {
            return Paths.get(USER_HOME_PATH.toString(), APP_DIR);
        } else if (Files.isWritable(CATALINA_HOME_PATH)) {
            return Paths.get(CATALINA_HOME_PATH.toString(), APP_DIR);
        } else {
            throw new IllegalStateException("Cannot get any writable storage");
        }
    }

    public static File getImageRoot() {
        File imageRoot = new File(getStoragePath().toString(), IMG_DIR);
        if (!imageRoot.exists()) {
            boolean success = imageRoot.mkdirs();
            if (!success) {
                throw new IllegalStateException("Could not make image path directories");
            }
        } else if (!imageRoot.isDirectory()) {
            throw new IllegalStateException("Image root exists but is not a directory");
        }

        return imageRoot;
    }

}
