package model;

import java.util.List;

import model.elements.IStreet;
import model.elements.StreetNode;

public interface ITextProcessor {

    List<String> suggest(String address);

    StreetNode parse(String address);

    public static class Entry {
        private final IStreet iStreet;
        private final String city;

        public Entry(final IStreet iStreet, final String city) {
            this.iStreet = iStreet;
            this.city = city;
        }

        public String getCity() {
            return city;
        }

        public IStreet getStreet() {
            return iStreet;
        }
    }
}