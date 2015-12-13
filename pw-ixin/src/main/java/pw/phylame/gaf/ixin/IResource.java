/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PW IxIn.
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

package pw.phylame.gaf.ixin;

import java.net.URL;
import java.util.HashMap;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IResource {
    public static ClassLoader classLoader = IResource.class.getClassLoader();

    private String baseDir;
    private String gfxDir;

    public IResource(String baseDir) {
        this(baseDir, "gfx");
    }

    public IResource(String baseDir, String gfxDir) {
        this.baseDir = baseDir;
        this.gfxDir = gfxDir;
        if (!baseDir.endsWith("/")) {
            this.baseDir += "/";
        }
    }

    public Icon getIcon(String path) {
        return getIcon(path, null);
    }

    public Icon getIcon(String path, String suffix) {
        path = convertPath(gfxDir + "/" + path, suffix);
        Icon icon = iconCaches.get(path);
        if (icon != null) {
            return icon;
        }
        URL url = findFile(path, null);
        if (url != null) {
            icon = new ImageIcon(url);
            iconCaches.put(path, icon);
        }
        return icon;
    }

    public Image getImage(String path) {
        return getImage(path, null);
    }

    public Image getImage(String path, String suffix) {
        path = convertPath(gfxDir + "/" + path, suffix);
        Image image = imageCaches.get(path);
        if (image != null) {
            return image;
        }
        URL url = findFile(path, null);
        if (url != null) {
            image = Toolkit.getDefaultToolkit().getImage(url);
            imageCaches.put(path, image);
        }
        return image;
    }

    public URL findFile(String path) {
        return findFile(path, null);
    }

    public URL findFile(String path, String suffix) {
        return classLoader.getResource(baseDir + convertPath(path, suffix));
    }

    private static String convertPath(String path, String suffix) {
        if (suffix != null && !suffix.isEmpty()) {
            int index = path.indexOf('.');
            if (index == -1) {
                path += suffix;
            } else {
                path = path.substring(0, index) + suffix + path.substring(index);
            }
        }
        return path;
    }

    private static HashMap<String, Icon> iconCaches = new HashMap<>();
    private static HashMap<String, Image> imageCaches = new HashMap<>();
}
