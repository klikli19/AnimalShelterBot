package pro.sky.animalshelterbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.animalshelterbot.constant.Commands;
import pro.sky.animalshelterbot.entity.OwnerDog;
import pro.sky.animalshelterbot.entity.User;
import pro.sky.animalshelterbot.repository.OwnerCatRepository;
import pro.sky.animalshelterbot.repository.OwnerDogRepository;
import pro.sky.animalshelterbot.repository.UserRepository;

/**
 * Сервис ProcessCallbackQueryService
 * Сервис для обработки нажатий кнопок пользователем
 *
 * @author Kilikova Anna
 * @author Bogomolov Ilya
 * @author Marina Gubina
 * @see UpdatesListener
 */
@Service
public class ProcessCallbackQueryService {

    /**
     * Поле: объект, который запускает события журнала.
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    /**
     * Поле: телеграм бот
     */
    private final TelegramBot telegramBot;

    /**
     * Поле: сервис информационного меню приюта
     */
    private final ShelterInfoMenuService shelterInfoMenuService;

    /**
     * Поле: сервис меню рекомендаций
     */
    private final RecommendationMenuService recommendationMenuService;

    /**
     * Поле: сервис меню советов
     */
    private final AdvicesMenuService advicesMenuService;

    /**
     * Поле: сервис стартового меню
     */
    private final StartMenuService startMenuService;

    /**
     * Поле: сервис меню данных о приюте
     */
    private final ShelterDataMenuService shelterDataMenuService;

    /**
     * Поле: сервис меню отправки отчета
     */
    private final SendReportMenuService sendReportMenuService;

    /**
     * Поле: репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Конструктор
     *
     * @param telegramBot               телеграм бот
     * @param shelterInfoMenuService    сервис информационного меню приюта
     * @param recommendationMenuService сервис меню рекомендаций
     * @param advicesMenuService        сервис меню советов
     * @param startMenuService          сервис стартового меню
     * @param shelterDataMenuService    сервис меню данных о приюте
     * @param sendReportMenuService     сервис меню отправки отчета
     * @param userRepository            репозиторий пользователей
     */
    public ProcessCallbackQueryService(TelegramBot telegramBot,
                                       ShelterInfoMenuService shelterInfoMenuService,
                                       RecommendationMenuService recommendationMenuService,
                                       AdvicesMenuService advicesMenuService,
                                       StartMenuService startMenuService,
                                       ShelterDataMenuService shelterDataMenuService,
                                       SendReportMenuService sendReportMenuService, OwnerDogRepository ownerDogRepository, OwnerCatRepository ownerCatRepository, UserRepository userRepository) {

        this.telegramBot = telegramBot;
        this.shelterInfoMenuService = shelterInfoMenuService;
        this.recommendationMenuService = recommendationMenuService;
        this.advicesMenuService = advicesMenuService;
        this.startMenuService = startMenuService;
        this.shelterDataMenuService = shelterDataMenuService;
        this.sendReportMenuService = sendReportMenuService;
        this.userRepository = userRepository;
    }

    /**
     * Обработка нажатия кнопки пользователем
     *
     * @param update доступные обновления
     */
    public void processCallbackQuery(Update update) {
        String data = update.callbackQuery().data();
        String command = null;
        for (Commands currentCommand : Commands.values()) {
            if (currentCommand.getCallbackData().equals(data)) {
                command = currentCommand.name();
            }
        }
        switch (Commands.valueOf(command)) {
            // Стартовое меню (startMenuService)
            case TAKE_A_KITTEN:
                if (userRepository.findUserByChatId(update.callbackQuery().message().chat().id()) == null) {
                    userRepository.save(new User(update.callbackQuery().message().chat().id(),
                            update.callbackQuery().message().chat().firstName(),
                            false));
                } else {
                    User user = userRepository.findUserByChatId(update.callbackQuery().message().chat().id());
                    user.setIsDog(false);
                    userRepository.save(user);
                }
                telegramBot.execute(startMenuService.startMenu(update));
                break;
            case TAKE_THE_DOG:
                if (userRepository.findUserByChatId(update.callbackQuery().message().chat().id()) == null) {
                    userRepository.save(new User(update.callbackQuery().message().chat().id(),
                            update.callbackQuery().message().chat().firstName(),
                            true));
                } else {
                    User user = userRepository.findUserByChatId(update.callbackQuery().message().chat().id());
                    user.setIsDog(true);
                    userRepository.save(user);
                }
                telegramBot.execute(startMenuService.startMenu(update));
                break;
            case BACK:
                telegramBot.execute(startMenuService.startMenu(update));
                break;
            case VOLUNTEER:
                telegramBot.execute(startMenuService.volunteerMenu(update));
                break;
            // Информационное меню приюта (shelterInfoMenuService)
            case BACK_TO_ANIMAL_MENU:
            case ANIMAL_INFO:
                telegramBot.execute(shelterInfoMenuService.animalInfoMenu(update));
                break;
            case DATING_RULES:
                telegramBot.execute(shelterInfoMenuService.datingRules(update));
                break;
            case LIST_DOCUMENTS:
                shelterInfoMenuService.listDocuments(update);
                break;
            case CONTACT_DETAILS:
                telegramBot.execute(shelterInfoMenuService.contactDetails(update));
                break;
            // Подменю по рекомендациям (recommendationMenuService)
            case RECOMMENDATIONS:
                telegramBot.execute(recommendationMenuService.recommendationMenu(update));
                break;
            case RECOMMENDATIONS_TRANSPORTATION:
                telegramBot.execute(recommendationMenuService.recommendationsTransportation(update));
                break;
            case RECOMMENDATIONS_DOG:
                telegramBot.execute(recommendationMenuService.recommendations(update));
                break;
            case RECOMMENDATIONS_PUPPY:
                telegramBot.execute(recommendationMenuService.recommendationsPuppy(update));
                break;
            case RECOMMENDATIONS_DISABLED_DOG:
                telegramBot.execute(recommendationMenuService.recommendationsDisabled(update));
                break;
            // Подменю по советам (advicesMenuService)
            case ADVICES:
                telegramBot.execute(advicesMenuService.advicesMenu(update));
                break;
            case ADVICES_CYNOLOGISTS:
                telegramBot.execute(advicesMenuService.advicesCynologists(update));
                break;
            case LIST_CYNOLOGISTS:
                telegramBot.execute(advicesMenuService.listCynologists(update));
                break;
            case REASONS_REFUSAL:
                telegramBot.execute(advicesMenuService.reasonsRefusal(update));
                break;
            // Меню данных о приюте (shelterDataMenuService)
            case INFO:
                telegramBot.execute(shelterDataMenuService.shelterInfoMenu(update));
                break;
            case SHELTER_RECOMMENDATIONS:
                shelterDataMenuService.shelterRecommendation(update);
                break;
            case SHELTER_DATA:
                telegramBot.execute(shelterDataMenuService.shelterData(update));
                break;
            case CAR_PASS:
                telegramBot.execute(shelterDataMenuService.carPass(update));
                break;
            case CHOOSING_A_PET:
                telegramBot.execute(shelterDataMenuService.choosingAPet(update));
                break;
            // Меню отправки отчета (sendReportMenuService)
            case SUBMIT_REPORT:
                telegramBot.execute(sendReportMenuService.submitReportMenu(update));
                break;
            case REPORT_FORM:
                telegramBot.execute(sendReportMenuService.reportForm(update));
                break;
            default:
                telegramBot.execute(new SendMessage(update.callbackQuery().message().chat().id(),
                        "Упс... что-то пошло не так, мы скоро решим проблему, не переживайте"));
        }
    }
}
