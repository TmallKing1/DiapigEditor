package top.pigest.queuemanagerdemo.misc;

import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.liveroom.predicate.UserPredicate;

public enum TrackMethod {
    ALL("追踪所有用户", user -> true),
    POOR("追踪没有荣耀等级的用户", user -> user.getHonor() < 1),
    FANS("追踪用户名带有「粉」字的用户", user -> user.getUsername().contains("粉")),
    NUMBER("追踪用户名带有数字的用户", user -> user.getUsername().matches(".*\\d.*"));
    private final String name;
    private final UserPredicate userPredicate;

    TrackMethod(String name, UserPredicate userPredicate) {
        this.name = name;
        this.userPredicate = userPredicate;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public boolean test(User user) {
        return userPredicate.test(user);
    }
}
