package model;

import java.util.List;

import model.elements.AccessPoint;

public interface ITextProcessor {

    List<String> suggest(String address);

    AccessPoint parse(String address);

    public static class Entry {
        private final int street;
        private final String city;

        public Entry(final int street, final String city) {
            this.street = street;
            this.city = city;
        }

        public String getCity() {
            return city;
        }

        public int getStreet() {
            return street;
        }
    }
}