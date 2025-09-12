package top.pigest.queuemanagerdemo.misc.ui;

import javafx.scene.layout.Pane;
import top.pigest.queuemanagerdemo.control.MultiMenuProvider;
import top.pigest.queuemanagerdemo.control.NamedPage;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class FansListPage extends MultiMenuProvider<Pane> implements NamedPage {

    private Pane lightedList;
    private Pane notGuardList;

    public FansListPage() {
        this.setInnerContainer(this.getMenus().entrySet().iterator().next().getValue().get());
    }

    public Pane initC0() {
        FansContainer fansContainer = new FansContainer("c0", 5, 1);
        return fansContainer.build();
    }

    public Pane getC0() {
        return lightedList == null ? (lightedList = initC0()) : lightedList;
    }

    public Pane initC1() {
        FansContainer fansContainer = new FansContainer("c1", 5, 2);
        return fansContainer.build();
    }

    public Pane getC1() {
        return notGuardList == null ? (notGuardList = initC1()) : notGuardList;
    }

    @Override
    public LinkedHashMap<String, Supplier<Pane>> getMenus() {
        LinkedHashMap<String, Supplier<Pane>> map = new LinkedHashMap<>();
        map.put("已点亮灯牌成员", this::getC0);
        // B站API变化，无法获取到所有20级及以下成员
        // map.put("20级及以下成员", this::getC1);
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
        return "粉丝团";
    }
}
