package de.quinscape.exceed.tooling;


import org.jooq.util.DefaultGeneratorStrategy;
import org.jooq.util.Definition;

/**
 * <p>
 *     JOOQ Generator strategy ensuring all POJOs are generated with a common base class.
 * </p>
 * <p>
 *     Is compiled on its own by a separate maven compiler plugin execution configured in the project pom.xml.
 * </p>
 */
public class GeneratorStrategy
    extends DefaultGeneratorStrategy
{
    @Override
    public String getJavaClassExtends(Definition definition, Mode mode)
    {
        if (mode == Mode.POJO)
        {
            // can't reference class directly, is not compiled yet.
            return "de.quinscape.exceed.runtime.domain.GeneratedDomainObject";
        }
        return super.getJavaClassExtends(definition, mode);
    }
}
