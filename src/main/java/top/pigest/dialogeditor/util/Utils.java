package top.pigest.dialogeditor.util;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTooltip;
import com.jfoenix.effects.JFXDepthManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import top.pigest.dialogeditor.Settings;
import top.pigest.dialogeditor.control.QMButton;

import java.util.*;
import java.util.function.Consumer;

public class Utils {
    private static final Map<StackPane, JFXSnackbar> snackbarMap = new HashMap<>();

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static <T> void onPresent(T object, Consumer<T> consumer) {
        if (object == null) {
            return;
        }
        consumer.accept(object);
    }

    public static void showDialogMessage(String message, boolean isError, StackPane rootStackPane) {
        showDialogMessage(message, isError, rootStackPane, 5, 400);
    }

    public static void showDialogMessage(String message, boolean isError, StackPane rootStackPane, double duration) {
        showDialogMessage(message, isError, rootStackPane, duration, 400);
    }

    public static void showDialogMessage(String message, boolean isError, StackPane rootStackPane, double duration, int width) {
        JFXSnackbar snackbar = new JFXSnackbar(rootStackPane);
        snackbar.setPrefWidth(width);
        snackbar.setCursor(Cursor.HAND);
        snackbar.setOnMouseClicked(event -> snackbar.close());
        Label toast = new Label();
        toast.setMinWidth(Control.USE_PREF_SIZE);
        toast.setWrapText(true);
        toast.setText(message);
        toast.setFont(Settings.DEFAULT_FONT);
        toast.setTextFill(Paint.valueOf("WHITE"));
        StackPane toastContainer = new StackPane(toast);
        toastContainer.setPadding(new Insets(10, 20, 10, 20));
        toastContainer.setBackground(new Background(new BackgroundFill(Paint.valueOf(isError ? "#8B0000" : "#1f1e33"), new CornerRadii(3), Insets.EMPTY)));
        JFXDepthManager.setDepth(toastContainer, 2);
        JFXSnackbar.SnackbarEvent snackbarEvent = new JFXSnackbar.SnackbarEvent(toastContainer, Duration.seconds(duration));
        if (snackbarMap.containsKey(rootStackPane)) {
            snackbarMap.get(rootStackPane).close();
        }
        snackbar.enqueue(snackbarEvent);
        snackbarMap.put(rootStackPane, snackbar);
    }

    public static void showChoosingDialog(String title, String message,
                                          String strA, String strB,
                                          Consumer<ActionEvent> actionA, Consumer<ActionEvent> actionB,
                                          StackPane rootStackPane) {
        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: #26282b");
        vBox.setPrefWidth(500);
        vBox.setAlignment(Pos.CENTER);
        JFXDialog dialog = new JFXDialog(rootStackPane, vBox, JFXDialog.DialogTransition.CENTER, false);
        dialog.setId("close-confirm");
        vBox.setPadding(new Insets(20, 20, 20, 20));
        Text titleNode = new Text(title);
        titleNode.setTextAlignment(TextAlignment.CENTER);
        titleNode.setFont(new Font(Settings.BOLD_FONT.getFamily(), 30));
        titleNode.setFill(Color.LIGHTGRAY);
        VBox.setMargin(titleNode, new Insets(0, 0, 10, 0));
        vBox.getChildren().add(titleNode);
        Text text = new Text(message);
        text.setFill(Color.WHITE);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Settings.DEFAULT_FONT);
        text.setWrappingWidth(450);
        VBox.setMargin(text, new Insets(0, 0, 30, 0));
        vBox.getChildren().add(text);
        HBox hBox = new HBox(40);
        hBox.setAlignment(Pos.CENTER);
        QMButton ok = new QMButton(strA, QMButton.DEFAULT_COLOR);
        if (strA.length() <= 2) {
            ok.setPrefWidth(80);
        }
        ok.setOnAction(event -> {
            actionA.accept(event);
            dialog.close();
        });
        QMButton cancel = new QMButton(strB, "#bb5555");
        if (strB.length() <= 2) {
            cancel.setPrefWidth(80);
        }
        cancel.setOnAction(event -> {
            actionB.accept(event);
            dialog.close();
        });
        hBox.getChildren().addAll(ok, cancel);
        vBox.getChildren().add(hBox);
        dialog.show();
    }

    public static Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Settings.DEFAULT_FONT);
        return label;
    }

    public static Label createLabel(String text, String color) {
        Label label = createLabel(text);
        label.setTextFill(Paint.valueOf(color));
        return label;
    }

    public static Label createLabel(String text, int prefWidth) {
        Label label = createLabel(text);
        label.setPrefWidth(prefWidth);
        return label;
    }

    public static Label createLabel(String text, int prefWidth, Pos alignment) {
        Label label = createLabel(text);
        label.setPrefWidth(prefWidth);
        label.setAlignment(alignment);
        return label;
    }

    public static JFXTooltip createTooltip(String text) {
        JFXTooltip tooltip = new JFXTooltip();
        tooltip.setHeight(18);
        Text text1 = new Text(text);
        text1.setFont(new Font(Settings.DEFAULT_FONT.getFamily(), 15));
        text1.setFill(Color.LIGHTGRAY);
        tooltip.setGraphic(text1);
        tooltip.setFont(Settings.DEFAULT_FONT);
        tooltip.setShowDelay(Duration.seconds(0.1));
        tooltip.setHideDelay(Duration.seconds(0.1));
        tooltip.setShowDuration(Duration.seconds(5));
        return tooltip;
    }

    public static Color toColor(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return Color.rgb(red, green, blue);
    }

    public static String colorToString(Color color) {
        int r = Math.toIntExact(Math.round(color.getRed() * 255.0));
        int g = Math.toIntExact(Math.round(color.getGreen() * 255.0));
        int b = Math.toIntExact(Math.round(color.getBlue() * 255.0));
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
