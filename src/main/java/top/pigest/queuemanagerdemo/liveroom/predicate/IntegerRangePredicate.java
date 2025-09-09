package top.pigest.queuemanagerdemo.liveroom.predicate;

import top.pigest.queuemanagerdemo.liveroom.data.User;

public abstract class IntegerRangePredicate implements UserPredicate {
    @Override
    public boolean test(User user) {
        return false;
    }
}
