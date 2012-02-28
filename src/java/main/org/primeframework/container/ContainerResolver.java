/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.primeframework.container;

import java.net.URL;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface determines path locations within the bounds of a container.
 * The container might be a J2EE container or a scheduler or just a simple
 * stand-alone JVM. This abstraction allows the application to find paths without
 * depending directly on container objects like the ServletContext.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ImplementedBy(ServletContainerResolver.class)
public interface ContainerResolver {
    /**
     * Returns the location on the file system of the container based path.
     *
     * @param   path The path.
     * @return  The real path or null if the path could not be resolved.
     */
    String getRealPath(String path);

    /**
     * Returns the URL to the given path. This is allows containers that use JAR files or similar
     * archives to retrieve files without extracting them.
     *
     * @param   path THe path to get the resource for.
     * @return  The resource URL or null if it couldn't be resolved.
     */
    URL getResource(String path);
}