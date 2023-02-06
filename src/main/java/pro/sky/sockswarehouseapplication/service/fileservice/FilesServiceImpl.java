package pro.sky.sockswarehouseapplication.service.fileservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.sky.sockswarehouseapplication.exceptions.FileProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FilesServiceImpl implements FilesService {

    @Value("${path.to.data.file}")
    private String dataFilePath;
    @Value("${path.to.data.tempfile}")
    private String dataTempFilePath;

    @Override
    public void saveToFile(String json, String dataFileName) {
        try {
            cleanDataFile(dataFileName);
            Files.writeString(Path.of(dataFilePath, dataFileName), json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String readFromFile(String dataFileName) {
        try {
            return Files.readString(Path.of(dataFilePath, dataFileName));

        } catch (IOException e) {
            throw new FileProcessingException("Файл отсутствует");
        }
    }

    @Override
    public void cleanDataFile(String dataFileName) {
        try {
            Path path = Path.of(dataFilePath, dataFileName);
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getDataFile(String dataFileName) {
        return new File(dataFilePath + "/" + dataFileName);
    }

    @Override
    public Path createTempFile(String suffix) {
        try {
            if (Files.notExists(Path.of(dataTempFilePath))) {
                Files.createDirectory(Path.of(dataTempFilePath));
            }
            return Files.createTempFile(Path.of(dataTempFilePath), "temp", suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createNewFileIfNotExist(String dataFileName) {
        try {
            Path path = Path.of(dataFilePath, dataFileName);
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
