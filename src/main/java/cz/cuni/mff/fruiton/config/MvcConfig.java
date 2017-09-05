package cz.cuni.mff.fruiton.config;

import cz.cuni.mff.fruiton.util.StorageUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@EnableWebMvc
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public final void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // add mapping to static resources and image directory
        registry.addResourceHandler("/img/**")
                .addResourceLocations(StorageUtils.getImageRoot().toURI().toString(), "classpath:/static/img/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
    }

    @Override
    public final void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(new ProtobufHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());

        super.extendMessageConverters(converters);
    }
}
