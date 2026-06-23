package au.com.dius.pact.consumer.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Minimal Pact annotation stub for test compilation.
 *
 * This mirrors the Pact JUnit5 annotation signature used by Pact tests.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pact {
    String consumer();
}
