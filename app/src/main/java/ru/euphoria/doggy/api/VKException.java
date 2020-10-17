package ru.euphoria.doggy.api;

import java.io.IOException;


/**
 * Thrown when server vk could not handle the request
 * see website to get description of error codes: http://vk.com/dev/errors
 * <p/>
 * Check {@link ErrorCodes} to get descriptions of error codes.
 */
public class VKException extends IOException {
    public String url;
    public String error;
    public String message;
    public int code;

    /**
     * Captcha ID,
     * see http://vk.com/dev/captcha_error
     */
    public String captchaSid;
    /**
     * Link to image, you want to show the user,
     * that he typed text from this image
     * <p/>
     * see http://vk.com/dev/captcha_error
     */
    public String captchaImg;

    /**
     * In some cases, VK requires passing a validation procedure of the user,
     * resulting in since version 5.0 API
     * (for older versions will be prompted captcha_error)
     * any request to API the following error is returned
     * <p/>
     * see http://vk.com/dev/need_validation
     */
    public String redirectUri;

    /**
     * Constructs a new {@code VKException}
     *
     * @param url     the url of executed request
     * @param message the detail error message for this exception
     * @param code    the error code
     */
    public VKException(String url, String message, int code) {
        super(message);
        this.url = url;
        this.message = message;
        this.code = code;
    }


    /**
     * Constructs a new {@code VKException}
     *
     * @param url   the url of executed request
     * @param error the detail error message for this exception
     */
    public VKException(String url, String error, String description) {
        this(url, description, 0);
        this.error = error;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "code: " + code + ", message: " + message;
    }
}