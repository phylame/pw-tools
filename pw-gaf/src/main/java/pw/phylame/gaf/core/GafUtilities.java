/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PW GAF.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.gaf.core;

import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Utility class for GAF.
 */
public final class GafUtilities {
    public static ClassLoader getContextClassLoader() {
        PrivilegedAction<ClassLoader> action = () -> {
            ClassLoader classLoader = null;

            try {
                classLoader = Thread.currentThread().getContextClassLoader();
            } catch (SecurityException ex) {
                // ignore
            }

            // Return the selected class loader
            return classLoader;
        };
        return AccessController.doPrivileged(action);
    }

    public static Enumeration<URL> resourcesForPath(ClassLoader loader, String name) {
        PrivilegedAction<Enumeration<URL>> action = () -> {
            Enumeration<URL> urls = null;
            try {
                if (loader != null) {
                    urls = loader.getResources(name);
                } else {
                    urls = ClassLoader.getSystemResources(name);
                }
            } catch (IOException | NoSuchMethodError e) {
                // ignore
            }
            return urls;
        };
        return AccessController.doPrivileged(action);
    }

    public static List<String> linesOfResource(String path, ClassLoader classLoader,
                                               boolean skipEmpty) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        Enumeration<URL> urls = resourcesForPath(classLoader, path);
        if (urls == null) {
            return lines;
        }
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() || !skipEmpty) {
                    lines.add(line);
                }
            }
            reader.close();
        }
        return lines;
    }

    /**
     * Loads supported languages listed in specified file.
     * <p>File syntax:</p>
     * <ul>
     * <li>each line only contains one language tag</li>
     * <li>line starts with '#' as a comment line</li>
     * <li>non-ascii character is not supported</li>
     * </ul>
     *
     * @param url URL of the file
     * @return array of language tags
     */
    public static List<String> supportedLanguages(URL url) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        List<String> tags = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                tags.add(line);
            }
        }
        br.close();
        return tags;
    }
}
