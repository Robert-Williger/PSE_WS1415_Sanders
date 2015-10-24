package model;

import java.util.List;

import model.elements.Street;
import model.elements.StreetNode;

public interface ITextProcessor {

    List<String> suggest(String address);

    StreetNode parse(String address);

    public static class Entry {
        private final Street street;
        private final String city;

        public Entry(final Street street, final String city) {
            this.street = street;
            this.city = city;
        }

        public String getCity() {
            return city;
        }

        public Street getStreet() {
            return street;
        }
    }
}