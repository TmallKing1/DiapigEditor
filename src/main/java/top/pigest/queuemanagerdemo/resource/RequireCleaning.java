package top.pigest.queuemanagerdemo.resource;

import javafx.scene.Node;

/**
 * 用于会用做主窗口主容器页面的类，会在主容器更换之前调用 {@link RequireCleaning#clean()} 方法
 * @see top.pigest.queuemanagerdemo.main.MainScene#setMainContainer(Node, String)
 */
public interface RequireCleaning {
    void clean();
}
