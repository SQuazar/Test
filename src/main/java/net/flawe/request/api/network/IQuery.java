package net.flawe.request.api.network;

public interface IQuery {
    String getUrl();

    Method getMethod();

    enum Method {
        POST, GET
    }
}
