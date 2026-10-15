import com.telegram.recruitment.client.TelegramApiClient;
import com.telegram.recruitment.config.BotConfig;
import com.telegram.recruitment.service.BotListenerService;
import com.telegram.recruitment.service.CvStorageService;

public class Main {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        BotConfig config = BotConfig.load();
        if (!config.isConfigured()) {
            System.err.println("Bot token is missing in config.properties");
            return;
        }

        TelegramApiClient apiClient = new TelegramApiClient(config);
        CvStorageService storageService = new CvStorageService(config);
        BotListenerService listenerService = new BotListenerService(apiClient, storageService, config);

        System.out.println("Starting Telegram Auto-Reply Bot (@" + config.getBotUsername() + ")...");
        if (config.hasRecruiterChatId()) {
            System.out.println("Recruiter alerts enabled for Chat ID: " + config.getRecruiterChatId());
        }
        apiClient.testConnection();

        listenerService.startListening();
    }
}