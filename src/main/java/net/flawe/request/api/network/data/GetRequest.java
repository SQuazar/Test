package net.flawe.request.api.network.data;

import org.apache.http.client.utils.URIBuilder;

import java.util.HashMap;

public class GetRequest implements Request {

    private final HashMap<String, Object> map = new HashMap<>();

    public void add(String key, Object value) {
        this.map.put(key, value);
    }

    @Override
    public String getData() {
        URIBuilder builder = new URIBuilder();
        map.forEach((key, value) -> builder.setParameter(key, String.valueOf(value)));
        return builder.toString();
    }
}
