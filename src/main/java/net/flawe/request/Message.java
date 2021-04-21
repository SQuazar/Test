package net.flawe.request;

import net.flawe.request.api.annotations.Entity;

@Entity
public class Message {

    @Entity.EntityKey(value = "", path = "response.items.[1]")
    private String response;

    public String getResponse() {
        return response;
    }
}
