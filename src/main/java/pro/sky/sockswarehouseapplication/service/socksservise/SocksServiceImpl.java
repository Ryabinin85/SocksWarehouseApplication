package pro.sky.sockswarehouseapplication.service.socksservise;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.sky.sockswarehouseapplication.exceptions.BadRequestException;
import pro.sky.sockswarehouseapplication.exceptions.FileProcessingException;
import pro.sky.sockswarehouseapplication.exceptions.ModelNotFoundException;
import pro.sky.sockswarehouseapplication.model.socks.Color;
import pro.sky.sockswarehouseapplication.model.socks.Socks;
import pro.sky.sockswarehouseapplication.model.socks.SocksSize;
import pro.sky.sockswarehouseapplication.model.transactions.TransactionsType;
import pro.sky.sockswarehouseapplication.service.fileservice.FilesService;
import pro.sky.sockswarehouseapplication.service.transactionsservice.TransactionsService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SocksServiceImpl implements SocksService {

    private final FilesService filesService;
    private final TransactionsService transactionsService;

    private static Map<Long, Socks> socksMap = new LinkedHashMap<>();
    private static Map<Long, Socks> defectiveSocksMap = new LinkedHashMap<>();

    private static Long id = 0L;
    private static Long defectiveId = 0L;
    @Value("${name.of.socks.data.file}")
    private String dataFileName;
    @Value("${name.of.socksfiltered.data.file}")
    private String filteredSocksDataFileName;

    @Value("${name.of.defectivesocks.data.file}")
    private String defectiveSocksDataFileName;

    public SocksServiceImpl(FilesService filesService, TransactionsService transactionsService) {
        this.filesService = filesService;
        this.transactionsService = transactionsService;
    }

    @PostConstruct
    private void init() {
        try {
            filesService.createNewFileIfNotExist(dataFileName);
            filesService.createNewFileIfNotExist(defectiveSocksDataFileName);
            readFromFile();
            readFromFileDefectiveSocks();
        } catch (RuntimeException e) {
            throw new FileProcessingException("Проблема с чтением из файла");
        }
    }

    @Override
    public String getDataFileName() {
        return dataFileName;
    }

    @Override
    public Map<Long, Socks> getAllSocks() {
        if (socksMap.isEmpty()) {
            throw new ModelNotFoundException("Носки на складе отсутствуют");
        } else
            return socksMap;
    }

    private void checkRequest(Socks socks) {
        boolean colorMatch = Arrays.stream(Color.values()).anyMatch(color -> color.equals(socks.getColor()));
        boolean sizeMatch = Arrays.stream(SocksSize.values()).anyMatch(size -> size.equals(socks.getSize()));

        if (!colorMatch) {
            throw new BadRequestException("Такого цвета нет");
        }

        if (!sizeMatch) {
            throw new BadRequestException("Такого размера нет");
        }

        if (socks.getQuantity() <= 0) {
            throw new BadRequestException("Количество должно быть больше нуля");
        }

        if (socks.getCottonPart() < 0 || socks.getCottonPart() > 100) {
            throw new BadRequestException("Значение хлопка должно быть от 0 до 100");
        }

    }

    @Override
    public void addSocks(Socks addedSocks) {
        checkRequest(addedSocks);
        transactionsService.addTransactions(TransactionsType.INCOMING, LocalDateTime.now(), addedSocks);

        if (socksMap.isEmpty() || !socksMap.containsValue(addedSocks)) {
            socksMap.put(id++, addedSocks);

        } else
            for (Socks socks : socksMap.values()) {
                if (socks.equals(addedSocks)) {
                    socks.setQuantity(socks.getQuantity() + addedSocks.getQuantity());
                    break;
                }
            }
        saveToFile(dataFileName, socksMap, id);
    }

    @Override
    public Map<Long, Socks> getSocksFilteredByMinCotton(Color color, double size, int cottonMin) {

        checkRequest(new Socks(color, SocksSize.getSize(size), cottonMin, 1) );

        Map<Long, Socks> collect = socksMap.entrySet().stream()
                .filter(o -> o.getValue().getColor().equals(color))
                .filter(o -> o.getValue().getSize().equals(SocksSize.getSize(size)))
                .filter(o -> o.getValue().getCottonPart() >= cottonMin)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        saveToFile(filteredSocksDataFileName, collect, id);

        if (collect.size() > 0) {
            return collect;
        } else
            throw new ModelNotFoundException("По заданным параметрам носки отсутствуют");
    }

    @Override
    public Map<Long, Socks> getSocksFilteredByMaxCotton(Color color, double size, int cottonMax) {

        checkRequest(new Socks(color, SocksSize.getSize(size), cottonMax, 1) );

        Map<Long, Socks> collect = socksMap.entrySet().stream()
                .filter(o -> o.getValue().getColor().equals(color))
                .filter(o -> o.getValue().getSize().equals(SocksSize.getSize(size)))
                .filter(o -> o.getValue().getCottonPart() <= cottonMax)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        saveToFile(filteredSocksDataFileName, collect, id);

        if (collect.size() > 0) {
            return collect;
        } else
            throw new ModelNotFoundException("По заданным параметрам носки отсутствуют");
    }

    @Override
    public void releaseSocks(Socks releaseSocks) {
        checkRequest(releaseSocks);
        boolean flag = false;
        int count = 0;
        if (!socksMap.containsValue(releaseSocks)) {
            throw new ModelNotFoundException("Таких носков нет на складе");
        } else {
            for (Socks socks : socksMap.values()) {
                if (socks.equals(releaseSocks) && socks.getQuantity() >= releaseSocks.getQuantity()) {
                    socks.setQuantity(socks.getQuantity() - releaseSocks.getQuantity());
                    flag = true;
                    saveToFile(dataFileName, socksMap, id);
                    transactionsService.addTransactions(TransactionsType.OUTGOING, LocalDateTime.now(), releaseSocks);
                    break;
                } else if (socks.equals(releaseSocks) && socks.getQuantity() <= releaseSocks.getQuantity()) {
                    count = socks.getQuantity();
                }
            }
            if (!flag) {
                throw new ModelNotFoundException("Такого количества нет на складе, всего осталось: " + count);
            }
        }
    }

    @Override
    public void deleteDefectiveSocks(Socks defectiveSocks) {
        checkRequest(defectiveSocks);
        boolean flag = false;
        int count = 0;
        if (!socksMap.containsValue(defectiveSocks)) {
            throw new ModelNotFoundException("Таких носков нет на складе");
        } else {
            for (Socks socks : socksMap.values()) {
                if (socks.equals(defectiveSocks) && socks.getQuantity() >= defectiveSocks.getQuantity()) {
                    socks.setQuantity(socks.getQuantity() - defectiveSocks.getQuantity());
                    flag = true;
                    saveToFile(dataFileName, socksMap, id);
                    addDefectiveSocks(defectiveSocks);
                    break;
                } else if (socks.equals(defectiveSocks) && socks.getQuantity() <= defectiveSocks.getQuantity()) {
                    count = socks.getQuantity();
                }
            }
            if (!flag) {
                throw new ModelNotFoundException("Такого количества нет на складе, всего осталось: " + count);
            }
        }
    }

    private void addDefectiveSocks(Socks defectiveSocks) {
        if (!defectiveSocksMap.isEmpty() && defectiveSocksMap.containsValue(defectiveSocks)) {
            for (Socks socks : defectiveSocksMap.values()) {
                if (socks.equals(defectiveSocks)) {
                    socks.setQuantity(socks.getQuantity() + defectiveSocks.getQuantity());
                    break;
                }
            }
        } else {
            defectiveSocksMap.put(defectiveId++, defectiveSocks);
        }
        saveToFile(defectiveSocksDataFileName, defectiveSocksMap, defectiveId);
        transactionsService.addTransactions(TransactionsType.WRITE_OFF, LocalDateTime.now(), defectiveSocks);
    }

    private void saveToFile(String dataFileName, Map<Long, Socks> socksMap, long id) {
        try {
            SocksDataFile dataFile = new SocksDataFile(id, socksMap);
            String json = new ObjectMapper().writeValueAsString(dataFile);
            filesService.saveToFile(json, dataFileName);
        } catch (JsonProcessingException e) {
            throw new FileProcessingException("Неверный запрос");
        }
    }

    private void readFromFile() {
        String json = filesService.readFromFile(dataFileName);
        try {
            SocksDataFile dataFile = new ObjectMapper().readValue(json, new TypeReference<SocksDataFile>() {
            });
            id = dataFile.getLastId();
            socksMap = dataFile.getMap();
        } catch (MismatchedInputException e) {
            e.getMessage();
        } catch (JsonProcessingException e) {
            throw new FileProcessingException("Неверный запрос");
        }
    }

    private void readFromFileDefectiveSocks() {
        String json = filesService.readFromFile(defectiveSocksDataFileName);
        try {
            SocksDataFile dataFile = new ObjectMapper().readValue(json, new TypeReference<SocksDataFile>() {
            });
            defectiveId = dataFile.getLastId();
            defectiveSocksMap = dataFile.getMap();
        } catch (MismatchedInputException e) {
            e.getMessage();
        } catch (JsonProcessingException e) {
            throw new FileProcessingException("Неверный запрос");
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SocksDataFile {
        private long lastId;
        private Map<Long, Socks> map;
    }
}
