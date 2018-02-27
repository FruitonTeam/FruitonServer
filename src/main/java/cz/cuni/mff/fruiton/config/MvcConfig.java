package cz.cuni.mff.fruiton.config;

import cz.cuni.mff.fruiton.component.util.ReleasesHelper;
import cz.cuni.mff.fruiton.util.StorageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableWebMvc
public class MvcConfig extends WebMvcConfigurerAdapter {

    private static final Logger logger = Logger.getLogger(MvcConfig.class.getName());

    private ReleasesHelper releasesHelper;

    @Autowired
    public MvcConfig(final ReleasesHelper releasesHelper) {
        this.releasesHelper = releasesHelper;
    }

    @Override
    public final void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // add mapping to static resources, avatar and releases directory
        registry.addResourceHandler("/**")
                .addResourceLocations(new ResourceProperties().getStaticLocations());
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations(StorageUtils.getImageRoot().toURI().toString());
        if (releasesHelper.isReleasesPathCorrect()) {
            registry.addResourceHandler("/releases/**")
                    .addResourceLocations(releasesHelper.getReleasesPath());
        } else {
            logger.log(Level.INFO, "Releases directory {0} is not correct, ignoring", releasesHelper.getReleasesPath());
        }
    }

    @Override
    public final void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(new ProtobufHttpMessageConverter());
    }

}
