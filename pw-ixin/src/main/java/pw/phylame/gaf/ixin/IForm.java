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

import java.awt.*;
import javax.swing.*;
import java.util.Map;
import java.util.HashMap;

import pw.phylame.gaf.core.I18nSupport;

/**
 * Main window for application.
 */
public class IForm extends JFrame {
    public IForm() throws HeadlessException {
        super();
    }

    public IForm(String title) throws HeadlessException {
        super(title);
    }

    public IForm(String title, ISettings snap) throws HeadlessException {
        super(title);
        this.snap = snap;
    }

    protected void createComponents(Object[][] menuModel, Object[] toolModel) {
        createComponents(menuModel, toolModel, IApplication.sharedIApplication());
    }

    protected void createComponents(Object[][] menuModel, Object[] toolModel, ICommandListener listener) {
        Container contentPane = getContentPane();

        if (menuModel != null && menuModel.length > 0) {
            if (createMenuBar(menuModel, listener)) {
                if (toolModel != null && toolModel.length > 0 && createToolBar(toolModel)) {
                    contentPane.add(toolBar, BorderLayout.PAGE_START);
                }
            }
        }

        createStatusBar();
        contentPane.add(statusBar, BorderLayout.PAGE_END);
    }

    private boolean createMenuBar(Object[][] menuModel, ICommandListener listener) {
        JMenuBar menuBar = new JMenuBar();
        for (Object[] model : menuModel) {
            JMenu menu = new JMenu();
            if (IxinUtilities.addMenuItems(menu, model, listener, menuActions, this) > 0) {
                menuBar.add(menu);
            }
        }
        if (menuBar.getMenuCount() > 0) {
            setJMenuBar(menuBar);
            return true;
        }
        return false;
    }

    private boolean createToolBar(Object[] toolModel) {
        toolBar = new JToolBar(getTitle());
        toolBar.setRollover(true);
        return IxinUtilities.addToolItems(toolBar, toolModel, menuActions, this) > 0;
    }

    private void createStatusBar() {
        statusBar = new IStatusBar();
    }

    protected void restore() {
        if (toolBar != null) {
            toolBar.setVisible(snap.getBoolean(TOOL_BAR_VISIBLE, true));
            toolBar.setFloatable(!snap.getBoolean(TOOL_BAR_LOCKED, false));
        }
        if (statusBar != null) {
            statusBar.setVisible(snap.getBoolean(STATUS_BAR_VISIBLE, true));
        }
        Point point = snap.getPoint(FORM_LOCATION, null);
        if (point != null) {
            setLocation(point);
        }
        setSize(snap.getDimension(FORM_SIZE, defaultSize));
    }

    protected void destroy() {
        if (snap != null) {
            snap.setPoint(FORM_LOCATION, getLocation());
            snap.setDimension(FORM_SIZE, getSize());
            snap.setBoolean(TOOL_BAR_VISIBLE, isToolBarVisible());
            snap.setBoolean(TOOL_BAR_LOCKED, isToolBarLocked());
            snap.setBoolean(STATUS_BAR_VISIBLE, isStatusBarVisible());
        }
    }

    public void addMenuAction(String key, Action action) {
        menuActions.put(key, action);
    }

    public Map<String, Action> getMenuActions() {
        return menuActions;
    }

    public Action getMenuAction(String key) {
        return menuActions.get(key);
    }

    public boolean isActionEnable(String actionKey) {
        Action action = getMenuAction(actionKey);
        if (action != null) {
            return action.isEnabled();
        } else {
            throw new RuntimeException("no such action: " + actionKey);
        }
    }

    public void setActionEnable(String actionKey, boolean enable) {
        Action action = getMenuAction(actionKey);
        if (action != null) {
            action.setEnabled(enable);
        } else {
            throw new RuntimeException("no such action: " + actionKey);
        }
    }

    public boolean isActionSelected(String actionKey) {
        Action action = getMenuAction(actionKey);
        if (action != null) {
            return (boolean) action.getValue(Action.SELECTED_KEY);
        } else {
            throw new RuntimeException("no such action: " + actionKey);
        }
    }

    public void setActionSelected(String actionKey, boolean selected) {
        Action action = getMenuAction(actionKey);
        if (action != null) {
            action.putValue(Action.SELECTED_KEY, selected);
        } else {
            throw new RuntimeException("no such action: " + actionKey);
        }
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public boolean isToolBarVisible() {
        return toolBar.isVisible();
    }

    public void setToolBarVisible(boolean visible) {
        toolBar.setVisible(visible);
    }

    public boolean isToolBarLocked() {
        return !toolBar.isFloatable();
    }

    public void setToolBarLocked(boolean locked) {
        toolBar.setFloatable(!locked);
    }

    public IStatusBar getStatusBar() {
        return statusBar;
    }

    public boolean isStatusBarVisible() {
        return statusBar.isVisible();
    }

    public void setStatusBarVisible(boolean visible) {
        statusBar.setVisible(visible);
    }

    public String getStatusText() {
        return statusBar.getText();
    }

    public void setStatusText(String text) {
        statusBar.setText(text);
    }

    public ISettings getSnap() {
        return snap;
    }

    public JPopupMenu createPopupMenu(String label, Object... model) {
        return createPopupMenu(label, IApplication.sharedIApplication(), model);
    }

    public JPopupMenu createPopupMenu(String label, ICommandListener listener, Object... model) {
        JPopupMenu menu = new JPopupMenu(label);
        IxinUtilities.addMenuItems(menu, model, listener, menuActions, this);
        return menu;
    }

    private Map<String, Action> menuActions = new HashMap<>();

    private JToolBar toolBar;
    private IStatusBar statusBar;

    private ISettings snap = null;

    public abstract class MenuAction extends IAction {
        public MenuAction(String actionKey) {
            super(actionKey);
            addMenuAction(actionKey, this);
        }

        public MenuAction(String actionKey, I18nSupport translator, IResource resource) {
            super(actionKey, translator, resource);
            addMenuAction(actionKey, this);
        }
    }

    private static final String FORM_LOCATION = "form.location";
    private static final String FORM_SIZE = "form.size";
    private static final String TOOL_BAR_VISIBLE = "form.toolbar.visible";
    private static final String TOOL_BAR_LOCKED = "form.toolbar.locked";
    private static final String STATUS_BAR_VISIBLE = "form.statusbar.visible";

    public static Dimension defaultSize = new Dimension(780, 439);
}
