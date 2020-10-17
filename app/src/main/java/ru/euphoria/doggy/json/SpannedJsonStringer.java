/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.euphoria.doggy.json;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ru.euphoria.doggy.data.SettingsStore;


public class SpannedJsonStringer {
    /**
     * The output data, containing at most one top-level array or object.
     */
    private final SpannableStringBuilder builder = new SpannableStringBuilder();
    private final ArrayList<CharSequence> out = new ArrayList<>();
    private final int COLOR_ATOM = SettingsStore.nightMode() ? 0xFFAD7DC9 : 0xFF3E7EFF;
    private final int COLOR_NAME = SettingsStore.nightMode() ? 0xFFE6608D : 0xFF660E7A;
    private final int COLOR_STRING = SettingsStore.nightMode() ? 0xFFF59F3F : 0xFF008000;
    /**
     * Unlike the original implementation, this stack isn't limited to 20
     * levels of nesting.
     */
    private final List<Scope> stack = new ArrayList<>();
    /**
     * A string containing a full set of spaces for a single level of
     * indentation, or null for no pretty printing.
     */
    private final String indent;
    private int count = 1;

    public SpannedJsonStringer() {
        indent = null;
    }

    SpannedJsonStringer(int indentSpaces) {
        char[] indentChars = new char[indentSpaces];
        Arrays.fill(indentChars, ' ');
        indent = new String(indentChars);
    }

    /**
     * Begins encoding a new array. Each call to this method must be paired with
     * a call to {@link #endArray}.
     *
     * @return this stringer.
     */
    public SpannedJsonStringer array() throws JSONException {
        return open(Scope.EMPTY_ARRAY, "[");
    }

    /**
     * Ends encoding the current array.
     *
     * @return this stringer.
     */
    public SpannedJsonStringer endArray() throws JSONException {
        return close(Scope.EMPTY_ARRAY, Scope.NONEMPTY_ARRAY, "]");
    }

    /**
     * Begins encoding a new object. Each call to this method must be paired
     * with a call to {@link #endObject}.
     *
     * @return this stringer.
     */
    public SpannedJsonStringer object() throws JSONException {
        return open(Scope.EMPTY_OBJECT, "{");
    }

    /**
     * Ends encoding the current object.
     *
     * @return this stringer.
     */
    public SpannedJsonStringer endObject() throws JSONException {
        return close(Scope.EMPTY_OBJECT, Scope.NONEMPTY_OBJECT, "}");
    }

    /**
     * Enters a new scope by appending any necessary whitespace and the given
     * bracket.
     */
    SpannedJsonStringer open(Scope empty, String openBracket) throws JSONException {
        if (stack.isEmpty() && builder.length() > 0) {
            throw new JSONException("Nesting problem: multiple top-level roots");
        }
        beforeValue();
        stack.add(empty);
        builder.append(openBracket);
        return this;
    }

    /**
     * Closes the current scope by appending any necessary whitespace and the
     * given bracket.
     */
    SpannedJsonStringer close(Scope empty, Scope nonempty, String closeBracket) throws JSONException {
        Scope context = peek();
        if (context != nonempty && context != empty) {
            throw new JSONException("Nesting problem");
        }

        stack.remove(stack.size() - 1);
        if (context == nonempty) {
            newline();
        }
        builder.append(closeBracket);
        return this;
    }

    /**
     * Returns the value on the top of the stack.
     */
    private Scope peek() throws JSONException {
        if (stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }
        return stack.get(stack.size() - 1);
    }

    /**
     * Replace the value on the top of the stack with the given value.
     */
    private void replaceTop(Scope topOfStack) {
        stack.set(stack.size() - 1, topOfStack);
    }

    private Spannable getColoredString(String value, int color) {
        Spannable string = new SpannableString(value);
        string.setSpan(new ForegroundColorSpan(color), 0, string.length(), Spannable
                .SPAN_EXCLUSIVE_EXCLUSIVE);
        return string;
    }

