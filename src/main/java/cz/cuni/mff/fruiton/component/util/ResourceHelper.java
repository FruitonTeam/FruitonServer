package cz.cuni.mff.fruiton.component.util;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public final class ResourceHelper {

    private static final Logger logger = Logger.getLogger(ResourceHelper.class.getName());

    private final ResourceLoader resourceLoader;

    @Autowired
    public ResourceHelper(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getBase64Image(final String path) {
        try {
            Resource r = resourceLoader.getResource("classpath:" + path);

            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(r.getInputStream()));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not get base64 image ", e);
        }
        return "";
    }

}
