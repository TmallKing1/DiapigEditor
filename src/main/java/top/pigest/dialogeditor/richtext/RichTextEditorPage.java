package top.pigest.dialogeditor.richtext;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.skins.JFXCustomColorPickerDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.dialogeditor.DialogEditor;
import top.pigest.dialogeditor.Settings;
import top.pigest.dialogeditor.control.ChildPage;
import top.pigest.dialogeditor.control.NamedPage;
import top.pigest.dialogeditor.control.QMButton;
import top.pigest.dialogeditor.control.WhiteFontIcon;
import top.pigest.dialogeditor.util.Utils;

import java.lang.reflect.Method;
import java.util.Objects;

public class RichTextEditorPage extends BorderPane implements NamedPage, ChildPage {
    private Pane parentPage;

    private final HBox buttons;
    private final QMButton colorButton = new QMButton("选择颜色");
    private final QMButton paintButton = new QMButton("");
    private final QMButton boldButton = new QMButton("").withIcon(new WhiteFontIcon("fas-bold"));
    private final QMButton copyButton = new QMButton("复制文字");
    private final TextArea textArea = new JFXTextArea();
    private final TextFlow textFlow;

    private Color currentColor = Color.valueOf("#1a8bcc");
    private String backText;

    public RichTextEditorPage() {
        buttons = new HBox(5);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(colorButton, copyButton);
        buttons.setPadding(new Insets(10));
        this.setTop(buttons);

        updateColorButton(currentColor);
        colorButton.setPrefWidth(150);
        colorButton.setOnAction(event -> showColorPicker());

        copyButton.setGraphic(new WhiteFontIcon("far-copy"));
        copyButton.setPrefWidth(150);
        copyButton.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(this.textArea.getText());
            clipboard.setContent(content);
        });

        VBox center = new VBox(10);
        center.setBorder(new Border(new BorderStroke(Color.DIMGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        textArea.setFont(Settings.CODE_FONT);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            String s1 = newValue.replace("\n", "\\n");
            if (s1.equals(newValue)) {
                updateTextFlow();
            } else {
                textArea.setText(s1);
            }
        });
        textArea.setPrefHeight(300);
        textFlow = new TextFlow();
        textFlow.getStylesheets().add(Objects.requireNonNull(DialogEditor.class.getResource("css/selectable-textflow.css")).toExternalForm());
        textFlow.setOnMousePressed(event -> {
            textFlow.requestFocus();
            event.consume();
        });
        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setPrefHeight(300);
        scrollPane.getStylesheets().add(Objects.requireNonNull(DialogEditor.class.getResource("css/scrollbar.css")).toExternalForm());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        center.getChildren().addAll(textArea, scrollPane);
        BorderPane.setMargin(center, new Insets(10));
        this.setCenter(center);
    }

    public void showColorPicker() {
        JFXCustomColorPickerDialog dialog = new JFXCustomColorPickerDialog(DialogEditor.INSTANCE.getPrimaryStage());
        setColor(dialog, currentColor);
        dialog.setOnSave(() -> {
            this.currentColor = getColor(dialog);
            if (currentColor != null) {
                updateColorButton(currentColor);
            }
        });
        dialog.show();
    }

    public static void setColor(JFXCustomColorPickerDialog dialog, Color color) {
        try {
            Method setCustomColor = JFXCustomColorPickerDialog.class.getDeclaredMethod("setCustomColor", Color.class);
            setCustomColor.setAccessible(true);
            setCustomColor.invoke(dialog, color);
        } catch (Exception ignored) {
        }
    }

    public static Color getColor(JFXCustomColorPickerDialog dialog) {
        try {
            Method getCustomColor = JFXCustomColorPickerDialog.class.getDeclaredMethod("getCustomColor");
            getCustomColor.setAccessible(true);
            return (Color) getCustomColor.invoke(dialog);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateColorButton(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        double l = r * 0.2126 + g * 0.7152 + b * 0.0722;
        if (l >= 0.5) {
            FontIcon fontIcon = new FontIcon("fas-palette");
            colorButton.setGraphic(fontIcon);
            colorButton.setTextFill(Color.BLACK);
        } else {
            WhiteFontIcon fontIcon = new WhiteFontIcon("fas-palette");
            colorButton.setGraphic(fontIcon);
            colorButton.setTextFill(Color.WHITE);
        }
        colorButton.setBackgroundColor(Utils.colorToString(Color.rgb((int) (r * 255), (int) (g * 255), (int) (b * 255))));
    }

    public void updateTextFlow() {
        textFlow.getChildren().clear();
        textFlow.getChildren().addAll(TextParser.parseText(textArea.getText(), message -> {
            Utils.showChoosingDialog("文本异常", message, "重新编辑", "取消", event -> {}, event -> {}, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
        }));
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
        return "文本编辑";
    }
}
