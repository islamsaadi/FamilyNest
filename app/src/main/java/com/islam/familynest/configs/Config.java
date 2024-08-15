package com.islam.familynest.configs;

public class Config {
    public static final String DB_URL = "https://familynest-4e792-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_NAME = "FamilyNest";
    public static final String APP_ENV = "production";

    private Config() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
