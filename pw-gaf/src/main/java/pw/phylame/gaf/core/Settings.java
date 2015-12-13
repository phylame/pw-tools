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
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <pre>General settings provider.</pre>
 * Settings file location: ${Application.sharedApplication().getHome()}/${base_name}${fileSuffix}.
 */
public class Settings {
    private static final Log LOG = LogFactory.getLog(Settings.class);

    public static String encoding = "UTF-8";

    public static String commentLabel = "#";

    public static String valueSeparator = "=";

    public static String lineSeparator = System.lineSeparator();

    public static String dateFormat = "yyyy-M-D h:m:s";

    public static String fileSuffix = ".pref";

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    // settings file path
    private String path;

    private Map<String, String> settings = new TreeMap<>();

    private String comment = null;

    private boolean changed = false;

    public Settings() throws IOException {
        this(true);
    }

    public Settings(boolean loading) throws IOException {
        this(loading, "settings", true);
    }

    public Settings(boolean loading, String baseName, boolean autoSync) throws IOException {
        if (loading) {
            path = Application.sharedApplication().getHome() + File.separatorChar + baseName + fileSuffix;
            init();
        }
        setChanged(false);
        if (autoSync) {
            Application.sharedApplication().addCleanup(this::syncIfNeed);
        }
    }

    protected void init() throws IOException {
        File file = new File(path);

        if (!file.exists()) {   // not exists, create new
            reset();
            return;
        }

        try (FileInputStream in = new FileInputStream(file)) {
            load(in);
        }
    }

