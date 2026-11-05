package powerband;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void startupMessageIsNotBlank() {
        assertFalse(Main.startupMessage().isBlank());
    }
}
