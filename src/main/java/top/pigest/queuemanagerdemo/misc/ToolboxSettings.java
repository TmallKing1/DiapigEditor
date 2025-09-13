package top.pigest.queuemanagerdemo.misc;

import com.google.gson.annotations.SerializedName;
import top.pigest.queuemanagerdemo.Settings;

public class ToolboxSettings {
    @SerializedName("track_enabled")
    public boolean trackEnabled = false;
    @SerializedName("track_method")
    public TrackMethod trackMethod = TrackMethod.ALL;
    @SerializedName("max_track_count")
    public int maxTrackCount = 50;

    public void setTrackEnabled(boolean trackEnabled) {
        this.trackEnabled = trackEnabled;
        Settings.saveSettings();
    }

    public void setTrackMethod(TrackMethod trackMethod) {
        this.trackMethod = trackMethod;
        Settings.saveSettings();
    }

    public void setMaxTrackCount(int maxTrackCount) {
        this.maxTrackCount = maxTrackCount;
        Settings.saveSettings();
    }
}
