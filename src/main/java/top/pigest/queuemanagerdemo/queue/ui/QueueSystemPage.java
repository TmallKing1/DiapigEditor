package top.pigest.queuemanagerdemo.queue.ui;

import javafx.scene.layout.Pane;
import top.pigest.queuemanagerdemo.control.MultiMenuProvider;
import top.pigest.queuemanagerdemo.control.NamedPage;
import top.pigest.queuemanagerdemo.control.PagedContainerFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class QueueSystemPage extends MultiMenuProvider<Pane> implements NamedPage {
    private Pane configs;
    private Pane settings;

    private Pane initC0() {
        return new PagedContainerFactory("c0").build();
    }

    private Pane getC0() {
        return configs == null ? (configs = initC0()) : configs;
    }

    private Pane initC1() {
        return new PagedContainerFactory("c1").build();
    }

    private Pane getC1() {
        return settings == null ? (settings = initC1()) : settings;
    }

    @Override
    public LinkedHashMap<String, Supplier<Pane>> getMenus() {
        LinkedHashMap<String, Supplier<Pane>> map = new LinkedHashMap<>();
        map.put("规则列表", this::getC0);
        map.put("全局设置", this::getC1);
        return map;
    }

    @Override
    public int getMenuIndex(Pane innerContainer) {
        String id = innerContainer.getId();
        if (id == null) {
            return -1;
        }
        return id.charAt(1) - '0';
    }

    @Override
    public String getName() {
        return "排队系统";
    }
}
