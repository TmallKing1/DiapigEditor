package top.pigest.queuemanagerdemo.misc;

import javafx.scene.layout.Pane;

import java.util.function.Supplier;

public record MiscFunction(String name, Supplier<Pane> supplier, String backgroundColor, String iconCode) {
}
