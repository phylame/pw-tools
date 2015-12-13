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

import org.apache.commons.cli.*;
import pw.phylame.gaf.core.Application;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public abstract class CApplication extends Application {
    protected CApplication(String name, String version, String[] args) {
        super(name, version, args);
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public String[] getInputs() {
        return inputs;
    }

    public void addOption(Option option, CAction action) {
        options.addOption(option);
        actions.put(option, action);
    }

    public void addOptionGroup(OptionGroup group) {
        options.addOptionGroup(group);
    }

    protected abstract void makeOptions();

    protected CommandLineParser getCommandLineParser() {
        return new DefaultParser();
    }

    protected void onOptionError(ParseException ex) {
        ex.printStackTrace();
        exit(-1);
    }

    private void parseOptions() {
        CommandLine cmd = null;
        try {
            cmd = getCommandLineParser().parse(options, getArguments());
        } catch (ParseException ex) {
            onOptionError(ex);
            exit(-1);
        }
        assert cmd != null;
        for (Option option : cmd.getOptions()) {
            CAction action = actions.get(option);
            if (action instanceof CInitializer) {
                ((CInitializer) action).perform(this, cmd);
            } else {
                commands.add((CCommand) action);

            }
        }
        inputs = cmd.getArgs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        makeOptions();
    }

    /**
     * Does something before execute command.
     * <p>This method will be invoked after parsing option.
     * <p>If <tt>false</tt> returned, app will be terminated.
     *
     * @return a boolean value that <tt>false</tt> for error, <tt>true</tt> for success
     */
    protected boolean optionParsed() {
        return true;
    }

    private int dispatchCommand() {
        int status = 0;
        if (!commands.isEmpty()) {
            for (CCommand command : commands) {
                status = Math.min(status, command.perform(this));
            }
        } else if (defaultCommand != null) {
            status = defaultCommand.perform(this);
        }
        return status;
    }

    @Override
    public void run() {
        parseOptions();
        if (!optionParsed()) {
            exit(-1);
        }
        exit(dispatchCommand());
    }

    private final Map<String, Object> context = new HashMap<>();
    private String[] inputs;

    protected final Options options = new Options();
    protected CCommand defaultCommand;
    private final Map<Option, CAction> actions = new HashMap<>();
    private final List<CCommand> commands = new LinkedList<>();
}
