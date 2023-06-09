package pro.sky.animalshelterbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.animalshelterbot.constant.Commands;
import pro.sky.animalshelterbot.constant.OwnerStatus;
import pro.sky.animalshelterbot.entity.OwnerCat;
import pro.sky.animalshelterbot.entity.OwnerDog;
import pro.sky.animalshelterbot.repository.UserRepository;

/**
 * Сервис ProcessMessageService
 * Сервис для обработки сообщений от пользователя
 *
 * @author Kilikova Anna
 * @author Bogomolov Ilya
 * @author Marina Gubina
 * @see UpdatesListener
 */
@Service
public class ProcessMessageService {

    /**
     * Поле: объект, который запускает события журнала.
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    /**
     * Поле: телеграм бот
     */
    private final TelegramBot telegramBot;

    /**
     * Поле: сервис владельца собаки
     */
    private final OwnerDogService ownerDogService;

    /**
     * Поле: сервис владельца кота
     */
    private final OwnerCatService ownerCatService;

    /**
     * Поле: репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Конструктор
     *
     * @param telegramBot     телеграм бот
     * @param ownerDogService сервис владельца собаки
     * @param ownerCatService сервис владельца кота
     * @param userRepository  репозиторий пользователей
     */
    public ProcessMessageService(TelegramBot telegramBot,
                                 OwnerDogService ownerDogService,
                                 OwnerCatService ownerCatService,
                                 UserRepository userRepository) {
        this.telegramBot = telegramBot;
        this.ownerDogService = ownerDogService;
        this.ownerCatService = ownerCatService;
        this.userRepository = userRepository;
    }

    /**
     * Обработка принятых сообщений от пользователя
     *
     * @param update доступные обновления
     */
    public void processMessage(Update update) {
        if(update.message().contact() != null){
            createContactInDB(update);
            return;
        }
        else if (update.message().text() == null) {
            return;
        }
        StringBuilder text = new StringBuilder(update.message().text());
        text.delete(0, 1);
        Commands command;
        try {
            command = Commands.valueOf(text.toString().toUpperCase());
        } catch (IllegalArgumentException e){
            logger.info("The command was not found in Enum Commands");
            telegramBot.execute(new SendMessage(update.message().chat().id(), "Некорректный ввод, повторите запрос"));
            return;
        }

        switch (command) {
            case START:
                greeting(update);
                break;
            case INFO:
                info(update);
                break;
            case VOLUNTEER:
                volunteerMenu(update);
                break;
        }
    }

    /**
     * Метод, присылающий приветствие для пользователя
     *
     * @param update доступное обновление
     */
    public void greeting(Update update) {

        logger.info("Launched method: greeting, for user with id: " +
                update.message().chat().id());

        SendMessage greeting = new SendMessage(update.message().chat().id(),
                "Привет, " + update.message().from().firstName() + "! \uD83D\uDE42 \n" +
                        "\nЯ создан для того, что-бы помочь тебе найти друга, четвероного друга." +
                        "Я живу в приюте для животных и рядом со мной находятся брошенные питомцы, " +
                        "потерявшиеся при переезде, пережившие своих хозяев или рожденные на улице. " +
                        "Поначалу животные в приютах ждут\uD83D\uDC15, что за ними вернутся старые владельцы. \uD83D\uDC64" +
                        "Потом они ждут своих друзей-волонтеров \uD83D\uDC71\uD83C\uDFFB\u200D♂️ \uD83D\uDC71\uD83C\uDFFB\u200D♀️, " +
                        "корм по расписанию⌛, посетителей, которые погладят и почешут за ухом.❤️ " +
                        "Но больше всего приютские подопечные ждут, что их заберут домой.\uD83C\uDFE0 \n");

        telegramBot.execute(greeting);

        String desc = update.message().from().firstName() + ", может ты и есть тот самый хозяин, который подарит новый дом нашему другу?";

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Хочу взять собаку! \uD83D\uDC36").callbackData(Commands.TAKE_THE_DOG.getCallbackData()),
                new InlineKeyboardButton("Хочу взять котенка! \uD83D\uDC31").callbackData(Commands.TAKE_A_KITTEN.getCallbackData())
        );

        SendMessage description = new SendMessage(update.message().chat().id(), desc);
        description.replyMarkup(inlineKeyboardMarkup);

        telegramBot.execute(description);

    }

    /**
     * Метод, выдающий информацию для пользователя
     *
     * @param update доступное обновление
     */
    public void info(Update update) {

        logger.info("Launched method: info, for user with id: " +
                update.message().chat().id());

        String infoMsg = "«Приют» — слово, от которого становится тоскливо.\uD83D\uDE14" +
                "«Приют для животных» звучит еще более безрадостно.\uD83D\uDE22" +
                "Это место, где живут брошенные питомцы, потерявшиеся при переезде, пережившие своих хозяев или рожденные на улице. \uD83C\uDF27" +
                "\n" +
                "Поначалу животные в приютах ждут\uD83D\uDC15, что за ними вернутся старые владельцы. \uD83D\uDC64" +
                "Потом они ждут своих друзей-волонтеров \uD83D\uDC71\uD83C\uDFFB\u200D♂️ \uD83D\uDC71\uD83C\uDFFB\u200D♀️, " +
                "корм по расписанию⌛, посетителей, которые погладят и почешут за ухом.❤️ " +
                "Но больше всего приютские подопечные ждут, что их заберут домой.\uD83C\uDFE0 " +
                "Если вы решили взять питомца из приюта, то мы с радостью расскажем, как это сделать!\uD83D\uDC4D";

        SendMessage info = new SendMessage(update.message().chat().id(), infoMsg);
        telegramBot.execute(info);
    }

    /**
     * Метод, присылающий информацию по связи с волонтером для пользователя
     *
     * @param update доступное обновление
     * @return сообщение пользователю
     */
    private void volunteerMenu(Update update) {

        logger.info("Launched method: volunteer, for user with id: " +
                update.message().chat().id());

        telegramBot.execute(new SendMessage(update.message().chat().id(),
                "Волонтер скоро с вами свяжется\uD83D\uDE09"));
    }

    private void createContactInDB(Update update){
        logger.info("Created owner in database: " +
                update.message().chat().id());

        Long chatId = update.message().chat().id();

        if (update.message().contact() != null) {
            if (userRepository.findUserByChatId(update.message().chat().id()).isDog() &&
                    ownerDogService.findByChatId(chatId) == null) {
                ownerDogService.create(new OwnerDog(chatId,
                                update.message().contact().firstName(),
                                update.message().contact().phoneNumber()),
                        OwnerStatus.IN_SEARCH);
            } else if (ownerCatService.findByChatId(chatId) == null) {
                ownerCatService.create(new OwnerCat(chatId,
                                update.message().contact().firstName(),
                                update.message().contact().phoneNumber()),
                        OwnerStatus.IN_SEARCH);
            }
            telegramBot.execute(new SendMessage(update.message().chat().id(),
                    "Мы свяжемся с вами в ближайшее время!"));
        }
    }

}
