import compressor.ChatCompressor
import org.example.App
import org.example.ClientType
import org.example.Commands.config
import org.example.Config
import org.example.HuggingFaceClient
import org.example.LMStudioClient
import org.example.PerplexityClient


fun createApp() : App {

    val perplexityClient = PerplexityClient()
    val huggingFaceClient = HuggingFaceClient()
    val lmStudioClient = LMStudioClient()

    val clients = mapOf(
        ClientType.PERPLEXITY to perplexityClient,
        ClientType.HUGGINGFACE to huggingFaceClient,
        ClientType.LMSTUDIO to lmStudioClient
    )

    val compressor = ChatCompressor(
        client = lmStudioClient,
        compressPrompt = COMPRESS_PTOMPT,
    )

    val config = Config()

    return App(
        clients = clients,
        chatCompressor = compressor,
        config = config,
    )
}