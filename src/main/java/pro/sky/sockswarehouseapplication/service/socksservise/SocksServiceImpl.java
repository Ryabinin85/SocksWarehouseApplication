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
import pro.sky.sockswarehouseapplication.dao.SocksDAO;
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

    private final SocksDAO socksDAO;

    private static Map<Long, Socks> socksMap = new LinkedHashMap<>();

    public static Map<Long, Socks> getSocksMap() {
        return socksMap;
    }

    private static Map<Long, Socks> defectiveSocksMap = new LinkedHashMap<>();

    private static Long id = 0L;
    private static Long defectiveId = 0L;
    @Value("${name.of.socks.data.file}")
    private String dataFileName;
    @Value("${name.of.socksfiltered.data.file}")
    private String filteredSocksDataFileName;

    @Value("${name.of.defectivesocks.data.file}")
    private String defectiveSocksDataFileName;

    public SocksServiceImpl(FilesService filesService,
                            TransactionsService transactionsService,
                            SocksDAO socksDAO) {
        this.filesService = filesService;
        this.transactionsService = transactionsService;
        this.socksDAO = socksDAO;
    }

    @PostConstruct
    private void init() {
        try {
            filesService.createNewFileIfNotExist(dataFileName);
            filesService.createNewFileIfNotExist(defectiveSocksDataFileName);
            readFromFile();
            readFromFileDefectiveSocks();
        } catch (RuntimeException e) {
            throw new FileProcessingException("???????????????? ?? ?????????????? ???? ??????????");
        }
    }

    @Override
    public String getDataFileName() {
        return dataFileName;
    }

    @Override
    public Map<Long, Socks> getAllSocks() {
        if (socksMap.isEmpty()) {
            throw new ModelNotFoundException("?????????? ???? ???????????? ??????????????????????");
        } else
            return socksMap;
    }

    private void checkRequest(Socks socks) {
        boolean colorMatch = Arrays.stream(Color.values()).anyMatch(color -> color.equals(socks.getColor()));
        boolean sizeMatch = Arrays.stream(SocksSize.values()).anyMatch(size -> size.equals(socks.getSize()));

        if (!colorMatch) {
            throw new BadRequestException("???????????? ?????????? ??????");
        }

        if (!sizeMatch) {
            throw new BadRequestException("???????????? ?????????????? ??????");
        }

        if (socks.getQuantity() <= 0) {
            throw new BadRequestException("???????????????????? ???????????? ???????? ???????????? ????????");
        }

        if (socks.getCottonPart() < 0 || socks.getCottonPart() > 100) {
            throw new BadRequestException("???????????????? ???????????? ???????????? ???????? ???? 0 ???? 100");
        }

    }

    @Override
    public void addSocks(Socks addedSocks) {
        checkRequest(addedSocks);

        if (socksMap.isEmpty() || !socksMap.containsValue(addedSocks)) {
            addedSocks.setId(id);
            socksMap.put(id++, addedSocks);
            socksDAO.save(addedSocks);
            transactionsService.addTransactions(TransactionsType.INCOMING, LocalDateTime.now(), addedSocks);

        } else {
            for (Socks socks : socksMap.values()) {
                if (socks.equals(addedSocks)) {
                    socks.setQuantity(socks.getQuantity() + addedSocks.getQuantity());
                    socksDAO.update(socks, socks.getId());
                    transactionsService.addTransactions(TransactionsType.INCOMING, LocalDateTime.now(), socks);
                    break;
                }
            }
        }
        saveToFile(dataFileName, socksMap, id);
    }

    @Override
    public Map<Long, Socks> getSocksFilteredByMinCotton(Color color, double size, int cottonMin) {

        checkRequest(new Socks(color, SocksSize.getSize(size), cottonMin, 1));

        Map<Long, Socks> collect = socksMap.entrySet().stream()
                .filter(o -> o.getValue().getColor().equals(color))
                .filter(o -> o.getValue().getSize().equals(SocksSize.getSize(size)))
                .filter(o -> o.getValue().getCottonPart() >= cottonMin)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        saveToFile(filteredSocksDataFileName, collect, id);

        if (collect.size() > 0) {
            return collect;
        } else
            throw new ModelNotFoundException("???? ???????????????? ???????????????????? ?????????? ??????????????????????");
    }

    @Override
    public Map<Long, Socks> getSocksFilteredByMaxCotton(Color color, double size, int cottonMax) {

        checkRequest(new Socks(color, SocksSize.getSize(size), cottonMax, 1));

        Map<Long, Socks> collect = socksMap.entrySet().stream()
                .filter(o -> o.getValue().getColor().equals(color))
                .filter(o -> o.getValue().getSize().equals(SocksSize.getSize(size)))
                .filter(o -> o.getValue().getCottonPart() <= cottonMax)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        saveToFile(filteredSocksDataFileName, collect, id);

        if (collect.size() > 0) {
            return collect;
        } else
            throw new ModelNotFoundException("???? ???????????????? ???????????????????? ?????????? ??????????????????????");
    }

    @Override
    public void releaseSocks(Socks releaseSocks) {
        checkRequest(releaseSocks);
        boolean flag = false;
        int count = 0;
        if (!socksMap.containsValue(releaseSocks)) {
            throw new ModelNotFoundException("?????????? ???????????? ?????? ???? ????????????");
        } else {
            for (Socks socks : socksMap.values()) {
                if (socks.equals(releaseSocks) && socks.getQuantity() >= releaseSocks.getQuantity()) {
                    socks.setQuantity(socks.getQuantity() - releaseSocks.getQuantity());
                    socksDAO.update(socks, socks.getId());
                    flag = true;
                    saveToFile(dataFileName, socksMap, id);
                    transactionsService.addTransactions(TransactionsType.OUTGOING, LocalDateTime.now(), socks);
                    break;
                } else if (socks.equals(releaseSocks) && socks.getQuantity() <= releaseSocks.getQuantity()) {
                    count = socks.getQuantity();
                }
            }
            if (!flag) {
                throw new ModelNotFoundException("???????????? ???????????????????? ?????? ???? ????????????, ?????????? ????????????????: " + count);
            }
        }
    }

    @Override
    public void deleteDefectiveSocks(Socks defectiveSocks) {
        checkRequest(defectiveSocks);
        boolean flag = false;
        int count = 0;
        if (!socksMap.containsValue(defectiveSocks)) {
            throw new ModelNotFoundException("?????????? ???????????? ?????? ???? ????????????");
        } else {
            for (Socks socks : socksMap.values()) {
                if (socks.equals(defectiveSocks) && socks.getQuantity() >= defectiveSocks.getQuantity()) {
                    socks.setQuantity(socks.getQuantity() - defectiveSocks.getQuantity());
                    flag = true;
                    socksDAO.update(socks, socks.getId());
                    transactionsService.addTransactions(TransactionsType.WRITE_OFF, LocalDateTime.now(), socks);
                    saveToFile(dataFileName, socksMap, id);
                    addDefectiveSocks(defectiveSocks);
                    break;
                } else if (socks.equals(defectiveSocks) && socks.getQuantity() <= defectiveSocks.getQuantity()) {
                    count = socks.getQuantity();
                }
            }
            if (!flag) {
                throw new ModelNotFoundException("???????????? ???????????????????? ?????? ???? ????????????, ?????????? ????????????????: " + count);
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
    }

    private void saveToFile(String dataFileName, Map<Long, Socks> socksMap, long id) {
        try {
            SocksDataFile dataFile = new SocksDataFile(id, socksMap);
            String json = new ObjectMapper().writeValueAsString(dataFile);
            filesService.saveToFile(json, dataFileName);
        } catch (JsonProcessingException e) {
            throw new FileProcessingException("???????????????? ????????????");
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
            throw new FileProcessingException("???????????????? ????????????");
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
            throw new FileProcessingException("???????????????? ????????????");
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
