package ru.euphoria.doggy.api;

/**
 * Numeric status codes of errors
 * See website http://vk.com/dev/errors
 */
public class  ErrorCodes {
    /** Unknown error occurred. Try again later. */
    public static final int UNKNOWN_ERROR = 1;

    /** Application is disabled. Enable your application or use test mode */
    public static final int APP_DISABLED = 2;

    /** Unknown method passed. Check the method name */
    public static final int UNKNOWN_METHOD = 3;

    /** Incorrect signature. Check if the signature has been formed correctly */
    public static final int INVALID_SIGNATURE = 4;

    /** User authorization failed. Make sure that you use a correct authorization type */
    public static final int USER_AUTHORIZATION_FAILED = 5;

    /** Too many requests per second. Decrease the request frequency */
    public static final int TOO_MANY_REQUESTS = 6;

    /** Permission to perform this action is denied */
    public static final int NO_RIGHTS = 7;

    /** Invalid request. Check the request syntax */
    public static final int BAD_REQUEST = 8;

    /** Flood control. You need to decrease the count of identical requests. */
    public static final int TOO_MANY_SIMILAR_ACTIONS = 9;

    /** Internal server error. Try again later */
    public static final int INTERNAL_SERVER_ERROR = 10;

    /** In test mode application should be disabled or user should be authorized */
    public static final int IN_TEST_MODE = 11;

    /** For execute method. Unable to compile code */
    public static final int EXEXUTE_CODE_COMPILE_ERROR = 12;

    /** For execute method. Runtime error occurred during code invocation */
    public static final int EXECUTE_CODE_RUNTIME_ERROR = 13;

    /** Captcha needed */
    public static final int CAPTCHA_NEEDED = 14;

    /** Access denied. Make sure that you use correct identifiers */
    public static final int ACCESS_DENIED = 15;

    /** HTTP authorization failed */
    public static final int REQUIRES_REQUESTS_OVER_HTTPS = 16;

    /** Validation required */
    public static final int VALIDATION_REQUIRED = 17;

    /** User was deleted or banned */
    public static final int USER_BANNED_OR_DELETED = 18;

    /** Permission to perform this action is denied for non-standalone application */
    public static final int ACTION_PROHIBITED = 20;

    /** Permission to perform this action is allowed only for Standalone and OpenAPI application */
    public static final int ACTION_ALLOWED_ONLY_FOR_STANDALONE = 21;

    /** This method was disabled */
    public static final int METHOD_OFF = 23;

    /** Confirmation require */
    public static final int CONFIRMATION_REQUIRED = 24;

    /** One of the parameters specified was missing or invalid */
    public static final int PARAMETER_IS_NOT_SPECIFIED = 100;

    /** Invalid application API ID */
    public static final int INCORRECT_APP_ID = 101;

    /** For add chat user. Out of limits */
    public static final int OUT_OF_LIMITS = 103;

    /** Invalid user id. Make sure that you use a correct id */
    public static final int INCORRECT_USER_ID = 113;

    /** Invalid timestam */
    public static final int INCORRECT_TIMESTAMP = 150;

    /** Access to album denied */
    public static final int ACCESS_TO_ALBUM_DENIED = 200;

    /** Access to audio denie */
    public static final int ACCESS_TO_AUDIO_DENIED = 201;

    /** Access to group denied */
    public static final int ACCESS_TO_GROUP_DENIED = 203;

    /** This album is full. You need to delete the odd objects from the album or use another album */
    public static final int ALBUM_IS_FULL = 300;

    /** Permission denied. You must enable votes processing in application setting */
    public static final int ACTION_DENIED = 500;

    /** Permission denied. You have no access to operations specified with given object(s) */
    public static final int PERMISSION_DENIED = 600;

    /** Message error */
    public static final int CANNOT_SEND_MESSAGE_BLACK_LIST = 900;
    public static final int CANNOT_SEND_MESSAGE_GROUP = 901;

    /** Invalid document id */
    public static final int INVALID_DOC_ID = 1150;

    /** Invalid document id */
    public static final int INVALID_DOC_TITLE = 1152;

    /** Access to document deleting is denied */
    public static final int ACCESS_TO_DOC_DENIED = 1153;

    private ErrorCodes() {
        // empty
    }

}