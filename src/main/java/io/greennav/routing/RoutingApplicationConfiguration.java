package io.greennav.routing;

import io.greennav.osm.OSMSource;
import io.greennav.osm.PBFSource;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RoutingApplicationConfiguration {
    @Bean
    CommandLineRunner initialize() {
        return args -> {
            BasicConfigurator.configure();
            try {
                final OSMSource src = new PBFSource("data/monaco-latest.osm.pbf");
                final Persistence db = getPersistence();
                src.persistTo(db);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
    }

    @Bean
    @Scope("application")
    Persistence getPersistence() {
        return new InMemoryPersistence();
    }
}
