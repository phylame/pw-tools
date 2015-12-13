/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PW CLI.
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

package pw.phylame.gaf.cli;

import org.apache.commons.cli.CommandLine;

public abstract class CFetchValue<T> implements CInitializer {
    private final String option;

    protected CFetchValue(String option) {
        this.option = option;
    }

    protected abstract T valueOfString(String str);

    /**
     * Validates the option value.
     * <p>If failed to validate the value, the CLI application will be terminated.
     * <p>The sub implement should display error message.
     *
     * @param value <tt>false</tt> if the value is invalid.
     */
    protected boolean validateValue(T value) {
        return true;
    }

    @Override
    public void perform(CApplication app, CommandLine cmd) {
        T value = valueOfString(cmd.getOptionValue(option));
        if (!validateValue(value)) {
            app.exit(-1);
        }
        app.getContext().put(option, value);
    }
}
