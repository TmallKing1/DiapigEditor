package top.pigest.queuemanagerdemo.liveroom.predicate;

import com.google.gson.JsonElement;
import javafx.scene.Node;
import top.pigest.queuemanagerdemo.liveroom.data.User;

/**
 * 用户谓词，用于判断一个 {@link User} 对象是否满足谓词指定的条件
 * 可直接作为函数式接口使用，也可使用它的实现类
 */
@FunctionalInterface
public interface UserPredicate {
    boolean test(User user);

    default String getName() {
        return "用户谓词";
    }

    default Node getEditNode() {
        throw new UnsupportedOperationException();
    }

    default JsonElement toJson() {
        throw new UnsupportedOperationException();
    }
}
