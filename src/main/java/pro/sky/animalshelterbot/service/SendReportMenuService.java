package pro.sky.animalshelterbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.animalshelterbot.constant.Commands;
import pro.sky.animalshelterbot.constant.OwnerStatus;
import pro.sky.animalshelterbot.entity.ReportCat;
import pro.sky.animalshelterbot.entity.ReportDog;
import pro.sky.animalshelterbot.entity.Volunteer;
import pro.sky.animalshelterbot.repository.OwnerDogRepository;
import pro.sky.animalshelterbot.repository.OwnerCatRepository;
import pro.sky.animalshelterbot.repository.ReportCatRepository;
import pro.sky.animalshelterbot.repository.ReportDogRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.DAYS;


/**
 * Сервис SendReportMenuService
 * Сервис меню отправки отчета
 *
 * @author Kilikova Anna
 * @author Bogomolov Ilya
 * @author Marina Gubina
 * @see UpdatesListener
 */
@Service
public class SendReportMenuService {

    /**
     * Поле: объект, который запускает события журнала.
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    /**
     * Поле: объект телеграм бот
     */
    private final TelegramBot telegramBot;

    /**
     * Поле: объект сервиса отчетов по собакам
     */
    private final ReportDogService reportDogService;

    /**
     * Поле: объект сервиса отчетов по котам
     */
    private final ReportCatService reportCatService;

    private Volunteer volunteer;
    private final ReportDogRepository repository;
    private final ReportCatRepository catRepository;

    private static final Pattern REPORT_PATTERN = Pattern.compile(
            "([А-яA-z\\s\\d\\D]+):(\\s)([А-яA-z\\s\\d\\D]+)\n" +
                    "([А-яA-z\\s\\d\\D]+):(\\s)([А-яA-z\\s\\d\\D]+)\n" +
                    "([А-яA-z\\s\\d\\D]+):(\\s)([А-яA-z\\s\\d\\D]+)");

    private final OwnerDogRepository ownerDogRepository;
    private final OwnerCatRepository ownerCatRepository;

    /**
     * Конструктор
     *
     * @param telegramBot      телеграм бот
     * @param reportDogService сервис отчетов по собакам
     * @param reportCatService сервис отчетов по котам
     * @param catRepository репозиторий отчетов по котам
     */
    public SendReportMenuService(TelegramBot telegramBot, ReportDogService reportDogService, ReportCatService reportCatService,
                                 ReportDogRepository repository, ReportCatRepository catRepository, OwnerDogRepository ownerDogRepository, OwnerCatRepository ownerCatRepository) {
        this.telegramBot = telegramBot;
        this.reportDogService = reportDogService;
        this.reportCatService = reportCatService;
        this.repository = repository;
        this.catRepository = catRepository;
        this.ownerDogRepository = ownerDogRepository;
        this.ownerCatRepository = ownerCatRepository;
    }

    /**
     * Метод, вызывающий подменю по отчетам
     *
     * @param update доступное обновление
     * @return меню для пользователя с кнопками
     */
    public SendMessage submitReportMenu(Update update) {
        SendMessage report = new SendMessage(update.callbackQuery().message().chat().id(), "Отправка отчета");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton(Commands.REPORT_FORM.getDescription())
                        .callbackData(Commands.REPORT_FORM.getCallbackData())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton(Commands.VOLUNTEER.getDescription())
                        .callbackData(Commands.VOLUNTEER.getCallbackData())

        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton(Commands.BACK.getDescription())
                        .callbackData(Commands.BACK.getCallbackData())
        );

        report.replyMarkup(inlineKeyboardMarkup);

