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
import java.awt.BorderLayout;

/**
 * Status bar for <tt>IForm</tt>
 */
public class IStatusBar extends JPanel {
    public static int margin = 2;

    private JLabel label;
    private String lastText;

    public IStatusBar() {
        super(new BorderLayout());
        add((label = new JLabel()), BorderLayout.LINE_START);
        label.setBorder(BorderFactory.createEmptyBorder(0, margin, 0, 0));
    }

    public JLabel getLabel() {
        return label;
    }

    public void setText(String text) {
        lastText = text;
        label.setText(text);
    }

    public String getText() {
        return label.getText();
    }

    public void setTemporaryText(String text) {
        label.setText(text);
    }

    public void resetPreviousText() {
        label.setText(lastText);
    }
}
