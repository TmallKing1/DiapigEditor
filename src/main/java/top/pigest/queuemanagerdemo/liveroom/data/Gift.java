package top.pigest.queuemanagerdemo.liveroom.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 礼物类，和 {@link User} 用户类似，通过 API 获取时可能会有缺失值
 */
public class Gift {
    /**
     * 礼物名称
     */
    private String name;
    /**
     * 礼物 ID
     */
    private int id;
    /**
     * 该礼物的价格（1000等于1元，100等于1电池）
     */
    private int price;
    /**
     * 礼物数量，在处理投喂时需要使用
     */
    private int count;
    /**
     * 礼物 PNG 链接
     */
    private String imgBasic;
    /**
     * 礼物 GIF 链接
     */
    private String gif;
    /**
     * 礼物的其他属性，可任意添加
     */
    private Map<String, String> properties = new HashMap<>();
    public Gift(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Gift setName(String name) {
        this.name = name;
        return this;
    }

    public String getGif() {
        return gif;
    }

    public Gift setGif(String gif) {
        this.gif = gif;
        return this;
    }

    public String getImgBasic() {
        return imgBasic;
    }

    public Gift setImgBasic(String imgBasic) {
        this.imgBasic = imgBasic;
        return this;
    }

    public int getCount() {
        return count;
    }

    public Gift setCount(int count) {
        this.count = count;
        return this;
    }

    public int getPrice() {
        return price;
    }

    public Gift setPrice(int price) {
        this.price = price;
        return this;
    }

    public int getId() {
        return id;
    }

    public Gift setId(int id) {
        this.id = id;
        return this;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }
    public Gift setProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public int getType() {
        return properties.containsKey("type") ? Integer.parseInt(properties.get("type")) : -1;
    }

    public Gift setType(int type) {
        return this.setProperty("type", String.valueOf(type));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Gift gift)) return false;
        return getId() == gift.getId() && getPrice() == gift.getPrice() && getCount() == gift.getCount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPrice(), getCount());
    }

    public boolean equalsIgnoreCount(Object o) {
        if (!(o instanceof Gift gift)) return false;
        return getId() == gift.getId() && getPrice() == gift.getPrice();
    }
}
