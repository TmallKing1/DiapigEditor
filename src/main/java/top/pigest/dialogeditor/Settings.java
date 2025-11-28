package top.pigest.dialogeditor;

import javafx.scene.text.Font;

import java.io.*;

public class Settings {
    public static final File DATA_DIRECTORY = new File(System.getProperty("user.dir") + "\\.PPDD");
    public static final Font DEFAULT_FONT;
    public static final Font BOLD_FONT;
    public static final Font CODE_FONT;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:141.0) Gecko/20100101 Firefox/141.0";

    static {
        try {
            DEFAULT_FONT = loadFont("font.otf", 20);
            BOLD_FONT = loadFont("font_bold.otf", 20);
            CODE_FONT = loadFont("font_spec.ttf", 20);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Font loadFont(String fontFileName, double size) throws IOException {
        return Font.loadFont(Settings.class.getResourceAsStream(fontFileName), size);
    }

}
