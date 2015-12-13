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

package pw.phylame.gaf.undo;

import java.util.LinkedList;

public class UndoManager {
    private LinkedList<UndoTask> undoStack = new LinkedList<>();
    private LinkedList<RedoTask> redoStack = new LinkedList<>();

    public void reset() {
        undoStack.clear();
        redoStack.clear();
    }

    public void insertUndoTask(UndoTask undoTask) {
        insertUndoTask(undoTask, true);
    }

    public void insertUndoTask(UndoTask undoTask, boolean clearRedo) {
        undoStack.push(undoTask);
        undoTask.setUndoManager(this);
        if (clearRedo && !redoStack.isEmpty()) {
            redoStack.clear();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo()) {
            throw new RuntimeException("no more undo task");
        }
        undoStack.pop().undo();
    }

    public String getPresentUndoMessage() {
        return canUndo() ? undoStack.getFirst().getMessage() : null;
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void redo() {
        if (!canRedo()) {
            throw new RuntimeException("no more redo task");
        }
        redoStack.pop().redo();
    }

    public String getPresentRedoMessage() {
        return canRedo() ? redoStack.getFirst().getMessage() : null;
    }

    final void addRelatedRedoTask(RedoTask redoTask) {
        redoStack.push(redoTask);
    }
}
