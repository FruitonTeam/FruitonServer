package cz.cuni.mff.fruiton.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class StorageUtils {

    private static final String APP_DIR = "Fruiton";

    private static final Path STORAGE_PATH = Paths.get(System.getProperty("user.home"), APP_DIR);

    private static final String IMAGE_PATH = Paths.get(STORAGE_PATH.toString(), "img").toString();

    private StorageUtils() {

    }

    public static File getImageRoot() {
        File imageRoot = new File(IMAGE_PATH);
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
