package Model;

public enum AuthResult {
    SUCCESS("Operation successful."),
    EMPTY_FIELDS("Please fill all the fields."),
    USER_NOT_FOUND("User couldn't found."),
    USER_ALREADY_EXISTS("This username already exists."),
    WRONG_SECURITY_ANSWER("Security answer to security question is wrong."),
    WRONG_PASSWORD("Password is wrong."),
    WEAK_PASSWORD("Password is weak (should be at least 6 characters)."),
    PASSWORD_UPDATED("Password updated successfully."),
    SAME_PASSWORD("New password cannot be same as old password."),
    ERROR("Unexpected error.");

    private final String message;

    AuthResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return this.name() + "###" + this.message;
    }
}