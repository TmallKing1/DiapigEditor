package top.pigest.dialogeditor.comment;

import java.util.List;

public class PlayerCommentResponse {
    private final boolean hasMore;
    private final String next;
    private final String sortType;
    private final List<PlayerComment> playerComments;
    public PlayerCommentResponse(boolean hasMore, String next, String sortType, List<PlayerComment> playerComments) {
        this.hasMore = hasMore;
        this.next = next;
        this.sortType = sortType;
        this.playerComments = playerComments;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public String getNext() {
        return next;
    }

    public String getSortType() {
        return sortType;
    }

    public List<PlayerComment> getPlayerComments() {
        return playerComments;
    }
}
