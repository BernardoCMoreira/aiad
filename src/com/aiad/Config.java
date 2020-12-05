package com.aiad;

public class Config {
    public static final int OPERATION_LENGTH = 10;
    public static final int TICKRATE = 2;
    public static final int PERIOD = 1000 / TICKRATE;
    public static final int MAX_RUNWAY_CLEARANCE_TIME = 20;

    public static final String AIRPLANE_CREATION_LOG = "creation_log.csv";
    public static final String AIRPLANE_ALLOCATION_LOG = "allocation_log.csv";


    public static final int CREATION_RATE = 2 * 1000;
    public static final int MAX_TIME_TO_ARRIVE = 45;
    public static final int MAX_FUEL_REMAINING = 15;
    public static final double DEBRIS_APPEARANCE_PROBABILITY = 0.0d;
}
