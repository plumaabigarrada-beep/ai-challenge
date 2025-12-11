package commands

import org.example.Config

class SetTemperatureCommand {
    fun execute(config: Config, temperature: Double?): String {
        if (temperature == null) return "Provide temperature value"
        if (temperature in 0.0..< 2.0) {
            config.temperature = temperature
            return "Temperature set to ${config.temperature}\n"
        }

        return "Invalid temperature. Please provide a 0 <= value < 2\n"
    }
}
