package pro.sky.sockswarehouseapplication.service.transactionsservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.sky.sockswarehouseapplication.exceptions.FileProcessingException;
import pro.sky.sockswarehouseapplication.model.socks.Socks;
import pro.sky.sockswarehouseapplication.model.transactions.Transactions;
import pro.sky.sockswarehouseapplication.model.transactions.TransactionsType;
import pro.sky.sockswarehouseapplication.service.fileservice.FilesService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;


@Service
public class TransactionsServiceImpl implements TransactionsService {

    @Value("${name.of.transactions.data.file}")
    private String dataFileName;
    private final FilesService filesService;

    private static Map<Long, Transactions> transactionsMap = new LinkedHashMap<>();

    private static Long id = 0L;

    public TransactionsServiceImpl(FilesService filesService) {
        this.filesService = filesService;
    }

    @PostConstruct
    private void init() {
        try {
            filesService.createNewFileIfNotExist(dataFileName);
            readFromFile();
        } catch (RuntimeException e) {
            throw new FileProcessingException("Проблема с чтением из файла с транзакциями");
        }
    }

    @Override
    public void addTransactions(TransactionsType type, LocalDateTime time, Socks socks) {
        Socks newSock = new Socks(socks.getColor(), socks.getSize(), socks.getCottonPart(), socks.getQuantity());
        Transactions transactions = new Transactions(type, time, newSock);
        transactionsMap.put(id++, transactions);
        saveToFile(dataFileName, transactionsMap);
    }

    @Override
    public Path createReport() throws IOException, NoSuchElementException {

        Path report = filesService.createTempFile("report");

        try (Writer writer = Files.newBufferedWriter(report, StandardOpenOption.APPEND)) {
            for (Map.Entry<Long, Transactions> transactions : transactionsMap.entrySet()) {
                writer.append(transactions.getKey().toString())
                        .append(": Тип: ")
                        .append(transactions.getValue().getType().name())
                        .append(", Время: ")
                        .append(transactions.getValue().getTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")))
                        .append(", Количество: ")
                        .append(String.valueOf(transactions.getValue().getSocks().getQuantity()))
                        .append(", Размер: ")
                        .append(transactions.getValue().getSocks().getSize().name())
                        .append(", Содержание хлопка ")
                        .append(String.valueOf(transactions.getValue().getSocks().getCottonPart()))
                        .append("%, Цвет: ")
                        .append(transactions.getValue().getSocks().getColor().name())
                        .append(System.lineSeparator());
            }
        }
        return report;
    }

    private void saveToFile(String dataFileName, Map<Long, Transactions> transactions) {
        try {
            TransactionsDataFile dataFile = new TransactionsDataFile(id, transactions);
            String json = new ObjectMapper().writeValueAsString(dataFile);
            filesService.saveToFile(json, dataFileName);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFromFile() {
        String json = filesService.readFromFile(dataFileName);
        try {
            TransactionsDataFile dataFile = new ObjectMapper().readValue(json, new TypeReference<TransactionsDataFile>() {
            });
            id = dataFile.getLastId();
            transactionsMap = dataFile.getTransactionsMap();
        } catch (MismatchedInputException e) {
            e.getMessage();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TransactionsDataFile {
        private long lastId;
        private Map<Long, Transactions> transactionsMap;
    }
}
