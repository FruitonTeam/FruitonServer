package cz.cuni.mff.fruiton.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ResourceUtils {

    private static final Logger logger = Logger.getLogger(ResourceUtils.class.getName());

    private ResourceUtils() {
    }

    public static String getBase64Image(final String path) {
        try {
            return Base64.getEncoder().encodeToString(
                    IOUtils.toByteArray(ResourceUtils.class.getClassLoader().getResourceAsStream(path)));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not get base64 image ", e);
        }
        return "";
    }

}
