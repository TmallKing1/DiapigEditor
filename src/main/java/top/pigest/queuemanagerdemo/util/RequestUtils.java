package top.pigest.queuemanagerdemo.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import top.pigest.queuemanagerdemo.Settings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class RequestUtils {
    private static final CloseableHttpClient CLIENT = HttpClients.custom().setDefaultCookieStore(Settings.getCookieStore()).build();

    public static JsonElement request(HttpUriRequest httpRequest) {
        try {
            HttpResponse response = CLIENT.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + statusCode);
            }
            return JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String requestToString(HttpUriRequest httpRequest) {
        try {
            HttpResponse response = CLIENT.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + statusCode);
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<JsonElement> requestAsync(HttpUriRequest httpRequest) {
        return CompletableFuture.supplyAsync(() -> request(httpRequest));
    }

    public static HttpGetBuilder httpGet(String uri) {
        return new HttpGetBuilder(uri);
    }

    public static HttpPostBuilder httpPost(String uri) {
        return new HttpPostBuilder(uri);
    }
}
