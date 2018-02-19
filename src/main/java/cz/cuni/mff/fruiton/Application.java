package cz.cuni.mff.fruiton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "cz.cuni.mff.fruiton.controller",
        "cz.cuni.mff.fruiton.config",
        "cz.cuni.mff.fruiton.dao",
        "cz.cuni.mff.fruiton.service",
        "cz.cuni.mff.fruiton.component"
})
@EntityScan(basePackages = "cz.cuni.mff.fruiton.dao")
@EnableScheduling
@EnableAsync
public class Application extends SpringBootServletInitializer {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Used when run as WAR.
     */
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

}
