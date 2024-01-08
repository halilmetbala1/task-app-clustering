package at.jku.dke.task_app.binary_search.config;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The application web configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * Creates a new instance of class {@link WebConfig}.
     */
    public WebConfig() {
    }

    /**
     * Provides the http trace repository.
     *
     * @return The http trace repository.
     */
    @Bean
    public HttpExchangeRepository httpTraceRepository() {
        var repo = new InMemoryHttpExchangeRepository();
        repo.setCapacity(500);
        return repo;
    }
}
