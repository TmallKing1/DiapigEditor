package top.pigest.queuemanagerdemo.window.login;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.TitledDialog;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.util.RequestUtils;
import top.pigest.queuemanagerdemo.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SMSLogin extends VBox implements CaptchaLogin, LoginMethodLocker {
    private final boolean fromPassword;
    private final LoginMain loginMain;
    private final List<Country> countries = new ArrayList<>();
    private final QMButton countryButton = Utils.make(new QMButton("", "#1f1e33"), button -> {
        button.setPrefWidth(100);
        button.setOnAction(actionEvent -> selectCountry());
    });
    private final QMButton smsCodeButton = Utils.make(new QMButton("获取验证码", "#1f1e33"), button -> {
        button.setPrefWidth(140);
        button.setOnAction(actionEvent -> startCaptcha());
    });
    private final QMButton loginButton = Utils.make(new QMButton("登录", QMButton.DEFAULT_COLOR), button -> {
        button.disable(true);
        button.setPrefWidth(200);
        button.setOnAction(actionEvent -> login());
        button.setDefaultButton(true);
        button.setGraphic(new WhiteFontIcon("fas-sign-in-alt"));
    });
    private final JFXTextField accountField;
    private final JFXTextField passwordField = Utils.make(new JFXTextField(), textField -> {
        textField.setLabelFloat(true);
        textField.setFocusColor(Paint.valueOf("#1a8bcc"));
        textField.setPrefWidth(240);
        textField.setPromptText("验证码");
        textField.setFont(Settings.DEFAULT_FONT);
    });
    private int selectedCountry = 1;
    private String selectedCountryCid = "86";
    private int countdown = 0;

    private String captchaKey;

    private String tmpCode;
    private String requestId;

    private Stage captcha;

    private SMSLogin(boolean fromPassword, LoginMain loginMain) {
        super(30);
        this.fromPassword = fromPassword;
        this.loginMain = loginMain;
        this.setAlignment(Pos.CENTER);
        if (fromPassword) {
            this.accountField = Utils.make(new JFXTextField(), textField -> textField.setPrefWidth(400));
            this.accountField.setDisable(true);
        } else {
            this.accountField = Utils.make(new JFXTextField(), textField -> textField.setPrefWidth(280));
            new Thread(this::initCountries).start();
        }
        this.accountField.setLabelFloat(true);
        this.accountField.setFocusColor(Paint.valueOf("#1a8bcc"));
        this.accountField.setPromptText("手机号");
        this.accountField.setFont(Settings.DEFAULT_FONT);
        this.accountField.textProperty().addListener((observable, oldValue, newValue) -> loginButton.disable((accountField.getText().isEmpty() && !this.fromPassword) || passwordField.getText().isEmpty()));
        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> loginButton.disable((accountField.getText().isEmpty() && !this.fromPassword) || passwordField.getText().isEmpty()));
        this.accountField.setTextFormatter(new TextFormatter<>(change -> {
            if (this.fromPassword || change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        this.passwordField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        this.getChildren().add(Utils.make(new HBox(20), hBox -> {
            hBox.setAlignment(Pos.CENTER);
            if (!this.fromPassword) {
                hBox.getChildren().add(this.countryButton);
            }
            hBox.getChildren().add(accountField);
        }));
        this.getChildren().add(Utils.make(new HBox(20), hBox -> {
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().add(passwordField);
            hBox.getChildren().add(smsCodeButton);
        }));
        this.getChildren().add(loginButton);
    }

    private void login() {
        if (captchaKey == null) {
            this.loginMain.showDialogMessage("请先获取短信验证码");
            return;
        }

        this.accountField.setDisable(true);
        this.passwordField.setDisable(true);
        this.loginButton.disable(true);
        this.loginMain.lockLoginMethodButtons(true);

        this.loginButton.setText("登录中");
        this.loginButton.setGraphic(new WhiteFontIcon("fas-bullseye"));

        CompletableFuture.runAsync(() -> {
            if (this.fromPassword) {
                JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://passport.bilibili.com/x/safecenter/login/tel/verify")
                        .appendFormDataParameter("tmp_code", tmpCode)
                        .appendFormDataParameter("captcha_key", captchaKey)
                        .appendFormDataParameter("type", "loginTelCheck")
                        .appendFormDataParameter("code", this.passwordField.getText())
                        .appendFormDataParameter("request_id", requestId)
                        .appendFormDataParameter("source", "risk")
                        .build());
                if (object.get("code").getAsInt() == 0) {
                    String code = object.getAsJsonObject("data").get("code").getAsString();
                    JsonObject obj1 = RequestUtils.requestToJson(RequestUtils.httpPost("https://passport.bilibili.com/x/passport-login/web/exchange_cookie")
                            .appendFormDataParameter("source", "risk")
                            .appendFormDataParameter("code", code)
                            .build());
                    if (obj1.get("code").getAsInt() == 0) {
                        RequestUtils.saveCookie(true);
                        Settings.setRefreshToken(obj1.getAsJsonObject("data").get("refresh_token").getAsString());
                        Platform.runLater(() -> ((LoginMain) this.getScene()).loginSuccess());
                    } else {
                        this.loginFail("交换 Cookie 失败", true);
                    }
                } else {
                    this.loginFail("登录失败" + "(%s)".formatted(object.get("code").getAsInt()), true);
                }
            } else {
                JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://passport.bilibili.com/x/passport-login/web/login/sms")
                        .appendFormDataParameter("cid", this.selectedCountryCid)
                        .appendFormDataParameter("tel", this.accountField.getText())
                        .appendFormDataParameter("code", this.passwordField.getText())
                        .appendFormDataParameter("source", "main_web")
                        .appendFormDataParameter("captcha_key", captchaKey)
                        .build());
                int code = object.get("code").getAsInt();
                if (code == 0) {
                    RequestUtils.saveCookie(true);
                    Settings.setRefreshToken(object.getAsJsonObject("data").get("refresh_token").getAsString());
                    Platform.runLater(() -> ((LoginMain) this.getScene()).loginSuccess());
                } else {
                    this.loginFail(object.get("message").getAsString() + "(%s)".formatted(code), true);
                }
            }
        }).exceptionally(throwable -> {
            this.loginFail("登录请求失败", true);
            return null;
        });
    }

    private void initCountries() {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpGet("https://passport.bilibili.com/web/generic/country/list").build());
        if (object.get("code").getAsInt() == 0) {
            object.getAsJsonObject("data").getAsJsonArray("common").forEach(country -> {
                JsonObject countryObject = country.getAsJsonObject();
                countries.add(new Country(countryObject.get("id").getAsInt(), countryObject.get("cname").getAsString(), countryObject.get("country_id").getAsString()));
            });
            object.getAsJsonObject("data").getAsJsonArray("others").forEach(country -> {
                JsonObject countryObject = country.getAsJsonObject();
                countries.add(new Country(countryObject.get("id").getAsInt(), countryObject.get("cname").getAsString(), countryObject.get("country_id").getAsString()));
            });
            this.selectedCountry = countries.getFirst().id;
            Platform.runLater(() -> this.countryButton.setText("+" + countries.getFirst().countryId));
        }
    }

    public void selectCountry() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(360);
        VBox vBox = new VBox(2);
        vBox.setAlignment(Pos.CENTER);
        scrollPane.setContent(vBox);
        TitledDialog dialog = new TitledDialog("选择国家或地区", ((LoginMain) this.getScene()).getRootStackPane(), scrollPane, JFXDialog.DialogTransition.CENTER, false);
        this.countries.forEach(country -> {
            QMButton button = new QMButton("%s (+%s)".formatted(country.cname, country.countryId), null);
            if (country.id == selectedCountry) {
                button.setTextFill(Paint.valueOf("0x1a8bcc"));
            }
            button.setPrefWidth(400);
            button.setTextAlignment(TextAlignment.LEFT);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setOnAction(actionEvent -> {
                this.selectedCountry = country.id;
                this.selectedCountryCid = country.countryId;
                this.countryButton.setText("+" + country.countryId);
                dialog.close();
            });
            vBox.getChildren().add(button);
        });
        dialog.show();
    }

    @Override
    public void startCaptcha() {
        if (!this.fromPassword && this.accountField.getText().isEmpty()) {
            this.loginFail("请输入手机号", true);
            return;
        }
        this.smsCodeButton.disable(true);
        this.captcha = new Stage(StageStyle.UNDECORATED);
        this.captcha.initModality(Modality.WINDOW_MODAL);
        this.captcha.setResizable(false);
        this.captcha.initOwner(this.getScene().getWindow());
        this.captcha.show();
        if (this.fromPassword) {
            this.captcha.setScene(new Captcha(this, true));
        } else {
            this.captcha.setScene(new Captcha(this, false));
        }
        this.captcha.setOnCloseRequest(event -> {
            ((Captcha) captcha.getScene()).stop();
            captchaFail(true, null);
        });
    }

    @Override
    public void captchaSuccess(String token, String gt, String challenge, String validate, String seccode) {
        captcha.close();
        if (this.fromPassword) {
            JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://passport.bilibili.com/x/safecenter/common/sms/send")
                    .appendFormDataParameter("tmp_code", tmpCode)
                    .appendFormDataParameter("sms_type", "loginTelCheck")
                    .appendFormDataParameter("recaptcha_token", token)
                    .appendFormDataParameter("gee_challenge", challenge)
                    .appendFormDataParameter("gee_validate", validate)
                    .appendFormDataParameter("gee_seccode", seccode)
                    .build());
            if (object.get("code").getAsInt() == 0) {
                captchaKey = object.getAsJsonObject("data").get("captcha_key").getAsString();
            }
        } else {
            JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://passport.bilibili.com/x/passport-login/web/sms/send")
                    .appendFormDataParameter("cid", this.selectedCountryCid)
                    .appendFormDataParameter("tel", this.accountField.getText())
                    .appendFormDataParameter("source", "main-fe-header")
                    .appendFormDataParameter("token", token)
                    .appendFormDataParameter("challenge", challenge)
                    .appendFormDataParameter("validate", validate)
                    .appendFormDataParameter("seccode", seccode)
                    .build());
            int code = object.get("code").getAsInt();
            if (code == 0) {
                captchaKey = object.getAsJsonObject("data").get("captcha_key").getAsString();
            } else {
                Platform.runLater(() -> this.loginMain.showDialogMessage(object.get("message").getAsString() + "(%s)".formatted(code), true));
                return;
            }
        }
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            countdown--;
            this.smsCodeButton.setText(this.countdown + "s");
        }));
        timeline.setCycleCount(60);
        timeline.setOnFinished(event -> {
            this.smsCodeButton.disable(false);
            this.smsCodeButton.setText("获取验证码");
        });
        timeline.play();
        Platform.runLater(() -> {
            this.loginMain.showDialogMessage("验证码已发送");

            this.smsCodeButton.disable(true);
            this.countdown = 60;
            this.smsCodeButton.setText(this.countdown + "s");
        });
    }

    @Override
    public void captchaFail(boolean manualCancel, String failMessage) {
        Platform.runLater(() -> {
            captcha.close();
            this.smsCodeButton.disable(false);
        });
        if (manualCancel) {
            loginFail("取消验证", false);
        } else {
            loginFail(failMessage, true);
        }
    }

    public void loginFail(String failMessage, boolean isError) {
        Platform.runLater(() -> {
            this.loginMain.loginFail(failMessage, isError);
            if (!this.fromPassword) {
                this.accountField.setDisable(false);
            }
            this.passwordField.setDisable(false);
            this.loginButton.setText("登录");
            this.loginButton.setGraphic(new WhiteFontIcon("fas-sign-in-alt"));
        });
    }

    public static SMSLogin standalone(LoginMain loginMain) {
        return new SMSLogin(false, loginMain);
    }

    public static SMSLogin fromPassword(LoginMain loginMain, String tmpCode, String requestId) {
        SMSLogin smsLogin = new SMSLogin(true, loginMain);
        smsLogin.tmpCode = tmpCode;
        smsLogin.requestId = requestId;
        smsLogin.accountField.setText("获取中……");
        CompletableFuture.supplyAsync(() -> {
            JsonObject object = RequestUtils.requestToJson(RequestUtils.httpGet("https://passport.bilibili.com/x/safecenter/user/info")
                    .appendUrlParameter("tmp_code", tmpCode)
                    .build());
            if (object.get("code").getAsInt() == 0) {
                return object.getAsJsonObject("data").getAsJsonObject("account_info").get("hide_tel").getAsString();
            }
            throw new RuntimeException();
        }).exceptionally(throwable -> "获取失败").thenAccept(result -> Platform.runLater(() -> smsLogin.accountField.setText(result)));
        return smsLogin;
    }

    @Override
    public boolean lockLoginMethod() {
        return captchaKey != null || fromPassword;
    }

    private record Country(int id, String cname, String countryId) {

    }
}
