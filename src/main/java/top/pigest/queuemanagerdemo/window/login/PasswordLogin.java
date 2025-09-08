package top.pigest.queuemanagerdemo.window.login;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.util.RequestUtils;
import top.pigest.queuemanagerdemo.util.Utils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

class PasswordLogin extends VBox implements CaptchaLogin, LoginMethodLocker {
    private Stage captcha;
    private final LoginMain loginMain;
    private final QMButton loginButton = Utils.make(new QMButton("登录", QMButton.DEFAULT_COLOR), button -> {
        button.disable(true);
        button.setPrefWidth(200);
        button.setOnAction(actionEvent -> login());
        button.setDefaultButton(true);
        button.setGraphic(new WhiteFontIcon("fas-sign-in-alt"));
    });
    private final JFXTextField accountField = Utils.make(new JFXTextField(), textField -> {
        textField.setLabelFloat(true);
        textField.setFocusColor(Paint.valueOf("#1a8bcc"));
        textField.setPrefWidth(400);
        textField.setPromptText("邮箱/手机号");
        textField.setFont(Settings.DEFAULT_FONT);
    });
    private final JFXPasswordField passwordField = Utils.make(new JFXPasswordField(), passwordField -> {
        passwordField.setLabelFloat(true);
        passwordField.setFocusColor(Paint.valueOf("#1a8bcc"));
        passwordField.setPrefWidth(400);
        passwordField.setPromptText("密码");
        passwordField.setFont(Settings.DEFAULT_FONT);
    });
    private String account;
    private String password;

    PasswordLogin(LoginMain loginMain) {
        super(30);
        this.loginMain = loginMain;
        this.accountField.textProperty().addListener((observable, oldValue, newValue) -> loginButton.disable(accountField.getText().isEmpty() || passwordField.getText().isEmpty()));
        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> loginButton.disable(accountField.getText().isEmpty() || passwordField.getText().isEmpty()));
        this.setAlignment(Pos.CENTER);
        this.getChildren().add(Utils.make(new HBox(20), hBox -> {
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().add(accountField);
        }));
        this.getChildren().add(Utils.make(new HBox(20), hBox -> {
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().add(passwordField);
        }));
        this.getChildren().add(loginButton);
    }

    private void login() {
        this.accountField.setDisable(true);
        this.passwordField.setDisable(true);
        this.loginButton.disable(true);
        this.account = this.accountField.getText();
        this.password = this.passwordField.getText();
        this.accountField.clear();
        this.passwordField.clear();
        this.loginMain.lockLoginMethodButtons(true);

        // Stage 1
        this.loginButton.setText("登录中(1/3)");
        this.loginButton.setGraphic(new WhiteFontIcon("fas-bullseye"));
        startCaptcha();
    }

    private void loginStage2(String token, String challenge, String validate, String seccode) {
        this.loginButton.setText("登录中(2/3)");
        new Thread(() -> {
            try {
                JsonObject element = RequestUtils.requestToJson(RequestUtils.httpGet("https://passport.bilibili.com/x/passport-login/web/key").build());
                if (element.get("code").getAsInt() == 0) {
                    String hash = element.getAsJsonObject("data").get("hash").getAsString();
                    String key = element.getAsJsonObject("data").get("key").getAsString();
                    String encryptedPassword = encrypt(hash, key);
                    loginStage3(encryptedPassword, token, challenge, validate, seccode);
                } else {
                    loginFail("无法拉取加密信息", true);
                }
            } catch (Exception e) {
                loginFail("加密失败", true);
            }
        }).start();
    }

    private void loginStage3(String encryptedPassword, String token, String challenge, String validate, String seccode) {
        Platform.runLater(() -> this.loginButton.setText("登录中(3/3)"));
        JsonObject element = RequestUtils.requestToJson(RequestUtils.httpPost("https://passport.bilibili.com/x/passport-login/web/login")
                .appendFormDataParameter("username", account)
                .appendFormDataParameter("password", encryptedPassword)
                .appendFormDataParameter("keep", "0")
                .appendFormDataParameter("token", token)
                .appendFormDataParameter("challenge", challenge)
                .appendFormDataParameter("validate", validate)
                .appendFormDataParameter("seccode", seccode)
                .appendFormDataParameter("source", "main_web")
                .build());
        int code = element.get("code").getAsInt();
        if (code == 0) {
            JsonObject data = element.getAsJsonObject("data");
            if (!data.get("message").getAsString().isEmpty()) {
                loginFail(data.get("message").getAsString(), false);
                String url = data.get("url").getAsString();
                String tmpCode = url.substring(url.indexOf("tmp_token=") + 10);
                if (tmpCode.indexOf("&") > 0) {
                    tmpCode = tmpCode.substring(0, tmpCode.indexOf("&"));
                }
                String requestId = url.substring(url.indexOf("requestId=") + 10);
                if (requestId.indexOf("&") > 0) {
                    requestId = requestId.substring(0, requestId.indexOf("&"));
                }
                String finalTmpCode = tmpCode;
                String finalRequestId = requestId;
                Platform.runLater(() -> {
                    SMSLogin node = SMSLogin.fromPassword(this.loginMain, finalTmpCode, finalRequestId);
                    this.loginMain.switchLoginContainer(node);
                });
            } else {
                RequestUtils.saveCookie(true);
                Settings.setRefreshToken(element.getAsJsonObject("data").get("refresh_token").getAsString());
                ((LoginMain) this.getScene()).loginSuccess();
            }
        } else {
            loginFail(element.get("message").getAsString() + "(%s)".formatted(code), true);
        }
    }

    private String encrypt(String hash, String publicKey) throws Exception {
        String[] split = publicKey.strip().split("\n");
        String newKey = split[1] + split[2] + split[3] + split[4];

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(newKey));
        PublicKey key = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.PUBLIC_KEY, key);
        byte[] bytes = cipher.doFinal((hash + password).getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public void startCaptcha() {
        captcha = new Stage(StageStyle.UNDECORATED);
        captcha.initModality(Modality.WINDOW_MODAL);
        captcha.setResizable(false);
        captcha.initOwner(this.getScene().getWindow());
        captcha.show();
        captcha.setScene(new Captcha(this));
        captcha.setOnCloseRequest(event -> {
            ((Captcha) captcha.getScene()).stop();
            captchaFail(true, null);
        });
    }

    @Override
    public void captchaSuccess(String token, String gt, String challenge, String validate, String seccode) {
        captcha.close();
        loginStage2(token, challenge, validate, seccode);
    }

    @Override
    public void captchaFail(boolean manualCancel, String failMessage) {
        Platform.runLater(() -> {
            captcha.close();
            if (manualCancel) {
                loginFail("取消验证", false);
            } else {
                loginFail(failMessage, true);
            }
        });
    }

    public void loginFail(String failMessage, boolean isError) {
        Platform.runLater(() -> {
            this.loginMain.loginFail(failMessage, isError);
            this.accountField.setDisable(false);
            this.passwordField.setDisable(false);
            this.loginButton.setText("登录");
            this.loginButton.setGraphic(new WhiteFontIcon("fas-sign-in-alt"));
        });
    }

    @Override
    public boolean lockLoginMethod() {
        return !(this.accountField.getText().isEmpty() && this.passwordField.getText().isEmpty());
    }
}
