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

public class CFetchInteger extends CFetchValue<Integer> {
    public CFetchInteger(String option) {
        super(option);
    }

    @Override
    protected Integer valueOfString(String str) {
        return Integer.valueOf(str);
    }
}
