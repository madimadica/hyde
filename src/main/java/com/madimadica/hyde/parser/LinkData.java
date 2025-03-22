package com.madimadica.hyde.parser;

public record LinkData(String destination, String title) {
    public static LinkData ofDestinationAndTitle(String destination, String title) {
        return new LinkData(destination, title);
    }
}
