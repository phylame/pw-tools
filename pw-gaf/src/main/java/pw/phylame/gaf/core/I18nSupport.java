/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PW GAF.
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

package pw.phylame.gaf.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;

/**
 * Interface for I18N support component.
 */
public interface I18nSupport {

    String fetchString(String key) throws MissingResourceException;

    default String format(String text, Object... args) {
        return MessageFormat.format(text, args);
    }

    default String getText(String key) throws MissingResourceException {
        return fetchString(key);
    }

    default String getOptionalText(String key, String defaultText) {
        try {
            return fetchString(key);
        } catch (MissingResourceException e) {
            return defaultText;
        }
    }

    default String getText(String key, Object... args) throws MissingResourceException {
        return format(fetchString(key), args);
    }

    default String getOptionalText(String key, String defaultText, Object... args) {
        try {
            return format(fetchString(key), args);
        } catch (MissingResourceException e) {
            return format(defaultText, args);
        }
    }
}
