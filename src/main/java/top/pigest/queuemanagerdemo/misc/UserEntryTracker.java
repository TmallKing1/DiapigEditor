package top.pigest.queuemanagerdemo.misc;

import javafx.collections.ObservableList;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.liveroom.data.event.InteractWord;
import top.pigest.queuemanagerdemo.liveroom.event.EventHandler;
import top.pigest.queuemanagerdemo.liveroom.event.InteractEvent;
import top.pigest.queuemanagerdemo.util.ArrayObservableList;

public class UserEntryTracker {
    public static UserEntryTracker INSTANCE;

    private ObservableList<User> users;
    private TrackMethod trackMethod;
    public UserEntryTracker() {
        this.users = new ArrayObservableList<>();
        this.trackMethod = Settings.getToolboxSettings().trackMethod;
        InteractEvent.INSTANCE.addHandler(new EventHandler<>("user_entry_tracker", interactWord -> {
            if (interactWord.messageType() == InteractWord.MessageType.ENTER && trackMethod.test(interactWord.user())) {
                users.remove(interactWord.user());
                users.add(interactWord.user());
                int overflow = users.size() - Settings.getToolboxSettings().maxTrackCount;
                while (overflow > 0) {
                    users.removeFirst();
                    overflow--;
                }
            }
        }));
    }

    public static void enable() {
        INSTANCE = new UserEntryTracker();
    }

    public static void disable() {
        InteractEvent.INSTANCE.removeHandler("user_entry_tracker");
        INSTANCE.users = null;
        INSTANCE = null;
    }

    public void setTrackMethod(TrackMethod trackMethod) {
        this.trackMethod = trackMethod;
    }

    public ObservableList<User> getUsers() {
        return users;
    }
}
