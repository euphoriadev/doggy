package dalvik.annotation.optimization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder for the real FastNative annotation in the Android platform.
 *
 * Allows the run-test to compile without an Android bootclasspath.
 */

@Retention(RetentionPolicy.CLASS) // Save memory, don't instantiate as an object at runtime
@Target(ElementType.METHOD)
public @interface FastNative {}
