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

import java.io.*;
import java.util.Locale;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.MissingResourceException;

/**
 * A helper for rendering translated keys.
 */
public class TranslateHelper implements I18nSupport {
    private static final Application app = Application.sharedApplication();

    public static String commentLabel = "#";
    public static String valueSeparator = "=";
    public static String encoding = "UTF-8";
    public static String lineSeparator = System.lineSeparator();

    // without language tag and extension
    private String output;
    private String[] targets;
    private LinkedList<String> keys = new LinkedList<>();
    private HashMap<String, String> messages = new HashMap<>();

    public TranslateHelper(String name, String output, String[] targets) {
        this(name, Locale.getDefault(), output, targets);
    }

    public TranslateHelper(String name, Locale locale, String output, String[] targets) {
        this(name, locale, TranslateHelper.class.getClassLoader(), output, targets);
    }

    public TranslateHelper(String name, Locale locale, ClassLoader classLoader,
                           String output, String[] targets) {
        this.output = output;
        this.targets = targets;
        loadMessages(name, locale, classLoader);
        app.addCleanup(this::renderAll);
    }

    private String convertPath(String name, String language, String country) {
        if (!language.isEmpty()) {
            name += "_" + language;
        }
        if (!country.isEmpty()) {
            name += "_" + country;
        }
        return name + ".properties";
    }

    private void loadMessages(String name, Locale locale, ClassLoader classLoader) {
        String path = convertPath(name, locale.getLanguage(), locale.getCountry());
        InputStream input = classLoader.getResourceAsStream(path);
        if (input == null) {
            path = convertPath(name, "", "");
            input = classLoader.getResourceAsStream(path);
        }
        if (input == null) {
            throw new RuntimeException("no such resource: " + name);
        }
        Closeable device = input;
        try {
            Reader reader = new InputStreamReader(input, encoding);
            device = reader;
            BufferedReader br = new BufferedReader(reader);
            device = br;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(commentLabel)) {
                    continue;
                }
                int index = line.indexOf(valueSeparator);
                if (index > 0) {
                    String key = line.substring(0, index).trim();
                    String value = line.substring(index + valueSeparator.length());
                    messages.put(key, value);
                    keys.add(key);
                }
            }
        } catch (IOException e) {
            app.error("cannot load resource: " + name, e);
        } finally {
            try {
                device.close();
            } catch (IOException e) {
                app.error("cannot close stream", e);
            }
        }
    }

    private void renderAll() {
        for (String target : targets) {
            store(output, target);
        }
    }

    private void store(String path, String target) {
        if (!target.isEmpty()) {
            path += "_" + target;
        }
        path += ".properties";
        Closeable device = null;
        try {
            OutputStream os = new FileOutputStream(path);
            device = os;
            os = new BufferedOutputStream(os);
            device = os;
            for (String key : keys) {
                String str = key + "=" + messages.get(key) + lineSeparator;
                os.write(str.getBytes(encoding));
            }
            os.flush();
        } catch (IOException e) {
            app.error("cannot store translate messages to: " + path, e);
        } finally {
            if (device != null) {
                try {
                    device.close();
                } catch (IOException e) {
                    app.error("cannot close translate helper output: " + path, e);
                }
            }
        }
    }

    @Override
    public String fetchString(String key) throws MissingResourceException {
        String text = messages.get(key);
        if (text == null) {
            messages.put(key, "");
            keys.add(key);
            text = key; // return the key
        }
        return text;
    }
}
