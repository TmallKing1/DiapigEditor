package top.pigest.queuemanagerdemo.misc.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import top.pigest.queuemanagerdemo.liveroom.data.FansMedal;
import top.pigest.queuemanagerdemo.liveroom.data.GuardInfo;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.control.DynamicListPagedContainer;
import top.pigest.queuemanagerdemo.control.MultiMenuProvider;

import java.util.ArrayList;
import java.util.List;

public class GuardContainer extends DynamicListPagedContainer<User> {
    public GuardContainer(String id, int maxPerPage) {
        super(id, maxPerPage);
    }

    @Override
    public List<User> getNextItems(int page) {
        try {
            List<Long> uids = new ArrayList<>();
            List<GuardInfo> guardInfoA = LiveRoomApi.getGuardsWithExpireDate(page);
            for (GuardInfo guardInfo : guardInfoA) {
                uids.add(guardInfo.getUid());
            }
            List<User> users = LiveRoomApi.getUserBriefInfo(uids);
            for (int i = 0; i < users.size(); i++) {
                GuardInfo guardInfo = guardInfoA.get(i);
                FansMedal fansMedal = LiveRoomApi.getFansUInfoMedal(guardInfo.getUid());
                users.get(i).setFansMedal(fansMedal).setGuardInfo(guardInfo);
            }
            return users;
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
