package io.greennav.routing;

import io.greennav.osm.OSMSource;
import io.greennav.osm.PBFSource;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoutingApplicationMockup {
    public static void main(String[] args) {
        BasicConfigurator.configure();

        try {
            Persistence db = new InMemoryPersistence();
            OSMSource src = new PBFSource("res/monaco-latest.osm.pbf");

            src.persistTo(db);

            RestAPI api = new RestAPI();
            api.createServer(db);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
