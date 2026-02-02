package top.pigest.dialogeditor.control;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.paint.Color;
import top.pigest.dialogeditor.Settings;

public class QMTextField extends JFXTextField {
    public QMTextField(String promptText) {
        super();
        this.setUnFocusColor(Color.LIGHTGRAY);
        this.setFocusColor(Color.AQUA);
        this.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
        this.setFont(Settings.DEFAULT_FONT);
        this.setPrefWidth(200);
        this.setMaxWidth(USE_PREF_SIZE);
        this.setPromptText(promptText);
    }
}
