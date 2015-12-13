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

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import java.util.LinkedList;

import pw.phylame.gaf.core.Plugin;
import pw.phylame.gaf.core.Application;

/**
 * Swing application model.
 */
public abstract class
IApplication<FORM extends IForm> extends Application implements ICommandListener {
    /**
     * Application resources provider.
     */
    private IResource resource;

    /**
     * Main form of the application.
     */
    private FORM form;

    /**
     * The delegate of command dispatcher.
     */
    private ICommandListener delegate;

    protected IApplication(String name, String version, String[] args) {
        super(name, version, args);
    }

    static IApplication sharedIApplication() {
        return (IApplication) instance;
    }

    public void installResource(IResource resource) {
        if (resource == null) {
            throw new NullPointerException();
        }
        this.resource = resource;
    }

    protected void ensureResourceInstalled() {
        if (resource == null) {
            throw new RuntimeException("no resource installed");
        }
    }

    public IResource getResource() {
        return resource;
    }

    public Icon loadIcon(String path) {
        ensureResourceInstalled();
        return resource.getIcon(path);
    }

    public Icon localizedIcon(String i18nKey) {
        return loadIcon(getText(i18nKey));
    }

    public Image loadImage(String path) {
        ensureResourceInstalled();
        return resource.getImage(path);
    }

    public Image localizedImage(String i18nKey) {
        return loadImage(getText(i18nKey));
    }

    public Icon loadOptionalIcon(String i18nKey) {
        String path = getOptionalText(i18nKey, null);
        if (path == null || path.isEmpty()) {
            return null;
        }
        ensureResourceInstalled();
        return resource.getIcon(path);
    }

    public Image loadOptionalImage(String i18nKey) {
        String path = getOptionalText(i18nKey, null);
        if (path == null || path.isEmpty()) {
            return null;
        }
        ensureResourceInstalled();
        return resource.getImage(path);
    }

    @Override
    protected boolean preparePlugin(Plugin plugin) {
        if (plugin instanceof IPlugin) {
            iPlugins.add((IPlugin) plugin);
        }
        return super.preparePlugin(plugin);
    }

    protected abstract FORM createForm();

    public FORM getForm() {
        return form;
    }

    /**
     * Sets the command delegate object.
     *
     * @param delegate the delegate
     */
    public void setDelegate(ICommandListener delegate) {
        this.delegate = delegate;
    }

    public ICommandListener getDelegate() {
        return delegate;
    }

    @Override
    public void run() {
        form = createForm();
        if (form == null) {
            throw new AssertionError("Implement of 'createForm' must return valid form");
        }
        iPlugins.forEach(IPlugin::performUI);
    }

    @Override
    public final void start() {
        onStart();
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void commandPerformed(String command) {
        if (delegate == null) {
            throw new RuntimeException("no delegate of dispatcher specified");
        }
        delegate.commandPerformed(command);
    }

    @Override
    protected void onTerminated() {
        form.destroy();
        super.onTerminated();
    }

    private LinkedList<IPlugin> iPlugins = new LinkedList<>();
}
