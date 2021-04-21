package net.flawe.request;

import net.flawe.request.api.annotations.Entity;

@Entity
public class Message {

    @Entity.EntityKey(value = "first_name", path = "response")
    private String response;

    public String getResponse() {
        return response;
    }
}
