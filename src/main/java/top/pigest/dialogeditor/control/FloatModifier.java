package top.pigest.dialogeditor.control;

import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import top.pigest.dialogeditor.Settings;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class FloatModifier extends HBox {
    private float value;
    private Consumer<Float> onValueSet = value -> {};
    private final float step;
    private final float min;
    private final float max;
    private final QMButton decrementButton = new QMButton("", null);
    private final JFXTextField valueField = new JFXTextField();
    private final QMButton incrementButton = new QMButton("", null);
    private boolean internalSet = false;
    public FloatModifier(float value) {
        this(value, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    public FloatModifier(float value, float min, float max) {
        this(value, 1, min, max);
    }
    public FloatModifier(float value, float step, float min, float max) {
        this.value = value;
        this.step = step;
        this.min = min;
        this.max = max;
        this.decrementButton.setGraphic(new WhiteFontIcon("fas-minus"));
        this.decrementButton.setOnAction(event -> this.decrement());
        this.incrementButton.setGraphic(new WhiteFontIcon("fas-plus"));
        this.incrementButton.setOnAction(event -> this.increment());
        this.valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValid(newValue)) {
                if (internalSet) {
                    internalSet = false;
                } else {
                    setValue(Float.parseFloat(newValue), false);
                }
            }
        });
        this.valueField.setUnFocusColor(Color.LIGHTGRAY);
        this.valueField.setFocusColor(Color.AQUA);
        this.valueField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
        this.valueField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !isValid(this.valueField.getText())) {
                setValue(getValue(), true);
            }
        });
        this.valueField.setText(value+"");
        this.valueField.setPrefWidth(100);
        this.valueField.setFont(Settings.DEFAULT_FONT);
        this.valueField.setAlignment(Pos.CENTER);

        this.getChildren().addAll(decrementButton, valueField, incrementButton);
        this.setValue(value, true);
    }

    public void setOnValueSet(Consumer<Float> onValueSet) {
        this.onValueSet = onValueSet;
    }

    public void setValue(float value, boolean updateText) {
        this.value = value;
        onValueSet.accept(value);
        incrementButton.disable(this.value >= max);
        decrementButton.disable(this.value <= min);
        if (updateText) {
            this.internalSet = true;
            DecimalFormat df = new DecimalFormat("0.00");
            this.valueField.setText(df.format(this.value));
        }
    }

    public float getValue() {
        return value;
    }

    public void decrement() {
        this.setValue(Math.max(value - step, min), true);
    }

    public void increment() {
        this.setValue(Math.min(value + step, max), true);
    }

    public boolean isValid(String s) {
        try {
            float f = Float.parseFloat(s);
            return f >= min && f <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
