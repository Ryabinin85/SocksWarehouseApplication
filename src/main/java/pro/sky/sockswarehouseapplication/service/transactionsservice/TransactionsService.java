package pro.sky.sockswarehouseapplication.service.transactionsservice;

import pro.sky.sockswarehouseapplication.model.socks.Socks;
import pro.sky.sockswarehouseapplication.model.transactions.TransactionsType;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

public interface TransactionsService {
    void addTransactions(TransactionsType type, LocalDateTime time, Socks socks);

    Path createReport() throws IOException, NoSuchElementException;
}
