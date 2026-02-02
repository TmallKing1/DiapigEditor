package top.pigest.dialogeditor.comment.ui;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.dialogeditor.DialogEditor;
import top.pigest.dialogeditor.Settings;
import top.pigest.dialogeditor.comment.CommentApi;
import top.pigest.dialogeditor.comment.PlayerComment;
import top.pigest.dialogeditor.comment.PlayerCommentResponse;
import top.pigest.dialogeditor.control.*;
import top.pigest.dialogeditor.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CommentGetterPage extends BorderPane implements NamedPage, ChildPage, SizeChangeListener {
    private Pane parentPage;

    private final HBox top;
    private final ScrollPane main;
    private final VBox inner;
    private final HBox bottom;

    private final JFXTextField guidField;
    private final JFXRadioButton all;
    private final JFXRadioButton onlyRecommend;
    private final JFXRadioButton noRecommend;
    private final JFXCheckBox filterDefaultField;
    private final IntegerModifier maxPage;
    private final QMButton start;
    private final QMButton export;

    private String guid;
    private List<PlayerComment> playerComments;
    private int filterType = 0;
    private int pageMax = 1;
    private boolean filterDefault = true;
    private FileChooser fileChooser;
    private File lastDirectory = new File(System.getProperty("user.home") + "/Documents");

    public CommentGetterPage() {
        top = new HBox(20);
        top.setAlignment(Pos.CENTER);

        VBox topLeft = new VBox(10);
        topLeft.setAlignment(Pos.CENTER);

        guidField = new QMTextField("关卡GUID");

        HBox selectButtons = new HBox(10);
        selectButtons.setAlignment(Pos.CENTER);
        ToggleGroup toggleGroup = new ToggleGroup();
        all = new JFXRadioButton("全部");
        onlyRecommend = new JFXRadioButton("仅推荐");
        noRecommend = new JFXRadioButton("仅欠佳");
        all.setFont(Settings.DEFAULT_FONT);
        all.setTextFill(Color.WHITE);
        all.setToggleGroup(toggleGroup);
        onlyRecommend.setFont(Settings.DEFAULT_FONT);
        onlyRecommend.setTextFill(Color.WHITE);
        onlyRecommend.setToggleGroup(toggleGroup);
        noRecommend.setFont(Settings.DEFAULT_FONT);
        noRecommend.setTextFill(Color.WHITE);
        noRecommend.setToggleGroup(toggleGroup);
        all.setSelected(true);
        selectButtons.getChildren().addAll(all, onlyRecommend, noRecommend);

        topLeft.getChildren().addAll(guidField, selectButtons);
        top.getChildren().add(topLeft);

        VBox topRight = new VBox(10);
        topRight.setAlignment(Pos.CENTER);

        HBox topRightTop = new HBox(10);
        topRightTop.setAlignment(Pos.CENTER);

        Text text = Utils.createText("最大页数");
        maxPage = new IntegerModifier(5, 1, 100);
        topRightTop.getChildren().addAll(text, maxPage);

        HBox topRightDown = new HBox(10);
        topRightDown.setAlignment(Pos.CENTER);

        filterDefaultField = new JFXCheckBox("过滤默认评价");
        filterDefaultField.setSelected(true);
        filterDefaultField.setFont(Settings.DEFAULT_FONT);
        filterDefaultField.setTextFill(Color.WHITE);

        start = new QMButton("开始抓取");
        start.setGraphic(new WhiteFontIcon("fas-paper-plane"));
        start.setOnAction(event -> start());

        topRightDown.getChildren().addAll(filterDefaultField, start);

        topRight.getChildren().addAll(topRightTop, topRightDown);
        top.getChildren().add(topRight);

        BorderPane.setAlignment(top, Pos.CENTER);
        BorderPane.setMargin(top, new Insets(10));
        this.setTop(top);

        inner = new VBox();
        inner.setStyle("-fx-background-color: #26282b");

        main = new ScrollPane();
        main.getStylesheets().add(Objects.requireNonNull(DialogEditor.class.getResource("css/scrollbar.css")).toExternalForm());
        main.setFitToWidth(true);
        main.setFitToHeight(true);
        main.setContent(inner);

        BorderPane.setAlignment(main, Pos.CENTER);
        this.setCenter(main);

        bottom = new HBox();
        bottom.setAlignment(Pos.CENTER);

        export = new QMButton("导出为 CSV", "#58135E");
        export.setGraphic(new WhiteFontIcon("fas-file-export"));
        export.disable(true);
        export.setOnAction(event -> {
            if (fileChooser == null) {
                fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV 文件", "*.csv"));
            }
            fileChooser.setInitialDirectory(lastDirectory);
            fileChooser.setTitle("导出评论为 CSV");
            File result = fileChooser.showSaveDialog(DialogEditor.INSTANCE.getPrimaryStage());
            if (result != null) {
                try {
                    if (!result.exists()) {
                        if (!result.createNewFile()) {
                            throw new RuntimeException();
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("发送者,IP属地,发送时间,是否推荐,评论内容\n");
                    for (PlayerComment playerComment : this.playerComments) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                        sb.append("%s,%s,%s,%s,%s\n".formatted(optCsvString(playerComment.uName()), playerComment.clientIp(),
                                formatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(playerComment.timestamp()), ZoneId.systemDefault())),
                                playerComment.isRecommend() ? "是" : "否",
                                optCsvString(playerComment.comment())));
                    }
                    Files.writeString(result.toPath(), "\ufeff" + sb, StandardCharsets.UTF_8);
                    lastDirectory = result.getParentFile();
                    Platform.runLater(() -> Utils.showDialogMessage("成功导出文件", false, DialogEditor.INSTANCE.getMainScene().getRootDrawer()));
                } catch (FileNotFoundException e) {
                    Platform.runLater(() -> Utils.showDialogMessage("文件不存在", true, DialogEditor.INSTANCE.getMainScene().getRootDrawer()));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> Utils.showDialogMessage("导出失败", true, DialogEditor.INSTANCE.getMainScene().getRootDrawer()));
                }
            }
        });

        bottom.getChildren().addAll(export);

        BorderPane.setAlignment(bottom, Pos.CENTER);
        BorderPane.setMargin(bottom, new Insets(10));
        this.setBottom(bottom);
    }

    public static String optCsvString(String cell) {
        boolean needsQuotes = cell.contains(",")
                              || cell.contains("\"")
                              || cell.contains("\n")
                              || cell.contains("\r")
                              || cell.startsWith(" ")
                              || cell.endsWith(" ")
                              || cell.startsWith("\t")
                              || cell.endsWith("\t");

        if (needsQuotes) {
            cell = cell.replace("\"", "\"\"");
            return "\"" + cell + "\"";
        } else {
            return cell;
        }
    }

    public void start() {
        this.start.disable(true);
        this.inner.getChildren().clear();
        guid = guidField.getText();
        filterType = all.isSelected() ? 0 : onlyRecommend.isSelected() ? 1 : 2;
        pageMax = maxPage.getValue();
        filterDefault = filterDefaultField.isSelected();
        CompletableFuture.supplyAsync(() -> {
            List<PlayerComment> comments = new ArrayList<>();
            PlayerCommentResponse response = new PlayerCommentResponse(true, "", "SORT_TYPE_HOT", List.of());
            for (int i = 1; i <= pageMax && response.isHasMore(); i++) {
                response = CommentApi.getPlayerComment(guid, response.getNext(), response.getSortType());
                Iterator<PlayerComment> it = response.getPlayerComments().iterator();
                while (it.hasNext()) {
                    PlayerComment playerComment = it.next();
                    switch (filterType) {
                        case 1 -> {
                            if (!playerComment.isRecommend()) {
                                it.remove();
                                continue;
                            }
                        }
                        case 2 -> {
                            if (playerComment.isRecommend()) {
                                it.remove();
                                continue;
                            }
                        }
                    }
                    if (filterDefault) {
                        if ((playerComment.isRecommend() && playerComment.comment().equals("非常优秀的奇域，推荐大家游玩~"))
                            || (!playerComment.isRecommend() && playerComment.comment().equals("奇域仍有待打磨，期待作者的更新~"))) {
                            it.remove();
                        }
                    }
                }
                comments.addAll(response.getPlayerComments());
                PlayerCommentResponse finalResponse = response;
                Platform.runLater(() ->
                        finalResponse.getPlayerComments().forEach(comment -> this.inner.getChildren().add(new CommentView(comment)))
                );
            }
            return comments;
        }).whenComplete((comments, throwable) -> {
            this.playerComments = comments;
            if (throwable != null) {
                Platform.runLater(() -> {
                            Utils.showDialogMessage(throwable.getCause().getMessage(), true, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                            export.disable(true);
                            Platform.runLater(() -> this.start.disable(false));
                        }
                );
                return;
            }
            Platform.runLater(() -> {
                        if (this.filterType == 0) {
                            long count = playerComments.stream().filter(PlayerComment::isRecommend).count();
                            Utils.showDialogMessage("获取成功，共 %s 条好评，%s 条差评".formatted(count, playerComments.size() - count), false, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                        } else {
                            Utils.showDialogMessage("获取成功，共 %s 条评论".formatted(playerComments.size()), false, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                        }
                        export.disable(playerComments.isEmpty());
                        Platform.runLater(() -> this.start.disable(false));
                    }
            );
        });
    }

    @Override
    public Pane getParentPage() {
        return parentPage;
    }

    @Override
    public void setParentPage(Pane parentPage) {
        this.parentPage = parentPage;
    }

    @Override
    public String getName() {
        return "评论抓取";
    }

    @Override
    public void onWidthChanged(int width) {
        Platform.runLater(() -> this.inner.getChildren().forEach(comment -> {
            if (comment instanceof CommentView view) {
                view.onResize();
            }
        }));
    }

    @Override
    public void onHeightChanged(int height) {

    }

    public static class CommentView extends VBox {

        private final Text content;

        public CommentView(PlayerComment comment) {
            super(5);
            this.setAlignment(Pos.CENTER);
            this.setFillWidth(Boolean.TRUE);
            HBox meta = new HBox(15);
            meta.setAlignment(Pos.CENTER_LEFT);

            HBox recommendation = new HBox(5);
            recommendation.setAlignment(Pos.CENTER);

            FontIcon icon = new FontIcon(comment.isRecommend() ? "fas-thumbs-up" : "fas-thumbs-down");
            icon.setFill(comment.isRecommend() ? Color.valueOf("#00C2AE") : Color.LIGHTGRAY);
            icon.setIconSize(16);
            Text rc = Utils.createText(comment.isRecommend() ? "推荐" : "欠佳");
            rc.setFont(Settings.BOLD_FONT);
            rc.setFill(comment.isRecommend() ? Color.valueOf("#00C2AE") : Color.LIGHTGRAY);
            recommendation.getChildren().addAll(icon, rc);

            Text sender = Utils.createText(comment.uName());
            sender.setFont(Settings.BOLD_FONT);
            Text ip = Utils.createText("IP属地：" + comment.clientIp());
            ip.setFill(Color.LIGHTGRAY);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("发布于yyyy年MM月dd日 HH:mm:ss");
            Text date = Utils.createText(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(comment.timestamp()), ZoneId.systemDefault())));
            date.setFill(Color.LIGHTGRAY);
            meta.getChildren().addAll(recommendation, sender, ip, date);

            content = Utils.createText(comment.comment().replaceAll("\t+", "\n"));
            content.setTextAlignment(TextAlignment.LEFT);
            onResize();

            this.getChildren().addAll(meta, content);
            this.setBorder(new Border(MultiMenuProvider.DEFAULT_BORDER_STROKE));
            this.setPadding(new Insets(5, 10, 5, 10));
        }

        private void onResize() {
            content.setWrappingWidth(DialogEditor.INSTANCE.getMainScene().getWidth() - 50);
        }
    }
}
