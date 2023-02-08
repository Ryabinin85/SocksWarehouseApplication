package pro.sky.sockswarehouseapplication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.sockswarehouseapplication.exceptions.FileProcessingException;
import pro.sky.sockswarehouseapplication.service.fileservice.FilesService;
import pro.sky.sockswarehouseapplication.service.socksservise.SocksService;
import pro.sky.sockswarehouseapplication.service.transactionsservice.TransactionsService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Работа с файлами", description = "CRUD операции с файлами")
public class FilesController {

    private final FilesService filesService;
    private final SocksService socksService;
    private final TransactionsService transactionsService;

    public FilesController(FilesService filesService, SocksService socksService, TransactionsService transactionsService) {
        this.filesService = filesService;
        this.socksService = socksService;
        this.transactionsService = transactionsService;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Загрузка файла с носками с сервера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные по носкам получены")})
    public ResponseEntity<InputStreamResource> downloadFile() {
        File dataFile = filesService.getDataFile(socksService.getDataFileName());

        if (dataFile.exists()) {
            InputStreamResource resource;
            try {
                resource = new InputStreamResource(new FileInputStream(dataFile));
            } catch (FileNotFoundException e) {
                throw new FileProcessingException("Файл не найден");
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"allsocks.json\"")
                    .contentLength(dataFile.length())
                    .body(resource);
        } else {
            throw new FileProcessingException("Файл не найден");
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузка файла с носками на сервер")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные по носкам загружены")})
    public ResponseEntity<Void> uploadFile(@RequestParam MultipartFile file) {

        filesService.cleanDataFile(socksService.getDataFileName());
        File dataFile = filesService.getDataFile(socksService.getDataFileName());

        try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
             FileOutputStream fos = new FileOutputStream(dataFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            while (bis.read(buffer) > 0) {
                bos.write(buffer);
            }
            return ResponseEntity.ok()
                    .contentLength(dataFile.length())
                    .build();

        } catch (IOException e) {
            throw new FileProcessingException("Ошибка загрузки файла");
        }
    }

    @GetMapping(value = "/report", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Скачать отчет по транзакциям")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отчет по транзакциям получен")})
    public ResponseEntity<Object> downloadReport() {

        try {
            Path path = transactionsService.createReport();
            if (Files.size(path) == 0) {
                return ResponseEntity.noContent().build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.txt\"")
                    .contentLength(Files.size(path))
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }
}
