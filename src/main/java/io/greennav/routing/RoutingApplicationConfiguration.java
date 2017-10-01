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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class RoutingApplicationConfiguration {
    @Bean
    CommandLineRunner initialize() {
        return args -> {
            BasicConfigurator.configure();
            final File pbfPath = new File("data/");
            Arrays.stream(pbfPath.listFiles())
                    .filter(f -> f.getName().endsWith("pbf"))
                    .forEach(f -> {
                        final OSMSource src;
                        try {
                            src = new PBFSource(f.getAbsolutePath());
                            final Persistence db = getPersistence();
                            src.persistTo(db);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        };
    }

    @Bean
    @Scope("application")
    Persistence getPersistence() {
        return new InMemoryPersistence();
    }
}
