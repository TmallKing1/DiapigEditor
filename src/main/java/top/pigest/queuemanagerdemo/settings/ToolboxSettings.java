package top.pigest.queuemanagerdemo.settings;

import com.google.gson.annotations.SerializedName;
import top.pigest.queuemanagerdemo.Settings;

public class ToolboxSettings {
    @SerializedName("live_area")
    public int liveArea;


    public void setLiveArea(int liveArea) {
        this.liveArea = liveArea;
        Settings.saveSettings();
    }
}
