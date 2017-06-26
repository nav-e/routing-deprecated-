package io.greennav.routing;

import com.google.gson.Gson;
import spark.ResponseTransformer;

public class JSONTransformer implements ResponseTransformer {
    private final Gson gson = new Gson();

    @Override
    public String render(Object model) throws Exception {
        return gson.toJson(model);
    }
}
