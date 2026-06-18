package com.activitytracker.util;

public final class Constants {

    private Constants() {}

    public static final String API_BASE = "/api/v1";
    public static final String AUTH_PATH = API_BASE + "/auth";
    public static final String ACTIVITY_TYPES_PATH = API_BASE + "/activity-types";
    public static final String ENTRIES_PATH = API_BASE + "/entries";
    public static final String USERS_PATH = API_BASE + "/users";

    public static final String BEARER_PREFIX = "Bearer ";
}
