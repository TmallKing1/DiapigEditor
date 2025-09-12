package top.pigest.queuemanagerdemo.misc;

import top.pigest.queuemanagerdemo.misc.ui.*;

import java.util.ArrayList;
import java.util.List;

public final class MiscRegistryManager {
    private static final List<MiscFunction> REGISTRIES = new ArrayList<>();

    static {
        register(new MiscFunction("模拟弹幕测试", CommandTestPage::new, "#003847", "fas-code"));
        register(new MiscFunction("激励抢码助手", () -> null, "#802E5A", "fas-mouse"));
        register(new MiscFunction("代肝账号记录", () -> null, "#701617", "fas-exchange-alt"));
        register(new MiscFunction("快捷网页开播", WebStartLivePage::new, "#284CB8", "fas-video"));
        register(new MiscFunction("进房用户追踪", () -> null, "#6E6E6E", "fas-robot"));
        register(new MiscFunction("自动弹幕回复", () -> null, "#7D7E28", "fas-reply"));
        register(new MiscFunction("粉丝团成员表", FansListPage::new, "#E774D2", "far-heart"));
        register(new MiscFunction("大航海船员表", GuardListPage::new, "#F08650", "fas-anchor"));
        register(new MiscFunction("二维码放大器", () -> null, "#3D3F70", "fas-qrcode"));
        register(new MiscFunction("粉丝勋章查询", MedalQueryPage::new, "#8B57E7", "far-eye"));
        register(new MiscFunction("盲盒概率查询", BlindChancePage::new, "#3B706F", "fas-percentage"));
        register(new MiscFunction("礼物图片下载", () -> null, "#00704E", "fas-gift"));
    }

    public static void register(MiscFunction miscFunction) {
        REGISTRIES.add(miscFunction);
    }

    public static List<MiscFunction> getRegistries() {
        return REGISTRIES;
    }
}
