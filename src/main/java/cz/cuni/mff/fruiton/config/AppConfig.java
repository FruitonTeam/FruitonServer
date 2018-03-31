package cz.cuni.mff.fruiton.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class AppConfig {

    private static final int TASK_SCHEDULER_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(TASK_SCHEDULER_POOL_SIZE);
        taskScheduler.initialize();
        return taskScheduler;
    }

}
