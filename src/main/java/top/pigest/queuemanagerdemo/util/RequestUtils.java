package top.pigest.queuemanagerdemo.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import top.pigest.queuemanagerdemo.Settings;

import java.io.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class RequestUtils {
    public static final RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).setCookieSpec(CookieSpecs.STANDARD).build();
    private static final File COOKIE_STORE_FILE = Settings.DATA_DIRECTORY.toPath().resolve("cookies.ser").toFile();
    public static CookieStore COOKIE_STORE = new BasicCookieStore();

    public static JsonObject requestToJson(HttpUriRequest httpRequest) {
        try (CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(getCookieStore()).build()) {
            Settings.checkAndRefresh();
            httpRequest.setHeader("User-Agent", Settings.USER_AGENT);
            HttpResponse response = client.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + statusCode);
            }
            return JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String requestToString(HttpUriRequest httpRequest) {
        try (CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(getCookieStore()).build()) {
            Settings.checkAndRefresh();
            httpRequest.setHeader("User-Agent", Settings.USER_AGENT);
            HttpResponse response = client.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + statusCode);
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse request(HttpUriRequest httpRequest) {
        try (CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(getCookieStore()).build()) {
            Settings.checkAndRefresh();
            httpRequest.setHeader("User-Agent", Settings.USER_AGENT);
            HttpResponse response = client.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + statusCode);
            }
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<JsonObject> requestToJsonAsync(HttpUriRequest httpRequest) {
        return CompletableFuture.supplyAsync(() -> requestToJson(httpRequest));
    }

    public static CompletableFuture<String> requestToStringAsync(HttpUriRequest httpRequest) {
        return CompletableFuture.supplyAsync(() -> requestToString(httpRequest));
    }

    public static CompletableFuture<HttpResponse> requestAsync(HttpUriRequest httpRequest) {
        return CompletableFuture.supplyAsync(() -> request(httpRequest));
    }

    public static HttpGetBuilder httpGet(String uri) {
        return new HttpGetBuilder(uri);
    }

    public static HttpPostBuilder httpPost(String uri) {
        return new HttpPostBuilder(uri);
    }

    public static CookieStore getCookieStore() {
        return COOKIE_STORE;
    }

    public static void loadCookie() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(COOKIE_STORE_FILE))) {
            COOKIE_STORE = (CookieStore) ois.readObject();
        } catch (Exception e) {
            COOKIE_STORE = new BasicCookieStore();
        }
    }

    public static void saveCookie(boolean updateRefreshTime) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(COOKIE_STORE_FILE))) {
            out.writeObject(COOKIE_STORE);
            if (updateRefreshTime) {
                Settings.setLastRefreshTime(System.currentTimeMillis());
            }
        } catch (Exception e) {
            System.err.println("保存 Cookie 失败");
            System.err.println(e.getMessage());
        }
    }

    public static String getCookie(String name) {
        return getCookieStore().getCookies().stream().filter(cookie -> cookie.getName().equalsIgnoreCase(name)
                                                                       && !cookie.getValue().isEmpty()
                                                                       && !cookie.isExpired(new Date(System.currentTimeMillis())))
                .findFirst().orElseThrow().getValue();
    }

    public static boolean hasCookie(String name) {
        return getCookieStore().getCookies().stream().anyMatch(cookie -> cookie.getName().equalsIgnoreCase(name)
                                                                         && !cookie.getValue().isEmpty()
                                                                         && !cookie.isExpired(new Date(System.currentTimeMillis())));
    }
}
