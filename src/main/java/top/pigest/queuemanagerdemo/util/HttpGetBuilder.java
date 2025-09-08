package top.pigest.queuemanagerdemo.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.system.WbiSign;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HttpGetBuilder {
    private final URIBuilder uriBuilder;
    private final List<NameValuePair> params;

    HttpGetBuilder(String uri) {
        try {
            this.uriBuilder = new URIBuilder(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.params = new ArrayList<>();
    }

    public HttpGetBuilder appendUrlParameter(String key, Object value) {
        params.add(new BasicNameValuePair(key, String.valueOf(value)));
        return this;
    }

    public HttpGet build() {
        try {
            HttpGet httpGet = new HttpGet(uriBuilder.addParameters(params).build());
            httpGet.setConfig(Settings.DEFAULT_REQUEST_CONFIG);
            return httpGet;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpGet buildWithWbiSign() {
        try {
            HttpGet httpGet = new HttpGet(WbiSign.getSignedUri(uriBuilder, params));
            httpGet.setConfig(Settings.DEFAULT_REQUEST_CONFIG);
            return httpGet;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
