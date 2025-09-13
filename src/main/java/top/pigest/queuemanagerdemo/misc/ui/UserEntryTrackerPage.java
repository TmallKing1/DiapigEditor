package top.pigest.queuemanagerdemo.misc.ui;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXToggleButton;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.IntegerModifier;
import top.pigest.queuemanagerdemo.control.MultiMenuProvider;
import top.pigest.queuemanagerdemo.control.NamedPage;
import top.pigest.queuemanagerdemo.control.PagedContainerFactory;
import top.pigest.queuemanagerdemo.misc.TrackMethod;
import top.pigest.queuemanagerdemo.misc.UserEntryTracker;
import top.pigest.queuemanagerdemo.util.ArrayObservableList;
import top.pigest.queuemanagerdemo.util.Utils;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Supplier;

public class UserEntryTrackerPage extends MultiMenuProvider<Pane> implements NamedPage {
    private Pane settings;
    private Pane userDisplay;

    public UserEntryTrackerPage() {
        this.setInnerContainer(this.getMenus().entrySet().iterator().next().getValue().get());
    }

    private Pane initC0() {
        return new PagedContainerFactory("c0")
                .addControl("启用追踪", Utils.make(new JFXToggleButton(), button -> {
                    button.setSize(8);
                    button.setSelected(Settings.getToolboxSettings().trackEnabled);
                    button.setDisableVisualFocus(true);
                    button.setOnAction(event -> {
                        Settings.getToolboxSettings().setTrackEnabled(button.isSelected());
                        if (button.isSelected()) {
                            UserEntryTracker.enable();
                        } else {
                            UserEntryTracker.disable();
                        }
                        this.userDisplay = initC1();
                    });
                }))
                .addControl("追踪过滤器", Utils.make(new JFXComboBox<TrackMethod>(), comboBox -> {
                    comboBox.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/combobox.css")).toExternalForm());
                    comboBox.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/scrollbar.css")).toExternalForm());
                    comboBox.setPrefWidth(500);
                    comboBox.setValue(Settings.getToolboxSettings().trackMethod);
                    comboBox.setCellFactory(new Callback<>() {
                        @Override
                        public ListCell<TrackMethod> call(ListView<TrackMethod> param) {
                            return new ListCell<>() {
                                @Override
                                protected void updateItem(TrackMethod item, boolean empty) {
                                    super.updateItem(item, empty);
                                    this.setFont(Settings.DEFAULT_FONT);
                                    if (item != null && !empty) {
                                        this.setText(item.toString());
                                    }
                                }
                            };
                        }
                    });
                    comboBox.getItems().addAll(TrackMethod.values());
                    comboBox.getButtonCell().setFont(Settings.DEFAULT_FONT);
                    comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        Settings.getToolboxSettings().setTrackMethod(newValue);
                        Utils.onPresent(UserEntryTracker.INSTANCE, t -> t.setTrackMethod(newValue));
                    });
                }))
                .addControl("最大存储个数", Utils.make(new IntegerModifier(Settings.getToolboxSettings().maxTrackCount, 1, 1, Integer.MAX_VALUE), control -> control.setOnValueSet(i -> Settings.getToolboxSettings().setMaxTrackCount(i))))
                .build();
    }

    private Pane getC0() {
        return settings == null ? (settings = initC0()) : settings;
    }

    private Pane initC1() {
        return new TrackedUserContainer("c1", UserEntryTracker.INSTANCE != null ? UserEntryTracker.INSTANCE.getUsers() : new ArrayObservableList<>()).build();
    }

    private Pane getC1() {
        return userDisplay == null ? (userDisplay = initC1()) : userDisplay;
    }

    @Override
    public LinkedHashMap<String, Supplier<Pane>> getMenus() {
        LinkedHashMap<String, Supplier<Pane>> menus = new LinkedHashMap<>();
        menus.put("追踪设置", this::getC0);
        menus.put("追踪用户列表", this::getC1);
        return menus;
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
        return "进房追踪";
    }
}