    private Spannable getColoredString(char value, int color) {
        Spannable string = new SpannableString(String.valueOf(value));
        string.setSpan(new ForegroundColorSpan(color), 0, 1, Spannable
                .SPAN_EXCLUSIVE_EXCLUSIVE);
        return string;
    }

    /**
     * Encodes {@code value}.
     *
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double or null. May not be {@link Double#isNaN() NaNs}
     *              or {@link Double#isInfinite() infinities}.
     * @return this stringer.
     */
    public SpannedJsonStringer value(Object value) throws JSONException {
        if (stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }

        if (value instanceof JSONArray) {
            array();
            for (int i = 0, l = ((JSONArray) value).length(); i < l; i++) {
                Object object = ((JSONArray) value).opt(i);
                value(object);
            }
            endArray();
            return this;

        } else if (value instanceof JSONObject) {
            object();
            Iterator<String> keys = ((JSONObject) value).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                key(key).value(((JSONObject) value).opt(key));
            }
            endObject();
            return this;
        }

        beforeValue();

        if (value == null
                || value instanceof Boolean
                || value == JSONObject.NULL) {
            builder.append(getColoredString(String.valueOf(value), COLOR_ATOM));

        } else if (value instanceof Number) {
            builder.append(getColoredString(JSONObject.numberToString((Number) value),
                    COLOR_ATOM));
        } else {
            string(value.toString(), false);
        }

