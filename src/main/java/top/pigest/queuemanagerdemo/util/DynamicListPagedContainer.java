package top.pigest.queuemanagerdemo.util;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class DynamicListPagedContainer<T> extends ListPagedContainer<T> {
    private int currentItemPage = 0;
    private boolean lockNext = false;
    private boolean isEnd;

    public DynamicListPagedContainer(String id, int maxPerPage) {
        this(id, new ArrayList<>(), maxPerPage);
    }

    public DynamicListPagedContainer(String id, List<T> items, int maxPerPage) {
        this(id, items, maxPerPage, LOADING_SUPPLIER);
    }

    public DynamicListPagedContainer(String id, List<T> items, int maxPerPage, Supplier<Node> emptySupplier) {
        super(id, items, maxPerPage, false, emptySupplier);
    }

    @Override
    public void update() {
        left.disable(!pages.hasPrev());
        right.disable(!pages.hasNext() && isEnd);
        text.setText("第 %s 页".formatted(this.pages.getIndex() + 1));
    }

    public int getCurrentItemPage() {
        return currentItemPage;
    }

    public void prepareReadNextPage() {
        if (isEnd) {
            return;
        }
        if (this.pages.getIndex() >= pages.size() - 2) {
            readNextPage();
        }
    }

    public void readNextPage() {
        currentItemPage++;
        if (!lockNext) {
            this.lockNext = true;
            CompletableFuture.supplyAsync(() -> this.getNextItems(currentItemPage))
                    .thenAccept(l -> {
                        if (l.isEmpty()) {
                            isEnd = true;
                            this.setEmptySupplier(DEFAULT_EMPTY_SUPPLIER);
                            this.update();
                            return;
                        }
                        Platform.runLater(() -> {
                            int index = this.pages.getIndex();
                            if (this.getItems().isEmpty()) {
                                this.pages.clear();
                                this.addPage();
                            }
                            this.getItems().addAll(l);
                            int f = this.pages.getLast().getChildren().size();
                            for (T item : l) {
                                if (f >= maxPerPage) {
                                    this.addPage();
                                    f = 0;
                                }
                                this.addNode(this.getNode(item));
                                f++;
                            }
                            this.pages.setIndex(Math.min(index, this.pages.size() - 1));
                            this.current.setCenter(this.pages.current());
                            this.update();
                            lockNext = false;
                        });
                    });
        }
    }

    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public BorderPane build() {
        BorderPane borderPane = super.build();
        prepareReadNextPage();
        return borderPane;
    }

    @Override
    public void rightAction(ActionEvent event) {
        prepareReadNextPage();
        super.rightAction(event);
    }

    public abstract List<T> getNextItems(int page);
}
