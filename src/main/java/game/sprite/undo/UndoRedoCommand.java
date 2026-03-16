package game.sprite.undo;

/**
 * Abstract base class for undo/redo commands in the sprite editor.
 */
public abstract class UndoRedoCommand {
    /**
     * Execute the command.
     */
    public abstract void execute();
    
    /**
     * Undo the command (reverse its effects).
     */
    public abstract void undo();
    
    /**
     * Get a human-readable description of this command.
     */
    public abstract String getDescription();
}
