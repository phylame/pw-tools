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

import javax.swing.KeyStroke;
import javax.swing.AbstractAction;

import pw.phylame.gaf.core.I18nSupport;

public abstract class IAction extends AbstractAction {
    // unique IApplication instance
    private static final IApplication app = IApplication.sharedIApplication();

    public static final String SELECTED_ICON = "SelectedIcon";

    public static String iconKeySuffix = ".icon";

    public static String acceleratorKeySuffix = ".shortcut";

    // shown in tool tip note
    public static String tipKeySuffix = ".tip";

    // shown in form status bar
    public static String detailsKeySuffix = ".details";

    public static String largeIconSuffix = "-large";

    public static String selectedIconSuffix = "-selected";

    // for default icon path prefix: ${iconPrefix}${actionCommand}.png, only icon not
    // specified in translator
    public static String iconPrefix = "actions/";

    public IAction(String i18nKey) {
        this(i18nKey, app.getTranslator(), app.getResource());
    }

    public IAction(String i18nKey, I18nSupport translator, IResource resource) {
        init(i18nKey, translator, resource);
    }

    private void init(String i18nKey, I18nSupport translator, IResource resource) {
        putValue(ACTION_COMMAND_KEY, i18nKey);

        // name and mnemonic
        String text = translator.getOptionalText(i18nKey, null);
        if (text != null && !text.isEmpty()) {
            Object[] pair = IxinUtilities.mnemonicOfText(text);
            putValue(NAME, pair[0]);
            if (IxinUtilities.useMnemonic) {
                putValue(MNEMONIC_KEY, pair[1]);
                putValue(DISPLAYED_MNEMONIC_INDEX_KEY, pair[2]);
            }
        }

        // small icon for menu item and button (if not large icon presented)
        // and large icon
        loadOptionalIcon(translator, resource, i18nKey);

        // menu accelerator
        loadOptionKeyStroke(translator, i18nKey + acceleratorKeySuffix, ACCELERATOR_KEY);

        // for tool tip
        loadOptionalText(translator, i18nKey + tipKeySuffix, SHORT_DESCRIPTION);

        // for frame help message (shown in status bar)
        loadOptionalText(translator, i18nKey + detailsKeySuffix, LONG_DESCRIPTION);
    }

    private void loadOptionalIcon(I18nSupport translator, IResource resource, String i18nKey) {
        String path = translator.getOptionalText(i18nKey + iconKeySuffix, iconPrefix + i18nKey + ".png");
        if (path == null || path.isEmpty()) {
            return;
        }
        putValue(SMALL_ICON, resource.getIcon(path, null));
        putValue(LARGE_ICON_KEY, resource.getIcon(path, largeIconSuffix));
        putValue(SELECTED_ICON, resource.getIcon(path, selectedIconSuffix));
    }

    private void loadOptionKeyStroke(I18nSupport translator, String i18nKey, String fieldKey) {
        String text = translator.getOptionalText(i18nKey, null);
        if (text == null || text.isEmpty()) {
            return;
        }
        putValue(fieldKey, KeyStroke.getKeyStroke(text));
    }

    private void loadOptionalText(I18nSupport translator, String i18nKey, String fieldKey) {
        String text = translator.getOptionalText(i18nKey, null);
        if (text == null || text.isEmpty()) {
            return;
        }
        putValue(fieldKey, text);
    }
}
