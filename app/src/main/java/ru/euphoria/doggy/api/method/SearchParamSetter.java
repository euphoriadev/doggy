package ru.euphoria.doggy.api.method;

import ru.euphoria.doggy.api.model.User;

public class SearchParamSetter extends ParamSetter<User> {
    public static final String FROM_LIST_FRIENDS = "friends";
    public static final String FROM_LIST_SUBSCRIPTIONS = "subscriptions";

    public SearchParamSetter(String method) {
        super(method);
    }

    @Override
    public SearchParamSetter put(String key, String value) {
        url.addQueryParameter(key, value);
        return this;
    }

    @Override
    public SearchParamSetter put(String key, int value) {
        url.addQueryParameter(key, String.valueOf(value));
        return this;
    }

    public SearchParamSetter q(String value) {
        return put("q", value);
    }

    public SearchParamSetter city(int value) {
        return put("city", value);
    }

    public SearchParamSetter country(int value) {
        return put("country", value);
    }

    public SearchParamSetter hometown(String value) {
        return put("hometown", value);
    }

    public SearchParamSetter sex(int value) {
        return put("sex", value);
    }

    public SearchParamSetter ageFrom(int value) {
        return put("age_from", value);
    }

    public SearchParamSetter ageTo(int value) {
        return put("age_to", value);
    }

    public SearchParamSetter birthDay(int value) {
        return put("birth_day", value);
    }

    public SearchParamSetter birthMonth(int value) {
        return put("birth_month", value);
    }

    public SearchParamSetter birthYear(int value) {
        return put("birth_year", value);
    }

    public SearchParamSetter fromList(String value) {
        return put("from_list", value);
    }

}
