package top.pigest.queuemanagerdemo.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.system.WbiSign;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HttpPostBuilder {
    private final URIBuilder uriBuilder;
    private final List<NameValuePair> urlParams;
    private final List<NameValuePair> formDataParams;

    HttpPostBuilder(String uri) {
        try {
            this.uriBuilder = new URIBuilder(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.urlParams = new ArrayList<>();
        this.formDataParams = new ArrayList<>();
    }

    public HttpPostBuilder appendUrlParameter(String key, Object value) {
        urlParams.add(new BasicNameValuePair(key, String.valueOf(value)));
        return this;
    }

    public HttpPostBuilder appendFormDataParameter(String key, Object value) {
        formDataParams.add(new BasicNameValuePair(key, String.valueOf(value)));
        return this;
    }

    public HttpPost build() {
        try {
            HttpPost httpPost = new HttpPost(uriBuilder.addParameters(urlParams).build());
            if (!formDataParams.isEmpty()) {
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                httpPost.setEntity(new UrlEncodedFormEntity(formDataParams));
            }
            httpPost.setConfig(Settings.DEFAULT_REQUEST_CONFIG);
            return httpPost;
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpPost buildWithWbiSign() {
        try {
            HttpPost httpPost = new HttpPost(WbiSign.getSignedUri(uriBuilder, urlParams));
            if (!formDataParams.isEmpty()) {
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                httpPost.setEntity(new UrlEncodedFormEntity(formDataParams));
            }
            httpPost.setConfig(Settings.DEFAULT_REQUEST_CONFIG);
            return httpPost;
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
