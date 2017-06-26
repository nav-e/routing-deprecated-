package io.greennav.routing;

import io.greennav.osm.OSMSource;
import io.greennav.osm.PBFSource;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoutingApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutingApplication.class, args);
		BasicConfigurator.configure();

		try {
			final Persistence db = new InMemoryPersistence();
			final OSMSource src = new PBFSource("data/monaco-latest.osm.pbf");
			src.persistTo(db);
			final RestAPI api = new RestAPI();
			api.createServer(db);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
