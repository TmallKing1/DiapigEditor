package top.pigest.queuemanagerdemo.music.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.resource.RequireCleaning;
import top.pigest.queuemanagerdemo.util.RequestUtils;
import top.pigest.queuemanagerdemo.util.Utils;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.Date;
import java.util.Optional;

public class MusicPreloadPage extends StackPane implements RequireCleaning {
    private WebEngine webEngine;
    private WebView webView;
    private Timeline preloadTimeline;

    public MusicPreloadPage(MusicSystemPage msp) {
        StackPane pane = new StackPane();
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        webView = new WebView();
        webView.setMaxSize(0, 0);
        webView.setContextMenuEnabled(false);
        webEngine = webView.getEngine();
        webEngine.setUserAgent(Settings.USER_AGENT);
        webEngine.load("https://music.163.com/#/login");
        this.preloadTimeline = new Timeline(new KeyFrame(new Duration(500), event -> {
            Optional<HttpCookie> sDeviceId = cookieManager.getCookieStore().getCookies().stream().filter(cookie -> cookie.getName().equalsIgnoreCase("sDeviceId")).findFirst();
            if (sDeviceId.isPresent()) {
                CookieStore cookieStore = RequestUtils.getCookieStore();
                HttpCookie httpCookie = sDeviceId.get();
                BasicClientCookie cookie = new BasicClientCookie(httpCookie.getName(), httpCookie.getValue());
                cookie.setPath(httpCookie.getPath());
                cookie.setDomain(httpCookie.getDomain());
                cookie.setExpiryDate(new Date(System.currentTimeMillis() + httpCookie.getMaxAge() * 1000));
                cookieStore.addCookie(cookie);
                RequestUtils.saveCookie(false);
                msp.preloaded = true;
                Platform.runLater(() -> QueueManager.INSTANCE.getMainScene().setMainContainer(new MusicSystemPage().withParentPage(msp.getParentPage()), this.getId()));
                this.preloadTimeline.stop();
            }
        }));
        this.preloadTimeline.setCycleCount(Timeline.INDEFINITE);
        this.preloadTimeline.play();
        Label label = new Label("正在初始化\n请稍等几秒钟");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.setFont(Settings.DEFAULT_FONT);
        pane.getChildren().addAll(webView, label);
        pane.setId("c0");
    }

    @Override
    public void clean() {
        Utils.onPresent(webView.getParent(), parent -> ((Pane) parent).getChildren().remove(webView));
        Utils.onPresent(webEngine, webView1 -> webEngine.load(null));
        webView = null;
        webEngine = null;
    }
}
