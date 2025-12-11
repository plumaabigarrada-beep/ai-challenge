package commands

import org.example.Config

class SetModelCommand {
    fun execute(config: Config, model: String?): String {
        if (model.isNullOrEmpty()) {
            return "Please provide a model name\n"
        }

        config.model = model
        return "Model set to $model\n"
    }
}
