package powerband;

/**
 * Application entrypoint. The real startup flow (instruments, SuperCollider
 * connection, editor) arrives with later features.
 */
public final class Main {

    private static final String STARTUP_MESSAGE = "Power Band: project skeleton is ready";

    private Main() {
    }

    public static String startupMessage() {
        return STARTUP_MESSAGE;
    }

    public static void main(String[] args) {
        System.out.println(startupMessage());
    }
}
