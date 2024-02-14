package model;

import exeptions.UnsupportedStatusException;

public enum CityTitle {
    LRN, TLV, UFA, VVO;

    public static CityTitle from(String title) {
        for (CityTitle value : CityTitle.values()) {
            if (value.name().equals(title)) return value;
        }
        throw new UnsupportedStatusException("Unknown title: " + title);
    }
}
