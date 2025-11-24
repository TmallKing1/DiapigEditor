package top.pigest.dialogeditor.control;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import top.pigest.dialogeditor.Settings;

public class ScrollableText extends ScrollablePane<Text> {

    public ScrollableText(String text, double width) {
        super(new Text(text), width);
        this.getText().setFont(Settings.DEFAULT_FONT);
        resetAnimation();
    }

    public ScrollableText(String text, double width, boolean centered) {
        super(new Text(text), width, centered);
        this.getText().setFont(Settings.DEFAULT_FONT);
        resetAnimation();
    }

    public ScrollableText(String text, double width, boolean centered, Font font) {
        super(new Text(text), width, centered);
        this.getText().setFont(font);
        resetAnimation();
    }

    public Text getText() {
        return this.getNode();
    }

    public String getTextContent() {
        return this.getNode().getText();
    }

    public void setText(String text) {
        this.getNode().setText(text);
        this.resetAnimation();
    }
}
