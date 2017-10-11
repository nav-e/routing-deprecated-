package io.greennav.osm;

import io.greennav.persistence.Persistence;

import java.io.IOException;

public interface OSMSource {
    void persistTo(Persistence persistence) throws IOException;
}
