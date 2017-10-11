package io.greennav.osm;

import crosby.binary.osmosis.OsmosisReader;
import io.greennav.persistence.Persistence;
import io.greennav.persistence.PersistingOsmHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PBFSource implements OSMSource {
    private String pbfPath;

    public PBFSource(String file) throws FileNotFoundException {
        this.pbfPath = file;
    }

    @Override
    public void persistTo(Persistence persistence) throws IOException {
        OsmosisReader reader = new OsmosisReader(new FileInputStream(pbfPath));

        reader.setSink(new PersistingOsmHandler(persistence));
        reader.run();
    }
}
