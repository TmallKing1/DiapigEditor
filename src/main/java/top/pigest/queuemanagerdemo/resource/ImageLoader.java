package top.pigest.queuemanagerdemo.resource;

import javafx.scene.image.Image;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片缓存，优化图片加载，避免过高内存占用
 */
public class ImageLoader {
    private static final Map<String, WeakReference<Image>> CACHE = new HashMap<>();

    public static Image image(String url) {
        WeakReference<Image> img = CACHE.get(url);
        Image image = img == null ? null : img.get();

        if (image == null) {
            image = new Image(url, true);
            CACHE.put(url, new WeakReference<>(image));
        }

        return image;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
