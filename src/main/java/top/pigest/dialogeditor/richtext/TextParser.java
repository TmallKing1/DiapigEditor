package top.pigest.dialogeditor.richtext;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import top.pigest.dialogeditor.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

    // 错误处理接口
    public interface ErrorHandler {
        void onError(String message);
    }

    /**
     * 将字符串解析为Text列表
     * @param input 输入字符串
     * @param errorHandler 错误处理器
     * @return Text列表
     */
    public static List<Text> parseText(String input, ErrorHandler errorHandler) {
        List<Text> textList = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            return textList;
        }

        // 检查是否存在非法换行符（不是\\n）
        if (input.contains("\n")) {
            if (errorHandler != null) {
                errorHandler.onError("文本中包含非法换行符，请使用 \\\\n 表示换行");
            }
            return textList;
        }

        // 将\\n替换为实际的换行符
        input = input.replace("\\n", "\n");

        // 解析文本
        parseTextRecursive(input, Settings.DEFAULT_FONT, Color.WHITE, false, false, textList, errorHandler, 0);

        return textList;
    }

    /**
     * 递归解析文本
     */
    private static void parseTextRecursive(String input, Font currentFont, Color currentColor,
                                           boolean isBold, boolean isItalic,
                                           List<Text> textList, ErrorHandler errorHandler,
                                           int depth) {
        if (depth > 100) { // 防止无限递归
            if (errorHandler != null) {
                errorHandler.onError("标签嵌套过深，可能存在未闭合的标签");
            }
            return;
        }

        if (input.isEmpty()) {
            return;
        }

        // 查找第一个标签开始位置
        int tagStart = input.indexOf('<');

        if (tagStart == -1) {
            // 没有标签，直接添加文本
            addTextSegment(input, currentFont, currentColor, isBold, isItalic, textList);
            return;
        }

        // 添加标签前的文本
        if (tagStart > 0) {
            String textBeforeTag = input.substring(0, tagStart);
            addTextSegment(textBeforeTag, currentFont, currentColor, isBold, isItalic, textList);
        }

        // 查找标签结束位置
        int tagEnd = input.indexOf('>', tagStart);
        if (tagEnd == -1) {
            // 标签未闭合
            if (errorHandler != null) {
                errorHandler.onError("存在未闭合的标签: " + input.substring(tagStart));
            }
            // 添加剩余文本
            addTextSegment(input.substring(tagStart), currentFont, currentColor, isBold, isItalic, textList);
            return;
        }

        String tag = input.substring(tagStart, tagEnd + 1);
        String remainingText = input.substring(tagEnd + 1);

        // 处理开始标签
        if (isStartTag(tag)) {
            String tagName = getTagName(tag);

            // 查找对应的结束标签，考虑嵌套情况
            String endTag = "</" + tagName + ">";
            int endTagPos = findMatchingEndTag(remainingText, tagName, endTag);

            if (endTagPos == -1) {
                // 没有找到结束标签
                if (errorHandler != null) {
                    errorHandler.onError("标签 " + tag + " 没有对应的结束标签");
                }
                // 添加剩余文本
                addTextSegment(remainingText, currentFont, currentColor, isBold, isItalic, textList);
                return;
            }

            // 提取标签内的内容
            String content = remainingText.substring(0, endTagPos);
            String afterContent = remainingText.substring(endTagPos + endTag.length());

            // 根据标签类型处理
            switch (tagName) {
                case "color":
                    handleColorTag(content, tag, currentFont, currentColor, isBold, isItalic, textList, errorHandler, depth);
                    break;
                case "b":
                    handleBoldTag(content, currentFont, currentColor, isItalic, textList, errorHandler, depth);
                    break;
                case "i":
                    handleItalicTag(content, currentFont, currentColor, isBold, textList, errorHandler, depth);
                    break;
                default:
                    if (errorHandler != null) {
                        errorHandler.onError("未知的标签: " + tagName);
                    }
                    // 将标签内容作为普通文本处理
                    addTextSegment(content, currentFont, currentColor, isBold, isItalic, textList);
                    break;
            }

            // 继续处理剩余文本
            parseTextRecursive(afterContent, currentFont, currentColor, isBold, isItalic,
                    textList, errorHandler, depth + 1);

        } else if (isEndTag(tag)) {
            // 遇到结束标签但没有对应的开始标签
            if (errorHandler != null) {
                errorHandler.onError("存在未匹配的结束标签: " + tag);
            }
            // 继续处理剩余文本
            parseTextRecursive(remainingText, currentFont, currentColor, isBold, isItalic,
                    textList, errorHandler, depth + 1);
        } else {
            // 无效的标签，作为普通文本处理
            addTextSegment(tag, currentFont, currentColor, isBold, isItalic, textList);
            parseTextRecursive(remainingText, currentFont, currentColor, isBold, isItalic,
                    textList, errorHandler, depth + 1);
        }
    }

    /**
     * 查找匹配的结束标签位置，考虑嵌套情况
     */
    private static int findMatchingEndTag(String text, String tagName, String endTag) {
        int currentPos = 0;
        int nestingLevel = 0;

        while (currentPos < text.length()) {
            int nextStartTag = text.indexOf("<" + tagName, currentPos);
            int nextEndTag = text.indexOf(endTag, currentPos);

            // 如果没有找到任何标签，返回第一个结束标签位置（如果没有嵌套）
            if (nextStartTag == -1 && nextEndTag == -1) {
                return -1;
            }

            // 确定下一个出现的标签是开始标签还是结束标签
            if (nextStartTag != -1 && (nextEndTag == -1 || nextStartTag < nextEndTag)) {
                // 下一个是开始标签，增加嵌套级别
                nestingLevel++;
                currentPos = nextStartTag + 1;
            } else {
                if (nestingLevel == 0) {
                    // 找到匹配的结束标签
                    return nextEndTag;
                } else {
                    // 减少嵌套级别
                    nestingLevel--;
                    currentPos = nextEndTag + 1;
                }
            }
        }

        return -1;
    }

    /**
     * 处理颜色标签
     */
    private static void handleColorTag(String content, String tag, Font currentFont, Color currentColor,
                                       boolean isBold, boolean isItalic,
                                       List<Text> textList, ErrorHandler errorHandler, int depth) {
        // 从标签中提取颜色值
        String colorValue = extractColorValue(tag);
        if (colorValue == null) {
            if (errorHandler != null) {
                errorHandler.onError("颜色标签格式错误: " + tag);
            }
            // 将内容作为普通文本处理
            addTextSegment(content, currentFont, currentColor, isBold, isItalic, textList);
            return;
        }

        Color color;
        try {
            if (colorValue.length() == 6) {
                // #RRGGBB
                color = Color.web("#" + colorValue);
            } else if (colorValue.length() == 8) {
                // #RRGGBBAA
                color = Color.web("#" + colorValue);
            } else {
                throw new IllegalArgumentException("Invalid color format");
            }
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError("无效的颜色值: " + colorValue);
            }
            color = currentColor; // 使用当前颜色而不是默认黑色
        }

        // 递归处理颜色标签内的内容
        parseTextRecursive(content, currentFont, color, isBold, isItalic,
                textList, errorHandler, depth + 1);
    }

    /**
     * 处理粗体标签
     */
    private static void handleBoldTag(String content, Font currentFont, Color currentColor,
                                      boolean isItalic, List<Text> textList,
                                      ErrorHandler errorHandler, int depth) {
        // 使用粗体字体
        Font boldFont = isItalic ?
                Font.font(Settings.BOLD_FONT.getFamily(), FontPosture.ITALIC, Settings.BOLD_FONT.getSize()) :
                Settings.BOLD_FONT;

        parseTextRecursive(content, boldFont, currentColor, true, isItalic,
                textList, errorHandler, depth + 1);
    }

    /**
     * 处理斜体标签
     */
    private static void handleItalicTag(String content, Font currentFont, Color currentColor,
                                        boolean isBold, List<Text> textList,
                                        ErrorHandler errorHandler, int depth) {
        // 设置斜体
        Font italicFont;
        if (isBold) {
            italicFont = Font.font(Settings.BOLD_FONT.getFamily(),
                    FontPosture.ITALIC, Settings.BOLD_FONT.getSize());
        } else {
            italicFont = Font.font(currentFont.getFamily(),
                    FontPosture.ITALIC, currentFont.getSize());
        }

        parseTextRecursive(content, italicFont, currentColor, isBold, true,
                textList, errorHandler, depth + 1);
    }

    /**
     * 添加文本段到列表
     */
    private static void addTextSegment(String text, Font font, Color color,
                                       boolean isBold, boolean isItalic, List<Text> textList) {
        if (text.isEmpty()) {
            return;
        }

        Text textNode = new Text(text);
        textNode.setFont(font);
        textNode.setFill(color);

        textList.add(textNode);
    }

    /**
     * 检查是否是开始标签
     */
    private static boolean isStartTag(String tag) {
        return tag.startsWith("<") && tag.endsWith(">") &&
               !tag.startsWith("</") && tag.length() > 2;
    }

    /**
     * 检查是否是结束标签
     */
    private static boolean isEndTag(String tag) {
        return tag.startsWith("</") && tag.endsWith(">") && tag.length() > 3;
    }

    /**
     * 获取标签名称
     */
    private static String getTagName(String tag) {
        String content = tag.substring(1, tag.length() - 1);
        if (content.contains("=")) {
            return content.substring(0, content.indexOf('='));
        }
        return content;
    }

    /**
     * 从颜色标签中提取颜色值
     */
    private static String extractColorValue(String tag) {
        Pattern pattern = Pattern.compile("<color=#([0-9A-Fa-f]{6,8})>");
        Matcher matcher = pattern.matcher(tag);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}