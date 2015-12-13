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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Map;
import java.util.HashMap;

import pw.phylame.gaf.core.I18nSupport;

/**
 * Utilities for IxIn.
 */
public final class IxinUtilities {

    public static boolean useMnemonic = true;
    public static char MNEMONIC_PREFIX = '&';

    public static final Map<String, String> lafMap = new HashMap<>();

    static {
        lafMap.put("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        lafMap.put("Metal", "javax.swing.plaf.metal.MetalLookAndFeel");
        lafMap.put("Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        lafMap.put("Java", UIManager.getCrossPlatformLookAndFeelClassName());
        lafMap.put("System", UIManager.getSystemLookAndFeelClassName());
    }

    public static void mapLafPath(String name, String classPath) {
        lafMap.put(name, classPath);
    }

    public static String lafForName(String name) {
        String classPath = lafMap.get(name);
        return (classPath != null && !classPath.isEmpty()) ? classPath : name;
    }

    // 1
    public static void setAntiAliasing(boolean enable) {
        System.setProperty("awt.useSystemAAFontSettings", enable ? "on" : "off");
        System.setProperty("swing.aatext", String.valueOf(enable));
    }

    // 2
    public static void setWindowDecorated(boolean decorated) {
        JDialog.setDefaultLookAndFeelDecorated(decorated);
        JFrame.setDefaultLookAndFeelDecorated(decorated);
    }

    // 3
    public static void setLafTheme(String name) {
        if (name != null && !name.isEmpty()) {
            try {
                UIManager.setLookAndFeel(lafForName(name));
            } catch (Exception e) {
                throw new RuntimeException("cannot set to new laf: " + name, e);
            }
        }
    }

    private static final String[] fontKeys = {
            "TextArea.font", "TextPane.font", "TextField.font",
            "PasswordField.font", "FormattedTextField.font",
            "EditorPane.font", "Menu.font", "MenuBar.font", "PopupMenu.font",
            "PopupMenuSeparator.font", "MenuItem.font",
            "CheckBoxMenuItem.font", "RadioButtonMenuItem.font",
            "Button.font", "CheckBox.font", "ArrowButton.font",
            "RadioButton.font", "ToggleButton.font", "Panel.font",
            "RootPane.font", "SplitPane.font", "TabbedPane.font",
            "ScrollPane.font", "OptionPane.font", "OptionPane.buttonFont",
            "OptionPane.messageFont", "DesktopPane.font",
            "InternalFrameTitlePane.font", "ComboBox.font", "Label.font",
            "List.font", "ProgressBar.font", "Slider.font", "Spinner.font",
            "Table.font", "TableHeader.font", "TitledBorder.font",
            "ToolBar.font", "ToolTip.font", "Tree.font", "Viewport.font",
            "DesktopIcon.font", "ColorChooser.font",
    };

    // 4
    public static void setGlobalFont(Font newFont) {
        if (newFont == null) {
            return;
        }
        UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
        for (String fontKey : fontKeys) {
            Object value = uiDefaults.get(fontKey);
            Font font = null;
            if (value == null) {
                font = newFont;
            } else if (value instanceof Font) {
                font = (Font) value;
                font = newFont.deriveFont(font.getStyle());
            } else if (value instanceof UIDefaults.ActiveValue) {
                font = (Font) ((UIDefaults.ActiveValue) value).createValue(uiDefaults);
                font = newFont.deriveFont(font.getStyle());
            } else if (value instanceof UIDefaults.LazyValue) {
                font = (Font) ((UIDefaults.LazyValue) value).createValue(uiDefaults);
                font = newFont.deriveFont(font.getStyle());
            }
            if (font != null) {
                uiDefaults.put(fontKey, font);
            }
        }
    }

    /**
     * Parses text and mnemonic from name.
     *
     * @param name the name
     * @return array of text, mnemonic
     */
    public static Object[] mnemonicOfText(String name) {
        // get mnemonic from name
        String text = name;
        int mnemonic = 0;

        int index = name.indexOf(MNEMONIC_PREFIX);
        if (index >= 0 && index < name.length()) {
            char next = name.charAt(index + 1);
            if (Character.isLetter(next)) {     // has mnemonic
                mnemonic = next;
                text = name.substring(0, index) + name.substring(index + 1);
            }
        }
        return new Object[]{text, mnemonic, index};
    }

    /**
     * Deletes mnemonic label in text.
     *
     * @param text          the text contain mnemonic
     * @param bracketLength length of bracket surrounding mnemonic and its prefix
     * @return trimmed text
     */
    public static String trimMnemonic(String text, int mnemonicIndex, int bracketLength) {
        if (mnemonicIndex == -1 || bracketLength == 0) {
            return text;
        }
        return text.substring(0, mnemonicIndex - bracketLength) +
                text.substring(mnemonicIndex + 1 + bracketLength);
    }

    public static JLabel mnemonicLabel(String text) {
        Object[] objects = mnemonicOfText(text);
        JLabel label = new JLabel((String) objects[0]);
        if (useMnemonic) {
            label.setDisplayedMnemonic((int) objects[1]);
            label.setDisplayedMnemonicIndex((int) objects[2]);
        }
        return label;
    }

    public static JLabel localizedLabel(String i18nKey, I18nSupport translator, Component com) {
        String text = translator.getText(i18nKey);
        JLabel label = mnemonicLabel(text);
        label.setLabelFor(com);
        return label;
    }

    public static void performTipOnForm(JComponent comp, Action action, IForm form) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        performTipOnForm(comp, (String) action.getValue(Action.LONG_DESCRIPTION), form);
    }

    public static void performTipOnForm(JComponent comp, String text, IForm form) {
        if (form == null) {
            throw new NullPointerException("form");
        }
        if (text == null || text.isEmpty()) {
            return;
        }
        comp.addMouseListener(new StatusTipPerformer(text, form));
    }

    public static Action getOrCreateAction(String i18nKey, ICommandListener listener,
                                           Map<String, Action> am) {
        Action action = am.get(i18nKey);
        if (action != null) {
            return action;
        }
        action = new IDispatchedAction(i18nKey, listener);
        am.put(i18nKey, action);
        return action;
    }

    public static Action getOrCreateAction(IActionModel model, ICommandListener listener,
                                           Map<String, Action> am) {
        Action action = am.get(model.actionKey);
        if (action != null) {
            return action;
        }
        action = new IDispatchedAction(model.actionKey, listener);
        action.setEnabled(model.enable);
        action.putValue(Action.SELECTED_KEY, model.selected);
        am.put(model.actionKey, action);
        return action;
    }

    /**
     * Creates menu item with action.
     *
     * @param action the action for menu item, never {@code null}
     * @param type   type of this item, if {@code null} return {@code JMenuItem}.
     * @param form   the form to show action description, if {@code null} do nothing
     * @return the menu item
     */
    public static JMenuItem createMenuItem(Action action, IButtonType type, IForm form) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        JMenuItem item;
        if (type == null) {
            item = new JMenuItem(action);
        } else {
            switch (type) {
                case Radio:
                    item = new JRadioButtonMenuItem(action);
                    break;
                case Check:
                    item = new JCheckBoxMenuItem(action);
                    break;
                default:
                    item = new JMenuItem(action);
                    break;
            }
        }
        if (form != null) {
            performTipOnForm(item, action, form);
        }
        // hide tool tip text
        item.setToolTipText(null);
        return item;
    }