        return report;
    }

    /**
     * Метод, присылающий форму отчета для пользователя
     *
     * @param update доступное обновление
     * @return форма отчета
     */
    public SendMessage reportForm(Update update) {

        logger.info("Launched method: report_form, for user with id: " +
                update.callbackQuery().message().chat().id());

        SendMessage message = new SendMessage(update.callbackQuery().message().chat().id(),
                "ЗАГРУЗИТЕ ОТЧЕТ В ФОРМАТЕ: \n \n" +
                        "Рацион: данные о рационе \n" +
                        "Информация: общая информация \n" +
                        "Привычки: данные о изменении привычек \n" +
                        "И прикрепите фото к отчету.");
        return message;
    }

    /**
     * Метод сохранения отчета из чата телеграм
     *
     * @param update доступное обновление
     */
    public void downloadReport(Update update) {

        logger.info("Launched method: download_report, for user with id: " +
                update.message().chat().id());

        String text = update.message().caption();
        Matcher matcher = REPORT_PATTERN.matcher(text);
        Long chatId = update.message().chat().id();

        logger.info("Accepted data [" + text + "]");

        if (matcher.matches()) {
            String animalDiet = matcher.group(3);
            String generalInfo = matcher.group(6);
            String changeBehavior = matcher.group(9);

            GetFile getFileRequest = new GetFile(update.message().photo()[1].fileId());
            GetFileResponse getFileResponse = telegramBot.execute(getFileRequest);
            try {
                File file = getFileResponse.file();
                file.fileSize();

                byte[] photo = telegramBot.getFileContent(file);
                LocalDate date = LocalDate.now();
                if(ownerDogRepository.findByChatId(chatId) != null &&
                        ownerDogRepository.findByChatId(chatId).getStatus() != OwnerStatus.IN_SEARCH){
                    reportDogService.downloadReport(chatId, animalDiet, generalInfo,
                            changeBehavior, photo, LocalDate.from(date.atStartOfDay()));
                    telegramBot.execute(new SendMessage(chatId, "Отчет успешно принят!"));
                } else if (ownerCatRepository.findByChatId(chatId) != null &&
                        ownerCatRepository.findByChatId(chatId).getStatus() != OwnerStatus.IN_SEARCH){
                    reportCatService.downloadReport(chatId, animalDiet, generalInfo,
                            changeBehavior, photo, LocalDate.from(date.atStartOfDay()));
                    telegramBot.execute(new SendMessage(chatId, "Отчет успешно принят!"));
                }
                else{telegramBot.execute(new SendMessage(chatId, "У вас нет питомца!!"));}

            } catch (IOException e) {
                logger.error("Ошибка загрузки фото");
                telegramBot.execute(new SendMessage(chatId,
                            "Ошибка загрузки фото"));
            }
        }
        else {
                telegramBot.execute(new SendMessage(chatId,
                        "Введены не все данные, заполните все поля в отчете! Повторите ввод!"));
        }
    }

    @Scheduled(cron = "0 0 14 ? * *")
    public void sendNotification() {
        logger.info("Requests report to OwnerDog");
        for (ReportDog reportDog : reportDogService.findNewReports()) {
            Long ownerDogId = reportDog.getOwnerDog().getId();
            long daysBetween = DAYS.between(LocalDate.now(), reportDog.getDateMessage());
            if (reportDog.getDateMessage().isBefore(LocalDate.now().minusDays(1))) {
                SendMessage sendMessage = new SendMessage(volunteer.getChatId(), "Отчет о собаке "
                        + reportDog.getOwnerDog().getDog().getName() + " (id: " + reportDog.getOwnerDog().getDog().getId() + ") от владельца "
                        + reportDog.getOwnerDog().getName() + " (id: " + ownerDogId + ") не поступал уже " + daysBetween + " дней. "
                        + "Дата последнего отчета: " + reportDog.getDateMessage());
                telegramBot.execute(sendMessage);
            }
            if (reportDog.getDateMessage().equals(LocalDate.now().minusDays(1))) {
                SendMessage sendToOwner = new SendMessage(reportDog.getOwnerDog().getChatId(), "Дорогой владелец, " +
                        "не забудь сегодня отправить отчет");
                telegramBot.execute(sendToOwner);
            }
        }
        for (ReportDog reportDog : reportDogService.findOldReports()) {
            Long ownerDogId = reportDog.getOwnerDog().getId();
            if (reportDog.getDateMessage().equals(LocalDate.now().minusDays(30))) {
                repository.save(reportDog);
                reportDog.getOwnerDog().setStatus(OwnerStatus.APPROVED);
                SendMessage sendMessage = new SendMessage(ownerDogId, reportDog.getOwnerDog().getName() + "! поздравляем," +
                        "испытательный срок в 30 дней для собаки " + reportDog.getOwnerDog().getDog().getName() +
                        " (id: " + reportDog.getOwnerDog().getDog().getId() + ") закончен");
                telegramBot.execute(sendMessage);
            }
        }
        logger.info("Requests report to OwnerCat");
        for (ReportCat reportCat : reportCatService.findNewReports()) {
            Long ownerCatId = reportCat.getOwnerCat().getId();
            long daysBetween = DAYS.between(LocalDate.now(), reportCat.getDateMessage());
            if (reportCat.getDateMessage().isBefore(LocalDate.now().minusDays(1))) {
                SendMessage sendMessage = new SendMessage(volunteer.getChatId(), "Отчет о кошке "
                        + reportCat.getOwnerCat().getCat().getName() + " (id: " + reportCat.getOwnerCat().getCat().getId() + ") от владельца "
                        + reportCat.getOwnerCat().getName() + " (id: " + ownerCatId + ") не поступал уже " + daysBetween + " дней. "
                        + "Дата последнего отчета: " + reportCat.getDateMessage());
                telegramBot.execute(sendMessage);
            }
            if (reportCat.getDateMessage().equals(LocalDate.now().minusDays(1))) {
                SendMessage sendToOwner = new SendMessage(reportCat.getOwnerCat().getChatId(), "Дорогой владелец, " +
                        "не забудь сегодня отправить отчет");
                telegramBot.execute(sendToOwner);
            }
        }
        for (ReportCat reportCat : reportCatService.findOldReports()) {
            Long ownerCatId = reportCat.getOwnerCat().getId();
            if (reportCat.getDateMessage().equals(LocalDate.now().minusDays(30))) {
                catRepository.save(reportCat);
                reportCat.getOwnerCat().setStatus(OwnerStatus.APPROVED);
                SendMessage sendMessage = new SendMessage(ownerCatId, reportCat.getOwnerCat().getName() + "! поздравляем," +
                        "испытательный срок в 30 дней для кошки " + reportCat.getOwnerCat().getCat().getName() +
                        " (id: " + reportCat.getOwnerCat().getCat().getId() + ") закончен");
                telegramBot.execute(sendMessage);
            }
        }
    }


}
