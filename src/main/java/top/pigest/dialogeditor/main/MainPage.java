package top.pigest.dialogeditor.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import top.pigest.dialogeditor.control.NamedPage;
import top.pigest.dialogeditor.dialog.ui.DialogEditorPage;
import top.pigest.dialogeditor.richtext.RichTextEditorPage;
import top.pigest.dialogeditor.util.Utils;
import top.pigest.dialogeditor.control.QMButton;

public class MainPage extends BorderPane implements NamedPage {

    public MainPage(MainScene parent) {
        GridPane functions = Utils.make(new GridPane(10, 10), gridPane -> gridPane.setAlignment(Pos.CENTER));
        functions.add(parent.createMainFunctionButton("#ae5220", "#cf9d81", "对话编辑", "far-object-ungroup", DialogEditorPage::new), 0, 0);
        functions.add(parent.createMainFunctionButton("#357c56", "#73be95", "富文本编辑", "fas-star-of-david", RichTextEditorPage::new), 1, 0);
        functions.add(parent.createMainFunctionButton("#932121", "#c15757", "我帮你种个铁花洒", "fas-wine-bottle", null), 0, 1);
        functions.add(parent.createMainFunctionButton("#0d608f", "#1a8bcc", "我吃一口尝尝咸淡", "fas-hamburger", null), 1, 1);
        this.setCenter(functions);
        QMButton exitButton = Utils.make(new QMButton("退出", "#bb5555"), button -> {
            button.setPrefSize(200, 40);
            button.setOnAction(actionEvent -> System.exit(0));
            BorderPane.setAlignment(button, Pos.CENTER);
            BorderPane.setMargin(button, new Insets(0, 0, 30, 0));
        });
        this.setBottom(exitButton);
    }

    @Override
    public String getName() {
        return "主页";
    }
}
