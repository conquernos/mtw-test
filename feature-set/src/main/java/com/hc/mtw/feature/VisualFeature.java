package com.hc.mtw.feature;


import org.conqueror.drone.selenium.webdriver.RGB;

public class VisualFeature {

    public enum FontStyle {

        normal(3), italic(2), oblique(1), etc(0);

        private final int number;

        FontStyle(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }

    }

    private final int windowWidth;
    private final int windowHeight;
    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final int r;
    private final int g;
    private final int b;
    private final float fontSize;
    private final String fontStyle;
    private final int fontStyleNumber;
    private final String fontFamily;
    private final int fontWeight;
    private final int wordCount;

    public VisualFeature(int windowWidth, int windowHeight, int x, int y, int w, int h, int r, int g, int b, float fontSize, String fontStyle, String fontFamily, int fontWeight, int wordCount) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.r = r;
        this.g = g;
        this.b = b;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        this.fontStyleNumber = FontStyle.valueOf(fontStyle).getNumber();
        this.fontFamily = fontFamily;
        this.fontWeight = fontWeight;
        this.wordCount = wordCount;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public RGB getRGB() {
        return new RGB(r, g, b);
    }

    public float getFontSize() {
        return fontSize;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public int getFontStyleNumber() {
        return fontStyleNumber;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public int getFontWeight() {
        return fontWeight;
    }

    public int getWordCount() {
        return wordCount;
    }

    @Override
    public String toString() {
        return "VisualFeature{" +
            "windowWidth=" + windowWidth +
            ", windowHeight=" + windowHeight +
            ", x=" + x +
            ", y=" + y +
            ", w=" + w +
            ", h=" + h +
            ", r=" + r +
            ", g=" + g +
            ", b=" + b +
            ", fontSize=" + fontSize +
            ", fontStyle='" + fontStyle + '\'' +
            ", fontFamily='" + fontFamily + '\'' +
            ", fontWeight=" + fontWeight +
            ", wordCount=" + wordCount +
            '}';
    }

}
