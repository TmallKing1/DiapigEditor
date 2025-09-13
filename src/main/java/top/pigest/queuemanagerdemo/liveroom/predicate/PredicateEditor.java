package top.pigest.queuemanagerdemo.liveroom.predicate;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import top.pigest.queuemanagerdemo.control.DataEditor;
import top.pigest.queuemanagerdemo.control.NamedPage;

public class PredicateEditor extends BorderPane implements NamedPage, DataEditor {
    private Pane parentPage;

    private final String from;
    public PredicateEditor(String from) {
        this.from = from;
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
        return "谓词编辑：" + from;
    }

    @Override
    public void save() {

    }
}
