package com.madimadica.hyde.ast;

public class ListData {
    public enum Type {
        UNORDERED,
        ORDERED
    }

    private Type type;
    private boolean tight = true;
    private char bulletChar; // [*+-]
    private int orderedStart;
    private char delimiter; // [.)]
    private int padding;
    private int markerOffset;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isTight() {
        return tight;
    }

    public void setTight(boolean tight) {
        this.tight = tight;
    }

    public char getBulletChar() {
        return bulletChar;
    }

    public void setBulletChar(char bulletChar) {
        this.bulletChar = bulletChar;
    }

    public int getOrderedStart() {
        return orderedStart;
    }

    public void setOrderedStart(int orderedStart) {
        this.orderedStart = orderedStart;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public int getMarkerOffset() {
        return markerOffset;
    }

    public void setMarkerOffset(int markerOffset) {
        this.markerOffset = markerOffset;
    }

    public int getMinimumIndent() {
        return markerOffset + padding;
    }

    public boolean isCompatibleWith(ListData that) {
        return this.type == that.type
            && this.delimiter == that.delimiter
            && this.bulletChar == that.bulletChar;
    }

}
