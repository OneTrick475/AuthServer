public interface Logger {
    void logFailedLogin(String username, String ip);
    void logAdminOperation(String type, String username, String ip, String change, String result);
}