    protected void load(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(commentLabel)) {   // comment
                continue;
            }
            int ix = line.indexOf(valueSeparator);
            if (ix > -1) {
                settings.put(line.substring(0, ix).trim(), line.substring(ix + valueSeparator.length()));
            }
        }
    }

    /**
     * Stores settings to specified output stream.
     *
     * @param out the output stream
     * @throws IOException occurs IO errors when writing content
     */
    protected void store(OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
        if (!isEmpty(comment)) {
            for (String line : comment.split("(\\r\\n)|(\\n)|(\\r)")) {
                writer.write(commentLabel + " " + line.trim() + lineSeparator);
            }
            writer.write(commentLabel + " Encoding: " + encoding + lineSeparator);
            writer.write(lineSeparator);
        }
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            writer.write(entry.getKey() + valueSeparator + entry.getValue() + lineSeparator);
        }
        writer.flush();
    }

    /**
     * Updates settings content to setting file.
     */
    public void sync() throws IOException {
        ensureSettingsHomeExisted();
        FileOutputStream out = new FileOutputStream(path);
        store(out);
        out.close();
    }

    protected void syncIfNeed() {
        if (isChanged()) {
            try {
                sync();
            } catch (IOException e) {
                LOG.debug("cannot sync settings to file", e);
            }
        }
    }

    private void ensureSettingsHomeExisted() {
        File dir = new File(path).getParentFile();
        if (!dir.exists() && !dir.mkdir()) {
            throw new RuntimeException("Cannot create settings home: " + dir.getAbsolutePath());
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        setChanged(true);
    }

    public void reset() {
        setChanged(true);
    }

    public void clear() {
        settings.clear();
        setChanged(true);
    }

    public int itemCount() {
        return settings.size();
    }

    public String[] itemNames() {
        return settings.keySet().toArray(new String[settings.size()]);
    }

    public String removeItem(String key) {
        return settings.remove(key);
    }

    public Set<Map.Entry<String, String>> itemEntries() {
        return settings.entrySet();
    }

    public void update(Settings rhs) {
        update(rhs, false);
    }

    public void update(Settings rhs, boolean removePresents) {
        if (removePresents) {
            settings.clear();
        }
        settings.putAll(rhs.settings);
        setChanged(true);
    }

    public boolean isChanged() {
        return changed;
    }

    protected void setChanged(boolean changed) {
        this.changed = changed;
    }

    protected String getString(String key, String defaultValue) {
        String str = settings.get(key);
        return !isEmpty(str) ? str : defaultValue;
    }

    protected void setString(String key, String str) {
        if (str == null) {
            throw new NullPointerException("str");
        }
        settings.put(key, str);
        setChanged(true);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getItem(key, defaultValue, Boolean.class);
    }

    public void setBoolean(String key, boolean value) {
        setItem(key, value, Boolean.class);
    }

    public int getInteger(String key, int defaultValue) {
        return getItem(key, defaultValue, Integer.class);
    }

    public void setInteger(String key, int value) {
        setItem(key, value, Integer.class);
    }

    public double getReal(String key, double defaultValue) {
        return getItem(key, defaultValue, Double.class);
    }

    public void setReal(String key, double value) {
        setItem(key, value, Double.class);
    }

    public <T> T getItem(String key, T defaultValue, Class<T> clazz) {
        String raw = getString(key, null);
        if (isEmpty(raw)) {
            return defaultValue;
        }
        Converter<T> converter = getConverter(clazz);
        if (converter == null) {
            throw new RuntimeException("no converter for " + clazz);
        }
        return converter.valueOf(raw, defaultValue);
    }

    public <T> void setItem(String key, T value, Class<T> clazz) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        Converter<T> converter = getConverter(clazz);
        if (converter == null) {
            throw new RuntimeException("no converter for " + clazz);
        }
        setString(key, converter.toString(value));
    }

    /**
     * Registers settings item converter.
     *
     * @param clazz     the class of item
     * @param converter the converter, if <tt>null</tt> removes from map.
     * @param <T>       type of item
     */
    public static <T> void registerConverter(Class<T> clazz, Converter<T> converter) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        if (converter == null) {
            throw new NullPointerException("converter");
        }
        converters.put(clazz, converter);
    }

    /**
     * Gets converter by its class
     *
     * @param clazz class of item
     * @param <T>   type of item
     * @return the converter
     */
    @SuppressWarnings("unchecked")
    public static <T> Converter<T> getConverter(Class<T> clazz) {
        return (Converter<T>) converters.get(clazz);
    }

    private static final Map<Class<?>, Converter<?>> converters = new HashMap<>();

    public interface Converter<T> {
        String toString(T o);

        T valueOf(String str, T defaultValue);
    }

    private static class IntegerConverter implements Converter<Integer> {
        @Override
        public String toString(Integer o) {
            return Integer.toString(o);
        }

        @Override
        public Integer valueOf(String str, Integer defaultValue) {
            try {
                return Integer.decode(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private static class RealConverter implements Converter<Double> {

        @Override
        public String toString(Double o) {
            return Double.toString(o);
        }

        @Override
        public Double valueOf(String str, Double defaultValue) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private static class BooleanConverter implements Converter<Boolean> {
        @Override
        public String toString(Boolean o) {
            return Boolean.toString(o);
        }

        @Override
        public Boolean valueOf(String str, Boolean defaultValue) {
            return Boolean.parseBoolean(str);
        }
    }

    private static class DateConverter implements Converter<Date> {
        @Override
        public String toString(Date o) {
            return new SimpleDateFormat(dateFormat).format(o);
        }

        @Override
        public Date valueOf(String str, Date defaultValue) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            try {
                return sdf.parse(str);
            } catch (ParseException e) {
                return defaultValue;
            }
        }
    }

    private static class LocaleConverter implements Converter<Locale> {
        @Override
        public String toString(Locale o) {
            return o.toLanguageTag();
        }

        @Override
        public Locale valueOf(String str, Locale defaultValue) {
            return Locale.forLanguageTag(str.replace("_", "-"));
        }
    }

    static {
        registerConverter(Integer.class, new IntegerConverter());
        registerConverter(Double.class, new RealConverter());
        registerConverter(Boolean.class, new BooleanConverter());
        registerConverter(Date.class, new DateConverter());
        registerConverter(Locale.class, new LocaleConverter());
    }
}
