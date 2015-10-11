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

import java.util.Map;


/**
 * Service that handles file extensions and their corresponding mime-type.
 * 
 * @author shelmberger
 * @author jterstiege
 */

public interface MediaTypeService
{
    /**
     * Returns the content type for the given  (file extension) name.
     * Internally the extension is always converted to lower case.
     * 
     * @param name          the file extension name
     * 
     * @return          the content type
     */

    String getContentType(String name);
    
    
    /**
     * Returns the map of all file extensions and their corresponding
     * mime-type.
     * 
     * @return          the map of all file extensions and their corresponding
     *                  mime-type        
     */

    Map<String, String> getExtensionToTypeMap();
}
