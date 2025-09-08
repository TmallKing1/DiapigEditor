package top.pigest.queuemanagerdemo.window.misc;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.util.PagedContainerFactory;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.window.main.ChildPage;
import top.pigest.queuemanagerdemo.window.main.NamedPage;

import java.util.concurrent.CompletableFuture;

public class WebStartLivePage extends BorderPane implements NamedPage, ChildPage {
    private JsonObject areas;
    private Pane parentPage;
    private JFXTextField link;
    private JFXTextField token;
    public WebStartLivePage() {
        PagedContainerFactory factory = new PagedContainerFactory("c0");
        factory.addControl("直播分区", Utils.make(new QMButton("正在加载", "#1a8bcc"), button -> {
            button.setPrefWidth(300);
            button.disable(true);
        }));

        CompletableFuture.runAsync(this::updateInfo);
    }

    private void updateInfo() {
        // 获取直播间分区列表

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
        return "快捷开播";
    }
}
