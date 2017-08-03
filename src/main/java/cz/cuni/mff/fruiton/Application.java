package cz.cuni.mff.fruiton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "cz.cuni.mff.fruiton.controller",
        "cz.cuni.mff.fruiton.config",
        "cz.cuni.mff.fruiton.service",
        "cz.cuni.mff.fruiton.component"
})
@EntityScan(basePackages = "cz.cuni.mff.arrows.dao")
@EnableScheduling
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
