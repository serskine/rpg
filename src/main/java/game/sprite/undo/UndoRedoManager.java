package game.sprite.undo;

import java.util.Stack;

/**
 * Manages undo/redo command history for the sprite editor.
 */
public class UndoRedoManager {
    private final Stack<UndoRedoCommand> undoStack = new Stack<>();
    private final Stack<UndoRedoCommand> redoStack = new Stack<>();
    
    /**
     * Execute a command and add it to the undo history.
     */
    public void execute(final UndoRedoCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // Clear redo stack when new command executed
    }
    
    /**
     * Undo the last command.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            final UndoRedoCommand command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }
    
    /**
     * Redo the last undone command.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            final UndoRedoCommand command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
    
    /**
     * Check if undo is possible.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Check if redo is possible.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Clear all undo/redo history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
