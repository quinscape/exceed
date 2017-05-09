package de.quinscape.exceed.runtime.service.client;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks providers that appear in the exceed docs context.
 *
 * @see de.quinscape.exceed.runtime.config.WebpackConfig#DOCS_MAIN_MODULE
 */
@Component
@ProviderQualifier

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceedDocsProvider
{
}