    public static int addMenuItems(JMenu menu, Object[] model,
                                   ICommandListener listener,
                                   Map<String, Action> am, IForm form) {
        menu.setAction(new IQuietAction((String) model[0]));
        // hide tool tip text
        menu.setToolTipText(null);
        return addMenuItems(menu.getPopupMenu(), model, 1, listener, am, form) + 1;
    }

    public static int addMenuItems(JPopupMenu menu, Object[] model,
                                   ICommandListener listener,
                                   Map<String, Action> am, IForm form) {
        return addMenuItems(menu, model, 0, listener, am, form);
    }

    private static int addMenuItems(JPopupMenu menu, Object[] menuModel,
                                    int fromIndex, ICommandListener listener,
                                    Map<String, Action> am, IForm form) {
        int number = 0;
        ButtonGroup group = null;
        for (int i = fromIndex; i < menuModel.length; ++i) {
            Object key = menuModel[i];
            JComponent item;
            if (key == null) {                          // separator
                item = new JPopupMenu.Separator();
            } else if (key instanceof String) {         // normal menu item
                Action action = getOrCreateAction((String) key, listener, am);
                item = createMenuItem(action, null, form);
            } else if (key instanceof Object[]) {       // sub menu
                JMenu submenu = new JMenu();
                addMenuItems(submenu, (Object[]) key, listener, am, form);
                item = submenu;
            } else if (key instanceof IActionModel) {     // action model
                IActionModel model = (IActionModel) key;
                Action action = getOrCreateAction(model, listener, am);
                item = createMenuItem(action, model.buttonType, form);
                // radio menu, add to button group
                if (model.buttonType == IButtonType.Radio) {
                    if (group == null) {
                        group = new ButtonGroup();
                    }
                    group.add((JMenuItem) item);
                } else if (group != null) {         // not radio and previous is radio
                    group = null;
                }
            } else {
                throw new RuntimeException("invalid menu model item: " + key);
            }
            menu.add(item);
            ++number;
        }
        return number;
    }

