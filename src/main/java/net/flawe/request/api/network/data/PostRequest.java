package net.flawe.request.api.network.data;

import com.google.gson.Gson;

import java.util.HashMap;

public class PostRequest implements Request {

    private final HashMap<String, Object> map = new HashMap<>();

    public void add(String key, Object value) {
        this.map.put(key, value);
    }

    @Override
    public String getData() {
        return new Gson().toJson(map);
    }
}
