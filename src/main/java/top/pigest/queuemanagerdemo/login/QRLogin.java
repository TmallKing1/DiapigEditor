package top.pigest.queuemanagerdemo.login;

import com.google.gson.JsonObject;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.http.client.methods.HttpGet;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.util.RequestUtils;
import top.pigest.queuemanagerdemo.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class QRLogin extends BorderPane implements LoginMethodLocker {
    private final LoginMain loginMain;
    private final StackPane stackPane = new StackPane();
    private final ImageView imageView = new ImageView();
    private final VBox qrCodeHintBox = Utils.make(new VBox(), vBox -> {
        vBox.setAlignment(Pos.CENTER);
        vBox.setPrefSize(270, 270);
        vBox.setMaxSize(270, 270);
        vBox.setStyle("-fx-background-color: #ffffffdd;");
    });
    private final Label qrCodeHint = Utils.make(Utils.createLabel(""), label -> {
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
    });
    private final Label hint = Utils.make(Utils.createLabel("请使用哔哩哔哩移动端扫描二维码"), label -> {
        BorderPane.setMargin(label, new Insets(0, 0, 30, 0));
        BorderPane.setAlignment(label, Pos.CENTER);
    });
    private final QMButton reset = Utils.make(new QMButton("重新生成", QMButton.DEFAULT_COLOR), button -> {
        button.setPrefWidth(200);
        button.setOnAction(event -> refreshQRCode());
        button.setGraphic(new WhiteFontIcon("fas-undo"));
        BorderPane.setMargin(button, new Insets(0, 0, 25, 0));
        BorderPane.setAlignment(button, Pos.CENTER);
    });
    private boolean lockLoginMethod = false;
    private String qrcodeKey;
    private Timeline timeline;

    QRLogin(LoginMain loginMain) {
        super();
        this.loginMain = loginMain;
        this.stackPane.setPrefSize(270, 270);
        new Thread(this::refreshQRCode).start();
        this.setTop(stackPane);
        BorderPane.setMargin(stackPane, new Insets(15, 0, 0, 0));
    }

    private void refreshQRCode() {
        Platform.runLater(() -> this.stackPane.getChildren().clear());
        this.lockLoginMethod = false;
        String s = getLoginURL();
        if (s == null) {
            return;
        }
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        Platform.runLater(() -> {
            try {
                Utils.stringToQRCode(s, writer, hints, this.imageView);
                this.stackPane.getChildren().add(imageView);
                this.setBottom(hint);
                this.timeline = createTimeline();
                this.timeline.play();
            } catch (WriterException | IOException e) {
                this.loginMain.loginFail("二维码生成失败", true);
                this.setBottom(reset);
            }
        });
    }

    private String getLoginURL() {
        HttpGet httpGet = new HttpGet("https://passport.bilibili.com/x/passport-login/web/qrcode/generate");
        httpGet.setConfig(RequestUtils.DEFAULT_REQUEST_CONFIG);
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpGet("https://passport.bilibili.com/x/passport-login/web/qrcode/generate").build());
        if (object.get("code").getAsInt() == 0) {
            qrcodeKey = object.getAsJsonObject("data").get("qrcode_key").getAsString();
            return object.getAsJsonObject("data").get("url").getAsString();
        } else {
            Platform.runLater(() -> this.loginMain.loginFail("二维码获取失败", true));
            return null;
        }
    }

    private Timeline createTimeline() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(500), event -> {
                    JsonObject object = RequestUtils.requestToJson(RequestUtils.httpGet("https://passport.bilibili.com/x/passport-login/web/qrcode/poll")
                            .appendUrlParameter("qrcode_key", qrcodeKey)
                            .build());
                    if (object.get("code").getAsInt() == 0) {
                        int code = object.getAsJsonObject("data").get("code").getAsInt();
                        int status = updateCodeStatus(code);
                        switch (status) {
                            case 0 -> loginSuccess(object.getAsJsonObject("data").get("refresh_token").getAsString());
                            case 2 -> loginFail();
                        }
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    public void loginSuccess(String refreshToken) {
        stopTimeline();
        Settings.setRefreshToken(refreshToken);
        RequestUtils.saveCookie(true);
        Platform.runLater(() -> ((LoginMain) this.getScene()).loginSuccess());
    }

    protected void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void loginFail() {
        stopTimeline();
    }

    private int updateCodeStatus(int statusCode) {
        switch (statusCode) {
            case 0 -> {
                return 0;
            }
            case 86038 -> {
                Platform.runLater(() -> {
                    this.qrCodeHintBox.getChildren().clear();
                    this.qrCodeHint.setText("二维码已失效");
                    this.qrCodeHintBox.getChildren().add(qrCodeHint);
                    this.stackPane.getChildren().add(this.qrCodeHintBox);
                    this.setBottom(this.reset);
                });
                return 2;
            }
            case 86090 -> {
                Platform.runLater(() -> {
                    if (!this.lockLoginMethod) {
                        this.qrCodeHintBox.getChildren().clear();
                        this.qrCodeHint.setText("扫码成功\n请在手机上点击【确认】");
                        FontIcon icon = new FontIcon("far-check-circle:50");
                        icon.setIconColor(Paint.valueOf("#379437"));
                        VBox.setMargin(icon, new Insets(0, 0, 30, 0));
                        this.qrCodeHintBox.getChildren().add(icon);
                        this.qrCodeHintBox.getChildren().add(this.qrCodeHint);
                        this.stackPane.getChildren().add(this.qrCodeHintBox);
                        this.setBottom(null);
                        this.lockLoginMethod = true;
                    }
                });
                return 1;
            }
            case 86101 -> {
                return 1;
            }
        }
        return 1;
    }

    @Override
    public boolean lockLoginMethod() {
        return lockLoginMethod;
    }
}
