package com.matthiasbruns.rxretrofit.network;

/**
 * Created by mbruns on 20.05.17.
 * Created to parse the JSON response with GSON
 *
 * "lng": -99.12766456604,
 * "geonameId": 3530597,
 * "countrycode": "MX",
 * "name": "Mexiko-Stadt",
 * "fclName": "city, village,...",
 * "toponymName": "Mexico City",
 * "fcodeName": "capital of a political entity",
 * "wikipedia": "en.wikipedia.org/wiki/Mexico_City",
 * "lat": 19.428472427036,
 * "fcl": "P",
 * "population": 12294193,
 * "fcode": "PPLC"
 */

public class Geoname {

    public double lat;
    public double lng;
    public long geonameId;
    public String countrycode;
    public String name;
    public String fclName;
    public String toponymName;
    public String fcodeName;
    public String wikipedia;
    public String fcl;
    public long population;
    public String fcode;
}
