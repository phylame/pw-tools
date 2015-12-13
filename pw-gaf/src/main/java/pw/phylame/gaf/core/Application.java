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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * General application model.
 */
public abstract class Application implements Runnable, I18nSupport {
    private static final Log LOG = LogFactory.getLog(Application.class);

    /**
     * Path of plugin registration.
     */
    public static String pluginManifest = "META-INF/pw-gaf/plugin.prop";

    /**
     * Unique application instance.
     */
    protected static Application instance;

    /**
     * Application name.
     */
    private String name;

    /**
     * Application version.
     */
    private String version;

    /**
     * Command line arguments.
     */
    private String[] arguments;

    /**
     * Installed translator.
     */
    private I18nSupport translator = null;

    /**
     * User home directory for application.
     * Location: ${login_user_home}/.${app_name}
     */
    private String home;

    private List<Plugin> plugins = new LinkedList<>();

    private List<Runnable> cleanups = new LinkedList<>();

    protected Application(String name, String version, String[] args) {
        if (instance != null) {         // already created
            throw new RuntimeException("Application already exist");
        }
        instance = this;

        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;

        if (version == null) {
            throw new NullPointerException("version");
        }
        this.version = version;

        this.arguments = args;

        home = System.getProperty("user.home") + File.separatorChar + "." + name.toLowerCase();
    }

    static Application sharedApplication() {
        return instance;
    }

    protected void ensureHomeExisted() {
        File homeDir = new File(home);
        if (!homeDir.exists() && !homeDir.mkdir()) {
            throw new RuntimeException("Cannot create home directory: " + home);
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getHome() {
        return home;
    }

    public void installTranslator(I18nSupport translator) {
        if (translator == null) {
            throw new NullPointerException();
        }
        this.translator = translator;
    }

    protected void ensureTranslatorInstalled() {
        if (translator == null) {
            throw new RuntimeException("no translator installed");
        }
    }

    public I18nSupport getTranslator() {
        return translator;
    }

    @Override
    public String fetchString(String key) throws MissingResourceException {
        return translator.fetchString(key);
    }

    @Override
    public String getText(String key) throws MissingResourceException {
        return translator.getText(key);
    }

    @Override
    public String getOptionalText(String key, String defaultText) {
        return translator.getOptionalText(key, defaultText);
    }

    @Override
    public String getText(String key, Object... args) throws MissingResourceException {
        return translator.getText(key, args);
    }

    @Override
    public String getOptionalText(String key, String defaultText, Object... args) {
        return translator.getOptionalText(key, defaultText, args);
    }

    public void loadPlugins() throws IOException, ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        loadPlugins(GafUtilities.getContextClassLoader());
    }

    public void loadPlugins(ClassLoader classLoader) throws IOException,
            ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<String> lines = GafUtilities.linesOfResource(pluginManifest, classLoader, true);
        for (String line : lines) {
            Class<?> clazz = Class.forName(line, true, classLoader);
            if (Plugin.class.isAssignableFrom(clazz)) {
                Plugin plugin = (Plugin) clazz.newInstance();
                plugin.initialize();
                if (preparePlugin(plugin)) {
                    plugins.add(plugin);
                }
            } else {
                LOG.debug("invalid plugin class: " + clazz);
            }
        }
    }

    /**
     * Prepares the specified plugin.
     *
     * @param plugin the plugin
     * @return <tt>true</tt> to add the plugin to plugin list, otherwise
     * <tt>false</tt> to ignore the plugin
     */
    protected boolean preparePlugin(Plugin plugin) {
        return true;
    }

    protected void onStart() {

    }

    public void start() {
        onStart();
        run();
    }

    protected void onTerminated() {
        plugins.forEach(Plugin::destroy);
        cleanups.forEach(Runnable::run);
    }

    public void exit() {
        exit(0);
    }

    public void exit(int status) {
        onTerminated();
        System.exit(status);
    }

    public void addCleanup(Runnable r) {
        if (r != null && !cleanups.contains(r)) {
            cleanups.add(r);
        }
    }

    public void removeCleanup(Runnable r) {
        if (r != null) {
            cleanups.remove(r);
        }
    }

    public void echo(String text) {
        System.out.println(name + ": " + text);
    }

    public void localizedEcho(String key, Object... args) {
        echo(getText(key, args));
    }

    public void error(String text) {
        System.err.println(name + ": " + text);
    }

    public void localizedError(String key, Object... args) {
        error(getText(key, args));
    }

    public void error(String desc, Exception e, DebugLevel level) {
        error(desc);
        if (e != null) {
            switch (level) {
                case Echo:
                    localizedError(errorTipKey, e.getLocalizedMessage());
                    break;
                case Trace:
                    e.printStackTrace();
                    break;
            }
        }
    }

    public void error(String desc, Exception e) {
        error(desc, e, debugLevel);
    }

    public void localizedError(Exception e, DebugLevel level, String key, Object... args) {
        error(getText(key, args), e, level);
    }

    public void localizedError(Exception e, String key, Object... args) {
        localizedError(e, debugLevel, key, args);
    }

    public enum DebugLevel {
        None, Echo, Trace
    }

    protected String errorTipKey = "cli.error.details";
    protected DebugLevel debugLevel = DebugLevel.Trace;
}
