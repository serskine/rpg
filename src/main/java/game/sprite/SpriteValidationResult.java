package game.sprite;

/**
 * Result of parsing a sprite data file.
 * Contains either valid sprite data or an error description.
 */
public class SpriteValidationResult {
    private final boolean isValid;
    private final String errorMessage;
    private final SpriteFile spriteFile;
    private final int errorLineNumber;

    /**
     * Private constructor - use factory methods instead.
     */
    private SpriteValidationResult(
        final boolean isValid,
        final String errorMessage,
        final SpriteFile spriteFile,
        final int errorLineNumber
    ) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
        this.spriteFile = spriteFile;
        this.errorLineNumber = errorLineNumber;
    }

    /**
     * Factory method for a successful validation result.
     */
    public static SpriteValidationResult success(final SpriteFile spriteFile) {
        return new SpriteValidationResult(true, null, spriteFile, -1);
    }

    /**
     * Factory method for a failed validation result.
     */
    public static SpriteValidationResult failure(final String errorMessage, final int lineNumber) {
        return new SpriteValidationResult(false, errorMessage, null, lineNumber);
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public SpriteFile getSpriteFile() {
        return spriteFile;
    }

    public int getErrorLineNumber() {
        return errorLineNumber;
    }

    @Override
    public String toString() {
        if (isValid) {
            return "SpriteValidationResult{valid, " + spriteFile.shapes().size() + " shapes}";
        } else {
            return "SpriteValidationResult{error: " + errorMessage + " (line " + errorLineNumber + ")}";
        }
    }
}
