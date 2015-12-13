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

import java.awt.event.ActionEvent;

import pw.phylame.gaf.core.I18nSupport;

/**
 * Action that dispatch command of action to <tt>ICommandListener</tt>.
 */
public class IDispatchedAction extends IAction {
    private static final IApplication app = IApplication.sharedIApplication();

    private ICommandListener listener;

    public IDispatchedAction(String i18nKey, ICommandListener listener) {
        this(i18nKey, app.getTranslator(), app.getResource(), listener);
    }

    public IDispatchedAction(String i18nKey, I18nSupport translator,
                             IResource resource, ICommandListener listener) {
        super(i18nKey, translator, resource);
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        this.listener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        listener.commandPerformed(e.getActionCommand());
    }
}
