import org.example.Commands
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandTest {
    @Test
    fun matches() {

        assertTrue { Commands.client.matches("-cl") }
        assertTrue { Commands.client.matches("--client") }

        assertTrue { Commands.exit.matches("--exit") }
        assertTrue { Commands.exit.matches("--quit") }

    }

}