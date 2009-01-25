package org.apache.maven.doxia.site.decoration.inheritance;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.util.PathTool;

/**
 * Utilities that allow conversion of old and new pathes and URLs relative to each other.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public abstract class PathUtils
{
    private PathUtils()
    {
        // nop
    }

    /**
     * @param oldPath not null
     * @param newPath not null
     * @return a PathDescriptor converted by the new path
     * @throws MalformedURLException if any
     */
    public static final PathDescriptor convertPath( final PathDescriptor oldPath, final PathDescriptor newPath )
        throws MalformedURLException
    {
        String relative = getRelativePath( oldPath, newPath );

        if ( relative == null )
        {
            return oldPath;
        }

        return new PathDescriptor( relative );
    }

    /**
     * @param oldPathDescriptor not null
     * @param newPathDescriptor not null
     * @return a relative path depending if PathDescriptor is a file or a web url.
     * @see PathTool#getRelativeFilePath(String, String)
     * @see PathTool#getRelativeWebPath(String, String)
     */
    public static final String getRelativePath( final PathDescriptor oldPathDescriptor,
                                                final PathDescriptor newPathDescriptor )
    {
        // Cannot convert from URL to file.
        if ( oldPathDescriptor.isFile() )
        {
            if ( !newPathDescriptor.isFile() )
            {
                // We want to convert from a file to an URL. This is normally not possible...
                if ( oldPathDescriptor.isRelative() )
                {
                    // unless the old path is a relative path. Then we might convert an existing
                    // site into a new URL using resolvePaths()...
                    return oldPathDescriptor.getPath();
                }

                // The old path is not relative. Bail out.
                return null;
            }
        }

        // Don't optimize to else. This might also be old.isFile && new.isFile ...
        if ( !oldPathDescriptor.isFile() )
        {
            // URLs, determine if they share protocol and domain info
            URL oldUrl = oldPathDescriptor.getPathUrl();
            URL newUrl = newPathDescriptor.getPathUrl();

            if ( oldUrl == null || newUrl == null )
            {
                // One of the sites has a strange URL. no relative path possible, bail out.
                return null;
            }

            if ( ( newUrl.getProtocol().equalsIgnoreCase( oldUrl.getProtocol() ) )
                            && ( newUrl.getHost().equalsIgnoreCase( oldUrl.getHost() ) )
                            && ( newUrl.getPort() == oldUrl.getPort() ) )
            {
                // Both pathes point to the same site. So we can use relative pathes.

                String oldPath = oldPathDescriptor.getPath();
                String newPath = newPathDescriptor.getPath();

                return PathTool.getRelativeWebPath( newPath, oldPath );
            }

            // Different sites. No relative Path possible.
            return null;
        }

        // Both Descriptors point to a path. We can build a relative path.
        String oldPath = oldPathDescriptor.getPath();
        String newPath = newPathDescriptor.getPath();

        if ( oldPath == null || newPath == null )
        {
            // One of the sites has a strange URL. no relative path possible, bail out.
            return null;
        }

        return PathTool.getRelativeFilePath( oldPath, newPath );
    }
}
