package top.pigest.dialogeditor.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import static top.pigest.dialogeditor.Settings.USER_AGENT;

public class RequestUtils {
    public static final RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).setCookieSpec(CookieSpecs.STANDARD).build();

    private static JsonObject errorObject(String message) {
        JsonObject errorObject = new JsonObject();
        errorObject.addProperty("code", -114514);
        errorObject.addProperty("message", message);
        return errorObject;
    }

    public static JsonObject requestToJson(HttpUriRequest httpRequest) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            httpRequest.setHeader("User-Agent", USER_AGENT);
            HttpResponse response = client.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                return errorObject("Failed : HTTP error code : " + statusCode);
            }
            return JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();
        } catch (IOException e) {
            return errorObject(e.getMessage());
        }
    }

    public static String requestToString(HttpUriRequest httpRequest) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            httpRequest.setHeader("User-Agent", USER_AGENT);
            HttpResponse response = client.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                return "Failed : HTTP error code : " + statusCode;
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public static CompletableFuture<JsonObject> requestToJsonAsync(HttpUriRequest httpRequest) {
        return CompletableFuture.supplyAsync(() -> requestToJson(httpRequest));
    }

    public static CompletableFuture<String> requestToStringAsync(HttpUriRequest httpRequest) {
        return CompletableFuture.supplyAsync(() -> requestToString(httpRequest));
    }

    public static HttpGetBuilder httpGet(String uri) {
        return new HttpGetBuilder(uri);
    }

    public static HttpPostBuilder httpPost(String uri) {
        return new HttpPostBuilder(uri);
    }

}
