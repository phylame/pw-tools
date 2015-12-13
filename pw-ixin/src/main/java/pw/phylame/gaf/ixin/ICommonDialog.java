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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Providers common dialog.
 */
public abstract class ICommonDialog extends JDialog {
    public static int margin = 5;
    public static int controlsSpace = 5;

    protected JPanel userPane;
    protected JPanel controlsPane = null;
    protected JButton defaultButton = null;

    public ICommonDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public ICommonDialog(Frame owner, String title) {
        super(owner, title);
    }

    public ICommonDialog(Dialog owner, String title) {
        super(owner, title);
    }

    public ICommonDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    protected void initialize(boolean resizable) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        setIconImage(IApplication.sharedIApplication().getForm().getIconImage());

        createContentPane();

        pack();
        setResizable(resizable);
        setLocationRelativeTo(getOwner());

        getRootPane().setDefaultButton(defaultButton);
    }

    private void createContentPane() {
        userPane = new JPanel(new BorderLayout());
        userPane.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
        createComponents(userPane);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        contentPane.add(userPane, BorderLayout.CENTER);
        if (controlsPane != null && controlsPane.getComponentCount() > 0) {
            contentPane.add(controlsPane, BorderLayout.PAGE_END);
        }

        setContentPane(contentPane);
    }

    // create buttons and others
    protected abstract void createComponents(JPanel userPane);

    protected void onCancel() {
        dispose();
    }

    protected JPanel createControlsPane(int alignment, Component... components) {
        JPanel pane = IxinUtilities.createAlignedPane(alignment, controlsSpace, components);
        if (pane != null) {
            pane.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
        }
        return pane;
    }

    protected JButton createCloseButton(String i18nKey) {
        return new JButton(new IAction(i18nKey) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
    }
}

