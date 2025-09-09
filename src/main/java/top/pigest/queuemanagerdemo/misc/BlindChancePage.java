package top.pigest.queuemanagerdemo.misc;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXScrollPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.liveroom.data.Gift;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.control.ChildPage;
import top.pigest.queuemanagerdemo.control.NamedPage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class BlindChancePage extends VBox implements ChildPage, NamedPage {
    private Pane parentPage;
    private final JFXComboBox<Gift> giftComboBox;
    private final QMButton execute;
    private Node display;

    public BlindChancePage() {
        this.setAlignment(Pos.TOP_CENTER);
        Text hint = new Text("选择要查询的盲盒");
        hint.setFont(Settings.DEFAULT_FONT);
        VBox.setMargin(hint, new Insets(10));
        giftComboBox = new JFXComboBox<>();
        giftComboBox.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/combobox.css")).toExternalForm());
        giftComboBox.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/scrollbar.css")).toExternalForm());
        giftComboBox.setDisable(true);
        giftComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Gift> call(ListView<Gift> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Gift item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            this.setFont(Settings.DEFAULT_FONT);
                            this.setText("%s (售价 %s 电池)".formatted(item.getName(), item.getPrice() / 100));
                        }
                    }
                };
            }
        });
        giftComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Gift item, boolean empty) {
                super.updateItem(item, empty);
                this.setFont(Settings.DEFAULT_FONT);
                if (item != null && !empty) {
                    this.setText(item.getName());
                }
            }
        });
        giftComboBox.setPrefWidth(300);
        CompletableFuture.supplyAsync(this::getBlinds).whenComplete((gifts, throwable) -> {
            if (throwable == null) {
                Platform.runLater(() -> {
                    giftComboBox.setDisable(false);
                    giftComboBox.getItems().addAll(gifts);
                });
            }
        });
        VBox.setMargin(giftComboBox, new Insets(10));
        execute = getExecute(new WhiteFontIcon("fas-search"));
        VBox.setMargin(execute, new Insets(10));
        this.getChildren().addAll(hint, giftComboBox, execute);
    }

    private QMButton getExecute(WhiteFontIcon whiteFontIcon) {
        QMButton execute = new QMButton("查询", "#1a8bcc");
        execute.setGraphic(whiteFontIcon);
        execute.setDefaultButton(true);
        execute.setPrefWidth(150);
        execute.setOnAction(event -> {
            if (giftComboBox.getValue() == null) {
                Utils.showDialogMessage("尚未选择礼物", true, QueueManager.INSTANCE.getMainScene().getRootDrawer());
            }
            startExecute();
            this.getChildren().remove(display);
            CompletableFuture.supplyAsync(() -> LiveRoomApi.getBlindInfo(giftComboBox.getValue()))
                    .whenComplete((info, throwable) -> Platform.runLater(() -> {
                        endExecute();
                        if (throwable != null) {
                            Utils.showDialogMessage("请求错误", true, QueueManager.INSTANCE.getMainScene().getRootDrawer());
                            return;
                        }
                        display = createScrollPane(info);
                        this.getChildren().add(display);
                    }));
        });
        return execute;
    }

    private void startExecute() {
        this.execute.disable(true);
        this.execute.setGraphic(new WhiteFontIcon("fas-bullseye"));
        this.execute.setText("检索中");
    }

    private void endExecute() {
        this.execute.disable(false);
        this.execute.setGraphic(new WhiteFontIcon("fas-search"));
        this.execute.setText("检索");
    }

    private List<Gift> getBlinds() {
        return LiveRoomApi.getGiftList().stream().filter(gift -> gift.getType() == 6).toList();
    }

    private ScrollPane createScrollPane(LinkedHashMap<Gift, String> gift) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/scrollbar.css")).toExternalForm());
        scrollPane.setFitToWidth(true);
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        gift.forEach((key, value) -> {
            BorderPane borderPane = new BorderPane();
            ImageView left = new ImageView();
            left.setFitHeight(50);
            left.setFitWidth(50);
            CompletableFuture.supplyAsync(() -> new Image(key.getImgBasic())).whenComplete((image, throwable) -> {
                if (throwable == null) {
                    Platform.runLater(() -> left.setImage(image));
                }
            });
            borderPane.setLeft(left);
            BorderPane.setAlignment(left, Pos.CENTER);
            BorderPane.setMargin(left, new Insets(0, 15, 0, 0));

            VBox center = new VBox(5);
            center.setAlignment(Pos.CENTER_LEFT);
            Text name = new Text(key.getName());
            name.setFont(Settings.DEFAULT_FONT);
            Text price = new Text("价值 %s 电池".formatted(key.getPrice() / 100));
            price.setFont(Settings.DEFAULT_FONT);
            if (key.getProperty("win").equals("1")) {
                price.setFill(Paint.valueOf("#1a8bcc"));
            }
            center.getChildren().addAll(name, price);
            borderPane.setCenter(center);
            BorderPane.setAlignment(center, Pos.CENTER);

            Text right = new Text(value);
            double r;
            try {
                r = NumberFormat.getPercentInstance().parse(value).doubleValue();
            } catch (ParseException e) {
                r = Double.parseDouble(value.replace("%", "")) / 100;
            }
            right.setFont(new Font(Settings.BOLD_FONT.getFamily(), 30));
            if (r < 0.001) {
                right.setFill(Paint.valueOf("#F0D813"));
            } else if (r < 0.005) {
                right.setFill(Paint.valueOf("#7E84F7"));
            } else if (r < 0.05) {
                right.setFill(Paint.valueOf("#73FBFD"));
            } else if (r < 0.2) {
                right.setFill(Paint.valueOf("#75FA61"));
            } else {
                right.setFill(Paint.valueOf("#C0C0C0"));
            }
            right.setStroke(Paint.valueOf("#000000"));
            right.setStrokeWidth(1.5);
            right.setStrokeType(StrokeType.OUTSIDE);
            borderPane.setRight(right);
            BorderPane.setAlignment(right, Pos.CENTER);
            BorderPane.setMargin(right, new Insets(0, 15, 0, 0));
            vBox.getChildren().add(borderPane);
        });
        scrollPane.setContent(vBox);
        JFXScrollPane.smoothScrolling(scrollPane);
        VBox.setMargin(scrollPane, new Insets(10, 40, 10, 40));
        return scrollPane;
    }

    @Override
    public Pane getParentPage() {
        return parentPage;
    }

    @Override
    public void setParentPage(Pane parentPage) {
        this.parentPage = parentPage;
    }

    @Override
    public String getName() {
        return "盲盒概率";
    }
}
