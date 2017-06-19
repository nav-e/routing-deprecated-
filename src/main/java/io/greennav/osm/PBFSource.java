package io.greennav.osm;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import io.greennav.persistence.Persistence;
import io.greennav.persistence.PersistingOsmHandler;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PBFSource implements OSMSource {
    private PbfReader reader;

    public PBFSource(String file) throws FileNotFoundException {
        reader = new PbfReader(file, false);
    }

    @Override
    public void persistTo(Persistence persistence) throws IOException {
        PersistingOsmHandler handler = new PersistingOsmHandler(persistence);
        reader.setHandler(handler);

        try {
            reader.read();
        } catch (OsmInputException e) {
            throw new IOException("Could not read file: " + e.getMessage());
        }
    }
}