        return this;
    }

    /**
     * Encodes {@code value} to this stringer.
     *
     * @return this stringer.
     */
    public SpannedJsonStringer value(boolean value) throws JSONException {
        if (stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }
        beforeValue();
        builder.append(getColoredString(String.valueOf(value), COLOR_ATOM));
        return this;
    }

    /**
     * Encodes {@code value} to this stringer.
     *
     * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
     *              {@link Double#isInfinite() infinities}.
     * @return this stringer.
     */
    public SpannedJsonStringer value(double value) throws JSONException {
        if (stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }
        beforeValue();
        builder.append(getColoredString(JSONObject.numberToString(value), COLOR_ATOM));
        return this;
    }

    /**
     * Encodes {@code value} to this stringer.
     *
     * @return this stringer.
     */
    public SpannedJsonStringer value(long value) throws JSONException {
        if (stack.isEmpty()) {
            throw new JSONException("Nesting problem");
        }
        beforeValue();
        builder.append(getColoredString(String.valueOf(value), COLOR_ATOM));
        return this;
    }

    private void string(String value, boolean isName) {
        int color = isName ? COLOR_NAME : COLOR_STRING;
        StringBuilder tmp = new StringBuilder("\"");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);

            /*
             * From RFC 4627, "All Unicode characters may be placed within the
             * quotation marks except for the characters that must be escaped:
             * quotation mark, reverse solidus, and the control characters
             * (U+0000 through U+001F)."
             */
            switch (c) {
                case '"':
                case '\\':
                case '/':
                    tmp.append('\\').append(c);
                    break;

                case '\t':
                    if (tmp.length() > 0) {
                        builder.append(getColoredString(tmp.toString(), color));
                        tmp.setLength(0);
                    }
                    builder.append(getColoredString("\\t", COLOR_ATOM));
                    break;

                case '\b':
                    if (tmp.length() > 0) {
                        builder.append(getColoredString(tmp.toString(), color));
                        tmp.setLength(0);
                    }
                    builder.append(getColoredString("\\b", COLOR_ATOM));
                    break;

                case '\n':
                    if (tmp.length() > 0) {
                        builder.append(getColoredString(tmp.toString(), color));
                        tmp.setLength(0);
                    }
                    builder.append(getColoredString("\\n", COLOR_ATOM));
                    break;

                case '\r':
                    if (tmp.length() > 0) {
                        builder.append(getColoredString(tmp.toString(), color));
                        tmp.setLength(0);
                    }
                    builder.append(getColoredString("\\r", COLOR_ATOM));
                    break;

                case '\f':
                    if (tmp.length() > 0) {
                        builder.append(getColoredString(tmp.toString(), color));
                        tmp.setLength(0);
                    }
                    builder.append(getColoredString("\\f", COLOR_ATOM));
                    break;

                default:
                    if (c <= 0x1F) {
                        tmp.append(String.format("\\u%04x", (int) c));
                    } else {
                        tmp.append(c);
                    }
                    break;
            }

        }
        tmp.append("\"");
        builder.append(getColoredString(tmp.toString(), color));
    }

    private void newline() {
        if (indent == null) {
            return;
        }

        if (count >= 15) {
            out.add(SpannedString.valueOf(builder));
            builder.clear();
            count = 1;
        } else {
            builder.append('\n');
            count++;
        }


        for (int i = 0; i < stack.size(); i++) {
            builder.append(indent);
        }
    }

    /**
     * Encodes the key (property name) to this stringer.
     *
     * @param name the name of the forthcoming value. May not be null.
     * @return this stringer.
     */
    public SpannedJsonStringer key(String name) throws JSONException {
        if (name == null) {
            throw new JSONException("Names must be non-null");
        }
        beforeKey();
        string(name, true);
        return this;
    }

    /**
     * Inserts any necessary separators and whitespace before a name. Also
     * adjusts the stack to expect the key's value.
     */
    private void beforeKey() throws JSONException {
        Scope context = peek();
        if (context == Scope.NONEMPTY_OBJECT) { // first in object
            builder.append(',');
        } else if (context != Scope.EMPTY_OBJECT) { // not in an object!
            throw new JSONException("Nesting problem");
        }
        newline();
        replaceTop(Scope.DANGLING_KEY);
    }

    /**
     * Inserts any necessary separators and whitespace before a literal value,
     * inline array, or inline object. Also adjusts the stack to expect either a
     * closing bracket or another element.
     */
    private void beforeValue() throws JSONException {
        if (stack.isEmpty()) {
            return;
        }

        Scope context = peek();
        if (context == Scope.EMPTY_ARRAY) { // first in array
            replaceTop(Scope.NONEMPTY_ARRAY);
            newline();
        } else if (context == Scope.NONEMPTY_ARRAY) { // another in array
            builder.append(',');
            newline();
        } else if (context == Scope.DANGLING_KEY) { // value for key
            builder.append(indent == null ? ":" : ": ");
            replaceTop(Scope.NONEMPTY_OBJECT);
        } else if (context != Scope.NULL) {
            throw new JSONException("Nesting problem");
        }
    }

    public void preGetCharSequences() {
        out.add(SpannedString.valueOf(builder));
        builder.clear();
    }

    public ArrayList<CharSequence> getCharSequences() {
        return out;
    }

    /**
     * Lexical scoping elements within this stringer, necessary to insert the
     * appropriate separator characters (ie. commas and colons) and to detect
     * nesting errors.
     */
    enum Scope {

        /**
         * An array with no elements requires no separators or newlines before
         * it is closed.
         */
        EMPTY_ARRAY,

        /**
         * A array with at least one value requires a comma and newline before
         * the next element.
         */
        NONEMPTY_ARRAY,

        /**
         * An object with no keys or values requires no separators or newlines
         * before it is closed.
         */
        EMPTY_OBJECT,

        /**
         * An object whose most recent element is a key. The next element must
         * be a value.
         */
        DANGLING_KEY,

        /**
         * An object with at least one name/value pair requires a comma and
         * newline before the next element.
         */
        NONEMPTY_OBJECT,

        /**
         * A special bracketless array needed by SpannedJsonStringer.join() and
         * JSONObject.quote() only. Not used for JSON encoding.
         */
        NULL,
    }
}