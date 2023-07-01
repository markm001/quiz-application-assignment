import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    @Test
    void main() {
        String x = "Hello World from Test";
        assertEquals("Hello World from Test", x);
    }
}