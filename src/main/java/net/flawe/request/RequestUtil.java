package net.flawe.request;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.flawe.request.api.Callback;
import net.flawe.request.api.annotations.Entity;
import net.flawe.request.api.network.IQuery;
import net.flawe.request.api.network.data.GetRequest;
import net.flawe.request.api.network.data.PostRequest;
import net.flawe.request.api.network.data.Request;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class RequestUtil {

    public static void main(String[] args) {
        IQuery query = new IQuery() {
            @Override
            public String getUrl() {
                return "https://api.vk.com/method/friends.get";
            }

            @Override
            public Method getMethod() {
                return Method.GET;
            }
        };
        GetRequest request = new GetRequest()
        {{
            add("user_id", 221249563);
//            add("fields","first_name");
            add("access_token", "");
            add("v", 5.52);
        }};
        executeQuery(query, request, new Header[]{}, new Callback<Message>() {
            @Override
            public void onResult(Message result, String response, int code) {
                if (result == null) {
                    System.out.println("Result is null");
                    return;
                }
                System.out.println(result.getResponse());
            }
        });
    }

    public static <T> void executeQuery(IQuery query, Request request, Header[] headers, Callback<T> callback) {
        if (query.getMethod() == IQuery.Method.POST && !(request instanceof PostRequest))
            return;
        if (query.getMethod() == IQuery.Method.GET && !(request instanceof GetRequest))
            return;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = null;
            switch (query.getMethod()) {
                case POST: {
                    HttpPost post = new HttpPost(query.getUrl());
                    post.setHeaders(headers);
                    post.setEntity(new StringEntity(request.getData()));
                    response = client.execute(post);
                }
                break;
                case GET: {
                    HttpGet get = new HttpGet(query.getUrl() + request.getData());
                    get.setHeaders(headers);
                    response = client.execute(get);
                }
                break;
            }
            if (response == null)
                return;
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null)
                try (InputStream stream = httpEntity.getContent()) {
                    String result = IOUtils.toString(stream);
                    JsonElement element = JsonParser.parseString(result);
                    if (!element.isJsonObject())
                        return;
                    Class<?> generic = (Class<?>) ((ParameterizedType) callback.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                    if (generic.isAnnotationPresent(Entity.class)) {
                        try {
                            Object instance = generic.newInstance();
                            for (Field field : generic.getDeclaredFields()) {
                                if (!field.isAccessible())
                                    field.setAccessible(true);
                                String key = field.getName();
                                if (field.isAnnotationPresent(Entity.EntityKey.class))
                                    key = field.getAnnotation(Entity.EntityKey.class).value();
                                JsonElement elem = find(field.getAnnotation(Entity.EntityKey.class).path(), element.getAsJsonObject());
                                Object obj = new Gson().fromJson(key.isEmpty() ? elem : elem.getAsJsonObject().get(key), field.getType());
                                field.set(instance, obj);
                            }
                            callback.onResult((T) instance, response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (generic.isAssignableFrom(String.class))
                            callback.onResult((T) result, response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
                        else
                            callback.onResult(null, response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonElement find(String path, JsonObject object) {
        String[] arr = path.split("\\.");
        JsonElement obj = object;
        for (String s : arr) {
            if (obj.isJsonArray()) {
                if (s.matches("\\[(\\d+)]")) {
                    String sIndex = s.replaceAll("[\\[\\]]", "");
                    int index = Integer.parseInt(sIndex);
                    obj = obj.getAsJsonArray().get(index);
                    continue;
                }
            }
            if (obj.isJsonObject())
                obj = obj.getAsJsonObject().get(s);
        }
        return obj;
    }

}
