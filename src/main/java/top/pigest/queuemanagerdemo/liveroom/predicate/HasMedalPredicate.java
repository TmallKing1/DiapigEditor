package top.pigest.queuemanagerdemo.liveroom.predicate;

import top.pigest.queuemanagerdemo.liveroom.User;

public class HasMedalPredicate implements UserPredicate {
    @Override
    public boolean test(User user) {
        return false;
    }
}
