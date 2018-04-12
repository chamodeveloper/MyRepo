package com.conviva.api;

/**
 * The ConvivaConstants class is responsible for managing the Constants or Enums required.
 */
public class ConvivaConstants {

    /**
     * Possible types ad and content events.
     */
    public static enum Events {
        /**
         * "Conviva.AdRequested"
         */
        AD_REQUESTED("Conviva.AdRequested"),
        /**
         * "Conviva.AdResponse"
         */
        AD_RESPONSE("Conviva.AdResponse"),
        /**
         * "Conviva.SlotStarted"
         */
        AD_SLOT_STARTED("Conviva.SlotStarted"),
        /**
         * "Conviva.SlotEnded"
         */
        AD_SLOT_ENDED("Conviva.SlotEnded"),
        /**
         * "Conviva.AdAttempted"
         */
        AD_ATTEMPTED("Conviva.AdAttempted"),
        /**
         * "Conviva.AdImpression"
         */
        AD_IMPRESSION_START("Conviva.AdImpression"),
        /**
         * "Conviva.AdStart"
         */
        AD_START("Conviva.AdStart"),
        /**
         * "Conviva.AdFirstQuartile"
         */
        AD_FIRST_QUARTILE("Conviva.AdFirstQuartile"),
        /**
         * "Conviva.AdMidQuartile"
         */
        AD_MID_QUARTILE("Conviva.AdMidQuartile"),
        /**
         * "Conviva.AdThirdQuartile"
         */
        AD_THIRD_QUARTILE("Conviva.AdThirdQuartile"),
        /**
         * "Conviva.AdComplete"
         */
        AD_COMPLETE("Conviva.AdComplete"),
        /**
         * "Conviva.AdEnd"
         */
        AD_END("Conviva.AdEnd"),
        /**
         * "AD_IMPRESSION_END"
         */
        AD_IMPRESSION_END("Conviva.AdImpression"),
        /**
         * "Conviva.AdSkipped"
         */
        AD_SKIPPED("Conviva.AdSkipped"),
        /**
         * "Conviva.AdError"
         */
        AD_ERROR("Conviva.AdError"),
        /**
         * "Conviva.AdProgress"
         */
        AD_PROGRESS("Conviva.AdProgress"),
        /**
         * "Conviva.AdClose"
         */
        AD_CLOSE("Conviva.AdClose"),
        /**
         * "Conviva.PauseContent"
         */
        CONTENT_PAUSED("Conviva.PauseContent"),
        /**
         * "Conviva.ResumeContent"
         */
        CONTENT_RESUMED("Conviva.ResumeContent"),
        /**
         * "Conviva.PodStart"
         */
        POD_START("Conviva.PodStart"),
        /**
         * "Conviva.PodEnd"
         */
        POD_END("Conviva.PodEnd");

        private String val;

        Events(final String value) {
            val = value;
        }

        public String getValue() {
            return val;
        }
    }

    /**
     * Possible types of Error Event attributes.
     */
    public enum ErrorType {
        /**
         * "ERROR_UNKNOWN"
         */
        ERROR_UNKNOWN("ERROR_UNKNOWN"),
        /**
         * "ERROR_IO"
         */
        ERROR_IO("ERROR_IO"),
        /**
         * "ERROR_TIMEOUT"
         */
        ERROR_TIMEOUT("ERROR_TIMEOUT"),
        /**
         * "ERROR_NULL_ASSET"
         */
        ERROR_NULL_ASSET("ERROR_NULL_ASSET"),
        /**
         * "ERROR_MISSING_PARAMETER"
         */
        ERROR_MISSING_PARAMETER("ERROR_MISSING_PARAMETER"),
        /**
         * "ERROR_NO_AD_AVAILABLE"
         */
        ERROR_NO_AD_AVAILABLE("ERROR_NO_AD_AVAILABLE"),
        /**
         * "ERROR_PARSE"
         */
        ERROR_PARSE("ERROR_PARSE"),
        /**
         * "ERROR_INVALID_VALUE"
         */
        ERROR_INVALID_VALUE("ERROR_INVALID_VALUE"),
        /**
         * "ERROR_INVALID_SLOT"
         */
        ERROR_INVALID_SLOT("ERROR_INVALID_SLOT"),
        /**
         * "ERROR_3P_COMPONENT"
         */
        ERROR_3P_COMPONENT("ERROR_3P_COMPONENT"),
        /**
         * "ERROR_UNSUPPORTED_3P_FEATURE"
         */
        ERROR_UNSUPPORTED_3P_FEATURE("ERROR_UNSUPPORTED_3P_FEATURE"),
        /**
         * "ERROR_DEVICE_LIMIT"
         */
        ERROR_DEVICE_LIMIT("ERROR_DEVICE_LIMIT"),
        /**
         * "ERROR_UNMATCHED_SLOT_SIZE"
         */
        ERROR_UNMATCHED_SLOT_SIZE("ERROR_UNMATCHED_SLOT_SIZE");


        private String val;

        ErrorType(final String value) {
            val = value;
        }

        public String getValue() {
            return val;
        }
    }

    ;
}
