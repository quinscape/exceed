package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.DomainObject;

/**
 * Enumerates the general value types of scoped contexts and provides an accessor method and a containment check method.
 */
public enum ScopedValueType
{
    OBJECT
        {
            /**
             * Returns the scoped domain object with the given name from the given scoped context.
             *
             * @param scopedContext     scoped context
             * @param name              name
             * @return  domain object
             */
            @Override
            public DomainObject get(ScopedResolver scopedContext, String name)
            {
                return scopedContext.getObject(name);
            }


            @Override
            public void set(ScopedResolver scopedContext, String name, Object value)
            {
                scopedContext.setObject(name, (DomainObject) value);
            }

            /**
             * Returns <code>true</code> of the given scope contains a domain object with the given name.
             *
             * @param scopedContext     scoped context
             * @param name              name
             * @return <code>true</code> if such a domain object exists
             */
            @Override
            public Object has(ScopedResolver scopedContext, String name)
            {
                return scopedContext.hasObject(name);
            }
        },
    LIST
        {
            /**
             * Returns the scoped domain object list with the given name from the given scoped context.
             *
             * @param scopedContext     scoped context
             * @param name              name
             * @return  domain object list
             */
            @Override
            public DataList get(ScopedResolver scopedContext, String name)
            {
                return scopedContext.getList(name);
            }


            /**
             * Returns <code>true</code> of the given scope contains a domain object list with the given name.
             *
             * @param scopedContext     scoped context
             * @param name              name
             * @return <code>true</code> if such a domain object list exists
             */
            @Override
            public Object has(ScopedResolver scopedContext, String name)
            {
                return scopedContext.hasList(name);
            }

            @Override
            public void set(ScopedResolver scopedContext, String name, Object value)
            {
                scopedContext.setList(name, (DataList) value);
            }

        },
    PROPERTY
        {
            /**
             * Returns the scoped property with the given name from the given scoped context.
             *
             * @param scopedContext     scoped context
             * @param name              name
             * @return  property
             */
            @Override
            public Object get(ScopedResolver scopedContext, String name)
            {
                return scopedContext.getProperty(name);
            }


            /**
             * Returns <code>true</code> of the given scope contains a property with the given name.
             *
             * @param scopedContext     scoped context
             * @param name              name
             * @return <code>true</code> if such a property exists
             */
            @Override
            public  Object has(ScopedResolver scopedContext, String name)
            {
                return scopedContext.hasProperty(name);
            }

            @Override
            public void set(ScopedResolver scopedContext, String name, Object value)
            {
                scopedContext.setProperty(name, value);
            }
        };


    public abstract Object get(ScopedResolver scopedContext, String name);
    public abstract void set(ScopedResolver scopedContext, String name, Object value);
    public abstract Object has(ScopedResolver scopedContext, String name);
}
