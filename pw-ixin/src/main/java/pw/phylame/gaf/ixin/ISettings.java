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

import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
import java.io.IOException;

import pw.phylame.gaf.core.Settings;

/**
 * <pre>General settings provider.</pre>
 * Settings file location: ${IApplication.sharedApplication().getHome()}/${base_name}${fileSuffix}.
 */
public class ISettings extends Settings {
    public ISettings() throws IOException {
    }

    public ISettings(boolean loading) throws IOException {
        super(loading);
    }

    public ISettings(boolean loading, String baseName, boolean autoSync) throws IOException {
        super(loading, baseName, autoSync);
    }

    public Point getPoint(String key, Point defaultValue) {
        return getItem(key, defaultValue, Point.class);
    }

    public void setPoint(String key, Point point) {
        setItem(key, point, Point.class);
    }

    public Dimension getDimension(String key, Dimension defaultValue) {
        return getItem(key, defaultValue, Dimension.class);
    }

    public void setDimension(String key, Dimension dimension) {
        setItem(key, dimension, Dimension.class);
    }

    public Font getFont(String key, Font defaultValue) {
        return getItem(key, defaultValue, Font.class);
    }

    public void setFont(String key, Font font) {
        setItem(key, font, Font.class);
    }

    public Color getColor(String key, Color defaultColor) {
        return getItem(key, defaultColor, Color.class);
    }

    public void setColor(String key, Color color) {
        setItem(key, color, Color.class);
    }

    private static class FontConverter implements Converter<Font> {
        @Override
        public String toString(Font o) {
            StringBuilder builder = new StringBuilder(o.getFamily());
            builder.append("-");
            switch (o.getStyle()) {
                case Font.PLAIN:
                    builder.append("plain");
                    break;
                case Font.BOLD:
                    builder.append("bold");
                    break;
                case Font.ITALIC:
                    builder.append("italic");
                    break;
                case Font.BOLD | Font.ITALIC:
                    builder.append("bolditalic");
                    break;
            }
            builder.append("-").append(o.getSize());
            return builder.toString();
        }

        @Override
        public Font valueOf(String str, Font defaultValue) {
            return Font.decode(str);
        }
    }

    private static class ColorConverter implements Converter<Color> {
        @Override
        public String toString(Color o) {
            return "#" + String.format("%X", o.getRGB()).substring(2);
        }

        @Override
        public Color valueOf(String str, Color defaultValue) {
            try {
                return Color.decode(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private static class PointConverter implements Converter<Point> {
        @Override
        public String toString(Point o) {
            return o.x + "-" + o.y;
        }

        @Override
        public Point valueOf(String str, Point defaultValue) {
            String[] parts = str.split("-");
            if (parts.length < 2) {
                return defaultValue;
            }
            try {
                int x = Integer.decode(parts[0].trim());
                return new Point(x, Integer.decode(parts[1].trim()));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private static class DimensionConverter implements Converter<Dimension> {
        @Override
        public String toString(Dimension o) {
            return o.width + "-" + o.height;
        }

        @Override
        public Dimension valueOf(String str, Dimension defaultValue) {
            String[] parts = str.split("-");
            if (parts.length < 2) {
                return defaultValue;
            }
            try {
                int width = Integer.decode(parts[0].trim());
                return new Dimension(width, Integer.decode(parts[1].trim()));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    static {
        registerConverter(Font.class, new FontConverter());
        registerConverter(Color.class, new ColorConverter());
        registerConverter(Point.class, new PointConverter());
        registerConverter(Dimension.class, new DimensionConverter());
    }
}
