package top.pigest.queuemanagerdemo.misc.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.control.DynamicListPagedContainer;
import top.pigest.queuemanagerdemo.control.MultiMenuProvider;

import java.util.ArrayList;
import java.util.List;

public class FansContainer extends DynamicListPagedContainer<User> {

    private final int rankType;
    public FansContainer(String id, int maxPerPage, int rankType) {
        super(id, maxPerPage);
        this.rankType = rankType;
    }

    @Override
    public List<User> getNextItems(int page) {
        try {
            return LiveRoomApi.getFans(page, this.rankType);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Node getNode(User item) {
        BorderPane borderPane = User.userNode(item);

        borderPane.setBorder(new Border(MultiMenuProvider.DEFAULT_BORDER_STROKE));
        borderPane.setPadding(new Insets(5, 10, 5, 10));
        return borderPane;
    }

}
