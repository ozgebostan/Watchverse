package Client.UI.utils;

public class SecurityConstants {
    private SecurityConstants() {}

    //In register phase, user will choose a security question
    //It will be used at forgot password phase later
    public static final String[] SECURITY_QUESTIONS = {
            "Select a security question...",
            "What is your mother's maiden name?",
            "What was your dream job?",
            "What is your favorite movie?",
            "Which elementary school did you attend?",
            "In which city were you born?",
            "What is your favorite book?"
    };
}