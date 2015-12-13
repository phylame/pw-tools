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

import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * A listener dispatch commands to delegate object.
 */
public class ICommandDispatcher implements ICommandListener {
    private ICommandListener delegate;
    private HashMap<String, Method> methodCaches = new HashMap<>();

    public ICommandListener getDelegate() {
        return delegate;
    }

    public void setDelegate(ICommandListener delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
        Method[] methods = delegate.getClass().getDeclaredMethods();
        for (Method method : methods) {
            ICommandMethod commonItem = method.getDeclaredAnnotation(ICommandMethod.class);
            if (commonItem != null) {
                methodCaches.put(commonItem.value(), method);
            }
        }
    }

    @Override
    public void commandPerformed(String command) {
        Method method = methodCaches.get(command);
        if (method == null) {
            throw new RuntimeException("no such method of delegate for command: " + command);
        }
        try {
            method.invoke(delegate);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("cannot execute command: " + command, e);
        }
    }
}
