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

public class IOptionDialog extends ICommonDialog {
    private Icon icon;
    private Object message;

    private int optionsAlignment;
    private Object[] options;
    private int optionIndex;

    public IOptionDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public IOptionDialog(Frame owner, String title) {
        super(owner, title, true);
    }

    public IOptionDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public IOptionDialog(Dialog owner, String title) {
        super(owner, title, true);
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setMessage(Object message) {
        if (message == null) {
            throw new NullPointerException();
        }
        this.message = message;
    }

    public void setOptions(int alignment, int defaultOption, Object... options) {
        optionsAlignment = alignment;
        this.options = options;
        optionIndex = defaultOption;
    }

    public int getOption() {
        return optionIndex;
    }

    @Override
    public void setVisible(boolean b) {
        initialize(false);
        setSize(Math.max(getWidth(), 256), Math.max(getHeight(), 123));
        super.setVisible(b);
    }

    public int showModal() {
        setVisible(true);
        return getOption();
    }

    @Override
    protected void createComponents(JPanel userPane) {
        JPanel pane = createIconPane();
        if (pane != null) {
            userPane.add(pane, BorderLayout.LINE_START);
        }
        userPane.add(createMessagePane(), BorderLayout.CENTER);
        controlsPane = createControlsPane();
    }

    protected JPanel createIconPane() {
        if (icon == null) {
            return null;
        }
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        pane.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
        pane.add(new JLabel(icon));
        pane.add(Box.createVerticalGlue());
        return pane;
    }

    protected JPanel createMessagePane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        pane.setBorder(BorderFactory.createEmptyBorder(margin, margin, 0, margin));
        pane.add(Box.createVerticalGlue());
        textMessage = message instanceof String || message instanceof JLabel;
        addMessageComponent(message, pane);
        pane.add(Box.createVerticalGlue());
        return pane;
    }

    private boolean textMessage = false;

    private void addMessageComponent(Object object, JPanel messagePane) {
        Component com;
        if (object instanceof String) {
            com = new JLabel((String) object, textMessage ? JLabel.CENTER : JLabel.LEADING);
        } else if (object instanceof Component) {
            com = (Component) object;
        } else if (object instanceof Object[]) {
            Object[] objects = (Object[]) object;
            for (Object o : objects) {
                addMessageComponent(o, messagePane);
            }
            return;
        } else {
            com = new JLabel(object.toString());
        }
        messagePane.add(IxinUtilities.createAlignedPane(SwingConstants.LEFT, 0, com));
        messagePane.add(Box.createVerticalStrut(margin));
    }

    protected JPanel createControlsPane() {
        if (options == null) {
            return null;
        }
        return createControlsPane(optionsAlignment, prepareOptions());
    }

    private Component[] prepareOptions() {
        Component components[] = new Component[options.length], com;
        for (int i = 0; i < options.length; i++) {
            Object option = options[i];
            if (option instanceof String) {
                com = new JButton(new OptionAction((String) option, i));
            } else if (option instanceof Component) {
                com = (Component) option;
            } else {
                throw new IllegalArgumentException("option require string or component: " + option);
            }
            components[i] = com;
            if (i == optionIndex) {
                if (!(com instanceof JButton)) {
                    throw new IllegalArgumentException("default option must be JButton: " + com);
                }
                defaultButton = (JButton) com;
            }
        }
        return components;
    }

    protected class OptionAction extends IAction {
        private int index;

        public OptionAction(String i18nKey, int index) {
            super(i18nKey);
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            optionIndex = index;
            dispose();
        }
    }
}
