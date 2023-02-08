package pro.sky.sockswarehouseapplication.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.sky.sockswarehouseapplication.model.socks.Color;
import pro.sky.sockswarehouseapplication.model.socks.Socks;
import pro.sky.sockswarehouseapplication.service.socksservise.SocksService;

import java.util.Map;

@RestController
@RequestMapping("/api/socks")
@Tag(name = "Носки", description = "CRUD операции с носками")
public class SocksController {

    private final SocksService socksService;

    public SocksController(SocksService socksService) {
        this.socksService = socksService;
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех ноcков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список всех носков получен")})
    public ResponseEntity<Map<Long, Socks>> getSocks() {
        return ResponseEntity.ok(socksService.getAllSocks());
    }

    @GetMapping(value = "/cottonmin", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение ноcков, отсортированных по цвету, размеру, минимальному значению содержания хлопка")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список носков получен")})
    @Parameters(value = {
            @Parameter(name = "color", description = "цвет носков"),
            @Parameter(name = "size", description = "размер от 27 до 47,5"),
            @Parameter(name = "cottonmin", description = "минимальное содержание хлопка от 0 до 100")
    })
    public ResponseEntity<Map<Long, Socks>> getSocksFilteredByMinCotton(
            @RequestParam("color") Color color,
            @RequestParam("size") double size,
            @RequestParam("cottonmin") int cottonMin) {

        return ResponseEntity.ok(socksService.getSocksFilteredByMinCotton(color, size, cottonMin));
    }

    @GetMapping(value = "/cottonmax", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение ноcков, отсортированных по цвету, размеру, максимальному значению содержания хлопка")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список носков получен")})
    @Parameters(value = {
            @Parameter(name = "color", description = "цвет носков"),
            @Parameter(name = "size", description = "размер от 27 до 47,5"),
            @Parameter(name = "cottonmax", description = "минимальное содержание хлопка от 0 до 100")
    })
    public ResponseEntity<Map<Long, Socks>> getSocksFilteredByMaxCotton(
            @RequestParam("color") Color color,
            @RequestParam("size") double size,
            @RequestParam("cottonmax") int cottonMax) {

        return ResponseEntity.ok(socksService.getSocksFilteredByMaxCotton(color, size, cottonMax));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление ноcков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Носки добавлены")})
    public ResponseEntity<Void> addSocks(@RequestBody Socks socks) {

        socksService.addSocks(socks);
        return ResponseEntity.ok().build();
    }


    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Выдача ноcков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Носки выданы")})
    public ResponseEntity<Void> releaseSocks(@RequestBody Socks socks) {

        socksService.releaseSocks(socks);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Списание испорченных ноcков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Носки списаны")})
    public ResponseEntity<Void> deleteDefectiveSocks(@RequestBody Socks socks) {

        socksService.deleteDefectiveSocks(socks);
        return ResponseEntity.ok().build();
    }
}