package top.pigest.dialogeditor.main;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.dialogeditor.Settings;
import top.pigest.dialogeditor.control.*;
import top.pigest.dialogeditor.resource.RequireCleaning;
import top.pigest.dialogeditor.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MainScene extends Scene {
    private final QMButton accountButton = new QMButton("用户", null);
    private final HBox menuItems = Utils.make(new HBox(), hBox -> hBox.setAlignment(Pos.CENTER_LEFT));
    private final QMButton bar = Utils.make(new QMButton("", null), qmButton -> qmButton.setOnAction(event -> showSidebar()));
    private final BorderPane top = Utils.make(new BorderPane(), border -> {
        bar.setGraphic(new WhiteFontIcon("fas-bars"));
        border.setLeft(new BorderPane(menuItems, null, null, null, bar));
        border.setRight(accountButton);
    });
    private final BorderPane borderPane = Utils.make(new BorderPane(), border -> border.setTop(top));
    private final JFXDrawer drawer = Utils.make(new JFXDrawer(new Duration(300)), drawer1 -> {
        drawer1.setDefaultDrawerSize(250);
        drawer1.setContent(borderPane);
    });
    private final List<QMButton> drawerButtons = new ArrayList<>();
    private final QMButton back = Utils.make(new QMButton("", null), qmButton -> {
        qmButton.setGraphic(new FontIcon("far-arrow-alt-circle-left"));
        qmButton.setOnAction(null);
    });

    private MainPage mainPage;

    public MainScene() {
        super(new Pane(), 800, 600, false, SceneAntialiasing.BALANCED);
        this.setRoot(drawer);
        drawer.setStyle("-fx-background-color: #26282b;");
        this.setFill(Color.valueOf("#26282b"));
        init();
    }

    private void init() {
        Platform.runLater(() -> {
            mainPage = new MainPage(this);
            this.setMainContainer(mainPage, "主页");
            accountButton.setGraphic(new WhiteFontIcon("far-user-circle"));
            VBox vbox = new VBox();
            vbox.setStyle("-fx-background-color: #26282b;");
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

    public Node getMainContainer() {
        return this.borderPane.getCenter();
    }

    public void setMainContainer(Node mainContainer, String id) {
        if (this.borderPane.getCenter() instanceof RequireCleaning) {
            ((RequireCleaning) this.borderPane.getCenter()).clean();
        }
        mainContainer.setId(id);
        this.borderPane.setCenter(mainContainer);
        drawerButtons.forEach(qmButton -> {
            if (qmButton.getId().equals(id)) {
                qmButton.setTextFill(Paint.valueOf("#1a8bcc"));
                ((FontIcon) qmButton.getGraphic()).setIconColor(Paint.valueOf("#1a8bcc"));
            } else {
                qmButton.setTextFill(Paint.valueOf("WHITE"));
                ((FontIcon) qmButton.getGraphic()).setIconColor(Paint.valueOf("WHITE"));
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
        if (supplier == null) {
            button.disable(true);
        } else {
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
        }
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
                back.setOnAction(event -> this.setMainContainer(parent, parent.getId()));
                this.menuItems.getChildren().add(back);
                if (childPage instanceof DataEditor) {
                    back.setGraphic(new WhiteFontIcon("far-save"));
                    if (!(this.getMainContainer() instanceof MultiMenuProvider<?>)) {
                        back.setText("保存");
                    }
                    back.setOnAction(event -> {
                        ((DataEditor) childPage).save();
                        this.setMainContainer(parent, parent.getId());
                    });
                } else {
                    back.setGraphic(new WhiteFontIcon("far-arrow-alt-circle-left"));
                    if (!(this.getMainContainer() instanceof MultiMenuProvider<?>)) {
                        back.setText("返回");
                    }
                }
                if (this.getMainContainer() instanceof MultiMenuProvider<?>) {
                    back.setText("");
                }
            }
        } else {
            back.setOnAction(null);
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
        this.menuItems.getChildren().forEach(node -> ((QMButton) node).setTextFill(Paint.valueOf("WHITE")));
        if (this.getMainContainer() instanceof MultiMenuProvider<?> multiMenuProvider) {
            int currentMenuIndex = multiMenuProvider.getCurrentMenuIndex();
            currentMenuIndex = this.getMainContainer() instanceof ChildPage ? currentMenuIndex + 1 : currentMenuIndex;
            if (currentMenuIndex != -1) {
                ((QMButton) this.menuItems.getChildren().get(currentMenuIndex)).setTextFill(Paint.valueOf("#1a8bcc"));
            }
        }
    }

    public String getUserName() {
        return "用户";
    }

    private void showSidebar() {
        drawer.open();
    }
}
