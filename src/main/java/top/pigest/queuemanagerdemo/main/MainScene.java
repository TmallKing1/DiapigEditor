package top.pigest.queuemanagerdemo.main;

import com.google.gson.JsonObject;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.*;
import top.pigest.queuemanagerdemo.liveroom.LiveMessageService;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.music.MusicHandler;
import top.pigest.queuemanagerdemo.util.RequestUtils;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.music.ui.MusicSystemPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MainScene extends Scene {
    private final QMButton accountButton = Utils.make(new QMButton("正在登录……", null), qmButton -> qmButton.setOnAction(event -> {
        if (isLogin()) {
            VBox vBox = new VBox(2);
            vBox.setAlignment(Pos.CENTER);
            TitledDialog dialog = new TitledDialog("操作", this.getRootDrawer(), vBox, JFXDialog.DialogTransition.CENTER, true);
            QMButton accountSettings = new QMButton("账号设置", null);
            QMButton exitLogin = new QMButton("退出登录", null);
            accountSettings.setOnAction(e -> dialog.close());
            accountSettings.setGraphic(new FontIcon("fas-user-cog"));
            exitLogin.setOnAction(e -> {
                dialog.close();
                new Thread(this::logout).start();
            });
            exitLogin.setGraphic(new FontIcon("fas-sign-out-alt"));
            vBox.getChildren().add(accountSettings);
            vBox.getChildren().add(exitLogin);
            vBox.getChildren().forEach(node -> {
                ((QMButton) node).setPrefWidth(300);
                ((QMButton) node).setTextAlignment(TextAlignment.LEFT);
                ((QMButton) node).setAlignment(Pos.CENTER_LEFT);
            });
            dialog.show();
        }
    }));
    private final HBox menuItems = Utils.make(new HBox(), hBox -> hBox.setAlignment(Pos.CENTER_LEFT));
    private final QMButton bar = Utils.make(new QMButton("", null), qmButton -> qmButton.setOnAction(event -> showSidebar()));
    private final BorderPane top = Utils.make(new BorderPane(), border -> {
        bar.setGraphic(new FontIcon("fas-bars"));
        border.setLeft(new BorderPane(menuItems, null, null, null, bar));
        border.setRight(accountButton);
    });
    private final BorderPane borderPane = Utils.make(new BorderPane(), border -> border.setTop(top));
    private final JFXDrawer drawer = Utils.make(new JFXDrawer(new Duration(300)), drawer1 -> {
        drawer1.setDefaultDrawerSize(250);
        drawer1.setContent(borderPane);
    });
    private final List<QMButton> drawerButtons = new ArrayList<>();

    private boolean login = false;
    private MainPage mainPage;

    public MainScene() {
        super(new Pane(), 800, 600, false, SceneAntialiasing.BALANCED);
        this.setRoot(drawer);
        refreshLoginState();
        autoMethods();
    }

    public void autoMethods() {
        if (Settings.getMusicServiceSettings().autoPlay && RequestUtils.hasCookie("MUSIC_U")) {
            CompletableFuture.runAsync(MusicHandler.INSTANCE::playNext);
        }
    }

    public boolean isLogin() {
        return login;
    }

    public void logout() {
        try {
            if (RequestUtils.hasCookie("bili_jct")) {
                JsonObject element = RequestUtils.requestToJson(RequestUtils.httpPost("https://passport.bilibili.com/login/exit/v2")
                        .appendUrlParameter("biliCSRF", RequestUtils.getCookie("bili_jct")).build());
                if (element.get("code").getAsInt() == 0) {
                    RequestUtils.saveCookie(true);
                    if (LiveMessageService.getInstance() != null) {
                        LiveMessageService.getInstance().close();
                    }
                    refreshLoginState();
                }
            }
        } catch (IOException e) {
            this.showDialogMessage("登出失败\n" + e.getMessage(), true);
        }
    }

    public void refreshLoginState() {
        RequestUtils.loadCookie();
        try {
            User user = Objects.requireNonNull(LiveRoomApi.uid());
            loggedIn(user.getUsername());
            QueueManager.INSTANCE.SELF = user;
            QueueManager.INSTANCE.ROOM_ID = LiveRoomApi.liveRoomId(user.getUid());
            if (Settings.getDanmakuServiceSettings().autoConnect) {
                try {
                    LiveMessageService.connect();
                } catch (LiveMessageService.ConnectFailedException e) {
                    Platform.runLater(() -> this.showDialogMessage("自动连接弹幕服务失败\n" + e.getMessage(), true));
                }
            }
        } catch (IOException | NullPointerException e) {
            notLoggedIn();
        }
        if (QueueManager.INSTANCE.isLoginOpen()) {
            Platform.runLater(() -> QueueManager.INSTANCE.closeLogin());
        }
    }

    private void notLoggedIn() {
        QueueManager.INSTANCE.SELF = null;
        QueueManager.INSTANCE.ROOM_ID = 0;
        Platform.runLater(() -> {
            this.login = false;
            this.setMainContainer(new LoginPage(), "登录页");
            accountButton.setText("未登录");
            accountButton.setGraphic(null);
            if (((LoginPage) this.getMainContainer()).getLoginButton().isDisable()) {
                ((LoginPage) getMainContainer()).switchLoginButtonState();
            }
            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            QMButton button = new QMButton("登录账号", null);
            button.setPrefWidth(200);
            button.setGraphic(new FontIcon("far-user-circle"));
            button.setOnAction(event -> {
                ((LoginPage) getMainContainer()).login();
                drawer.close();
            });
            vbox.getChildren().add(button);
            drawer.setSidePane(vbox);
        });
    }

    private void loggedIn(String name) {
        Platform.runLater(() -> {
            this.login = true;
            mainPage = new MainPage(this);
            this.setMainContainer(mainPage, "主页");
            accountButton.setText(name);
            accountButton.setGraphic(new FontIcon("far-user-circle"));
            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(drawerButtons);
            drawer.setSidePane(vbox);
        });
    }

    public JFXDrawer getRootDrawer() {
        return drawer;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public void showDialogMessage(String message, boolean isError) {
        Utils.showDialogMessage(message, isError, drawer);
    }

    public void switchLoginButtonState() {
        if (getMainContainer() instanceof LoginPage) {
            ((LoginPage) getMainContainer()).switchLoginButtonState();
        }
        if (getMainContainer() instanceof MusicSystemPage) {
            ((MusicSystemPage) getMainContainer()).switchLoginButtonState();
        }
    }

    public Node getMainContainer() {
        return this.borderPane.getCenter();
    }

    public void setMainContainer(Node mainContainer, String id) {
        mainContainer.setId(id);
        this.borderPane.setCenter(mainContainer);
        drawerButtons.forEach(qmButton -> {
            if (qmButton.getId().equals(id)) {
                qmButton.setTextFill(Paint.valueOf("#1a8bcc"));
                ((FontIcon) qmButton.getGraphic()).setIconColor(Paint.valueOf("#1a8bcc"));
            } else {
                qmButton.setTextFill(Paint.valueOf("BLACK"));
                ((FontIcon) qmButton.getGraphic()).setIconColor(Paint.valueOf("BLACK"));
            }
        });
        this.refreshMenuButtons();
    }

    public void setMainContainer(Supplier<Pane> supplier, QMButton source, Pane parent) {
        if (source != null && !source.getId().isEmpty() && source.getId().equals(this.getMainContainer().getId())) {
            return;
        }
        Pane pane = supplier.get();
        if (pane instanceof ChildPage childPage) {
            childPage.setParentPage(parent);
        }
        this.setMainContainer(pane, source != null ? source.getId() : "");
    }

    public void setMainContainer(Supplier<Pane> supplier, QMButton source) {
        if (source != null && !source.getId().isEmpty() && source.getId().equals(this.getMainContainer().getId())) {
            return;
        }
        this.setMainContainer(supplier, source, (Pane) this.getMainContainer());
    }

    public QMButton createMainFunctionButton(String backgroundColor, String ripplerColor, String text, String iconCode, Supplier<Pane> supplier) {
        QMButton button = new QMButton(null, backgroundColor);
        button.setId(text);
        button.setPrefSize(200, 200);
        FontIcon fontIcon = new WhiteFontIcon(iconCode + ":100");
        Text text1 = new Text(text);
        text1.setFont(Settings.DEFAULT_FONT);
        text1.setFill(Paint.valueOf("WHITE"));
        StackPane stackPane = new StackPane(fontIcon, text1);
        StackPane.setAlignment(fontIcon, Pos.CENTER);
        StackPane.setAlignment(text1, Pos.BOTTOM_LEFT);
        StackPane.setMargin(text1, new Insets(0, 0, 2, 2));
        button.setGraphic(stackPane);
        button.setRipplerFill(Paint.valueOf(ripplerColor));
        button.setOnAction(actionEvent -> this.setMainContainer(supplier, button));
        QMButton button1 = new QMButton(text, null);
        button1.setPrefWidth(200);
        button1.setId(text);
        button1.setGraphic(new FontIcon(iconCode));
        button1.setOnAction(actionEvent -> {
            this.setMainContainer(supplier, button1, mainPage);
            this.getRootDrawer().close();
        });
        this.drawerButtons.add(button1);
        return button;
    }

    public QMButton createMiscFunctionButton(String backgroundColor, String text, String iconCode, Supplier<Pane> supplier) {
        QMButton button = new QMButton(null, backgroundColor);
        button.setId(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        FontIcon fontIcon = new WhiteFontIcon(iconCode);
        button.setText(text);
        button.setGraphic(fontIcon);
        button.setOnAction(actionEvent -> this.setMainContainer(supplier, button));
        return button;
    }
    public void refreshMenuButtons() {
        this.menuItems.getChildren().clear();
        if (this.getMainContainer() instanceof ChildPage childPage) {
            Pane parent = childPage.getParentPage();
            if (parent != null) {
                QMButton back = new QMButton("", null);
                back.setGraphic(new FontIcon("far-arrow-alt-circle-left"));
                back.setOnAction(event -> this.setMainContainer(parent, parent.getId()));
                this.menuItems.getChildren().add(back);
            }
        }
        if (this.getMainContainer() instanceof MultiMenuProvider<?> multiMenuProvider) {
            this.menuItems.getChildren().addAll(multiMenuProvider.getMenuButtons());
            int currentMenuIndex = multiMenuProvider.getCurrentMenuIndex();
            currentMenuIndex = this.getMainContainer() instanceof ChildPage ? currentMenuIndex + 1 : currentMenuIndex;
            if (currentMenuIndex != -1) {
                ((QMButton) this.menuItems.getChildren().get(currentMenuIndex)).setTextFill(Paint.valueOf("#1a8bcc"));
            }
        }
        if (this.getMainContainer() instanceof NamedPage namedPage) {
            bar.setText(namedPage.getName());
        } else {
            bar.setText("");
        }
    }

    public void updateMenuButtonTextFill() {
        this.menuItems.getChildren().forEach(node -> ((QMButton) node).setTextFill(Paint.valueOf("BLACK")));
        if (this.getMainContainer() instanceof MultiMenuProvider<?> multiMenuProvider) {
            int currentMenuIndex = multiMenuProvider.getCurrentMenuIndex();
            currentMenuIndex = this.getMainContainer() instanceof ChildPage ? currentMenuIndex + 1 : currentMenuIndex;
            if (currentMenuIndex != -1) {
                ((QMButton) this.menuItems.getChildren().get(currentMenuIndex)).setTextFill(Paint.valueOf("#1a8bcc"));
            }
        }
    }

    public String getUserName() {
        return isLogin() ? accountButton.getText() : null;
    }

    private void showSidebar() {
        drawer.open();
    }
}