    public static AbstractButton createButton(Action action, IButtonType type,
                                              IForm form) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        AbstractButton button;
        if (type == null) {
            button = new JButton(action);
        } else {
            switch (type) {
                case Radio:
                    button = new JRadioButton(action);
                    break;
                case Check:
                    button = new JCheckBox(action);
                    break;
                case Toggle: {
                    button = new JToggleButton(action);
                    Icon icon = (Icon) action.getValue(IAction.SELECTED_ICON);
                    if (icon != null) {
                        button.setSelectedIcon(icon);
                    }
                }
                break;
                default:
                    button = new JButton(action);
                    break;
            }
        }
        if (form != null) {
            performTipOnForm(button, action, form);
        }
        return button;
    }

    public static JPanel createAlignedPane(int alignment, int space,
                                           Component... components) {
        if (components.length == 0) {
            return null;
        }

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));

        if (alignment != -1 && alignment != SwingConstants.LEFT) {
            pane.add(Box.createHorizontalGlue());
        }

        int end = components.length - 1;
        for (int ix = 0; ix < end; ++ix) {
            pane.add(components[ix]);
            pane.add(Box.createHorizontalStrut(space));
        }
        pane.add(components[end]);

        if (alignment != -1 && alignment != SwingConstants.RIGHT) {
            pane.add(Box.createHorizontalGlue());
        }
        return pane;
    }

    public static void addToolButton(JToolBar toolBar, AbstractButton button) {
        if (button.getIcon() != null) {
            button.setHideActionText(true);
        }
        Action action = button.getAction();
        if (action != null) {
            String tip = (String) action.getValue(Action.SHORT_DESCRIPTION);
            if (tip != null && !tip.isEmpty()) {
                KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
                if (keyStroke != null) {
                    tip += " (" + formatKeyStroke(keyStroke) + ")";
                }
                button.setToolTipText(tip);
            }
        }
        button.setFocusable(false);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setVerticalTextPosition(JButton.BOTTOM);
        toolBar.add(button);
    }

    public static AbstractButton addToolButton(JToolBar toolBar, Action action,
                                               IButtonType type, IForm form) {
        AbstractButton button = createButton(action, type, form);
        addToolButton(toolBar, button);
        return button;
    }

    /**
     * Gets buttons from actions and added to toolbar.
     *
     * @param toolBar   the toolbar
     * @param toolModel IxIn toolbar model
     * @param am        action map
     * @param form      the form to show action description, if {@code null} do nothing
     * @return number of added buttons
     */
    public static int addToolItems(JToolBar toolBar, Object[] toolModel,
                                   Map<String, Action> am, IForm form) {
        int number = 0;
        ButtonGroup group = null;
        for (Object key : toolModel) {
            if (key == null) {      // separator
                toolBar.addSeparator();
            } else if (key instanceof String) {
                Action action = am.get(key);
                if (action == null) {
                    continue;
                }
                addToolButton(toolBar, action, null, form);
            } else if (key instanceof IActionModel) {       // action model
                IActionModel model = (IActionModel) key;
                Action action = am.get(model.actionKey);
                if (action == null) {
                    continue;
                }
                AbstractButton b = addToolButton(toolBar, action,
                        model.buttonType, form);
                // radio menu, add to button group
                if (model.buttonType == IButtonType.Radio) {
                    if (group == null) {
                        group = new ButtonGroup();
                    }
                    group.add(b);
                } else if (group != null) {     // not radio and previous is radio
                    group = null;
                }
            } else if (key instanceof Component) {      // component
                toolBar.add((Component) key);
            }
            ++number;
        }
        return number;
    }

    public static String formatKeyStroke(KeyStroke keyStroke) {
        String str = keyStroke.toString();
        str = str.replaceAll("ctrl ", "Ctrl+");
        str = str.replaceAll("shift ", "Shift+");
        str = str.replaceAll("alt ", "Alt+");
        str = str.replaceAll("typed ", "");
        str = str.replaceAll("pressed ", "");
        str = str.replaceAll("released ", "");
        return str;
    }

    private static class StatusTipPerformer extends MouseAdapter {
        private String text;
        private IForm form;
        private boolean closed = true;

        StatusTipPerformer(String text, IForm form) {
            this.text = text;
            this.form = form;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            showTip();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            closeTip();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            closeTip();
        }

        private void showTip() {
            form.getStatusBar().setTemporaryText(text);
            closed = false;
        }

        private void closeTip() {
            if (!closed) {
                form.getStatusBar().resetPreviousText();
                closed = true;
            }
        }
    }
}
