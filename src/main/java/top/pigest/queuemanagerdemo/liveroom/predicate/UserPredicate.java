package top.pigest.queuemanagerdemo.liveroom.predicate;

import javafx.scene.Node;
import top.pigest.queuemanagerdemo.liveroom.User;

/**
 * 用户谓词，用于判断一个 {@link User} 对象是否满足谓词指定的条件
 * 可直接作为函数式接口使用，也可使用它的实现类
 */
@FunctionalInterface
public interface UserPredicate {
    boolean test(User user);

    /**
     * 获得查看和编辑该谓词时的 UI 节点
     * 为了使该接口成为一个 {@link FunctionalInterface} 编写了默认方法体<br>
     * 实现接口时需要重新编写获得节点的实际代码
     * @return 用于查看和编辑该谓词的 UI 节点
     */
    default Node getNode() {
        throw new UnsupportedOperationException();
    }
}
