/*
Copyright (C) 2005-2010 QuinScape GmbH

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

If the program is linked with libraries which are licensed under the following 
licenses
* Apache License, Version 2.0
* The Apache Software License, Version 1.1
* Common Development and Distribution License (CDDL) 1.0
* GNU Lesser General Public License, Version 3 (LGPLv3)
* GNU Lesser General Public License, Version 2.1 (LGPLv2.1)
* Mozilla Public License Version 1.1 (MPL)
* BSD-style licenses
* MIT License
the combination of the program with these linked libraries is not considered a 
"derivative work". Therefore the distribution of the program linked with libraries 
licensed under these FOSS Licenses is allowed if the distribution is compliant with 
such FOSS Licenses.


This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.*/
package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MediaTypeServiceImpl
    implements MediaTypeService, InitializingBean
{
    private static Logger log = LoggerFactory.getLogger(MediaTypeServiceImpl.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String DEFAULT_MEDIA_TYPE = "text/plain";

    private Resource mimeTypesResource;

    private Map<String, String> extensionToType = new HashMap<String, String>();
    

    public MediaTypeServiceImpl(Resource mimeTypesResource)
    {
        this.mimeTypesResource = mimeTypesResource;
    }

    
    /**
     * {@inheritDoc}
     */
    
    @Override
    public String getContentType(String name)
    {
        Assert.notNull(name, "Can not determine content type without a name.");
        
        // we always convert the name to lowercase so that e.g. "png" and "PNG" are handled in the same way
        name = name.toLowerCase();
        
        String mediaType = DEFAULT_MEDIA_TYPE;
        
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0)
        {
            String mediaTypeFromMap = extensionToType.get(name.substring(lastDot + 1));
            if (mediaTypeFromMap != null)
            {
                mediaType = mediaTypeFromMap;
            }
        }
        
        log.debug("Media type for {} is {}", name, mediaType);
        
        return mediaType;
    }
    
    
    /**
     * {@inheritDoc}
     */

    @Override
    public Map<String, String> getExtensionToTypeMap()
    {
        return extensionToType;
    }


    /**
     * {@inheritDoc}
     */

    @Override
    public void afterPropertiesSet() throws Exception
    {
        InputStream is = mimeTypesResource.getInputStream();
        if (is != null)
        {
            log.info("Initializing media types from '{}'", mimeTypesResource);
            
            List<String> lines = IOUtils.readLines(is, UTF8);
            for (String line : lines)
            {
                if (!line.startsWith("#"))
                {
                    List<String> parts = Util.splitAtWhitespace(line);
                    Iterator<String> iterator = parts.iterator();
                    if (iterator.hasNext())
                    {
                        String mediaType = iterator.next();

                        while(iterator.hasNext())
                        {
                            extensionToType.put(iterator.next(), mediaType);
                        }
                    }
                }
            }
            
            log.debug("Content type map is {}", extensionToType);
        }
        else
        {
            throw new ExceedRuntimeException("Media types file '" + mimeTypesResource + "' does not exist.");
        }
    }
}
