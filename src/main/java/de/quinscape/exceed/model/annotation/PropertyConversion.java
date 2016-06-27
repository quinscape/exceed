package de.quinscape.exceed.model.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates action model properties as being subject to property conversion with the property converter
 * for the defined type;
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface PropertyConversion
{
    /**
     * Property type.
     * @return
     */
    String type();
}
