package pro.sky.sockswarehouseapplication.service.socksservise;


import pro.sky.sockswarehouseapplication.model.socks.Color;
import pro.sky.sockswarehouseapplication.model.socks.Socks;

import java.util.Map;

public interface SocksService {
    String getDataFileName();

    Map<Long, Socks> getAllSocks();

    boolean addSocks(Socks socks);

    Map<Long, Socks> getSocksFilteredByMinCotton(Color color, double size, int cottonMin);

    Map<Long, Socks> getSocksFilteredByMaxCotton(Color color, double size, int cottonMax);

    void releaseSocks(Socks socks);

    void deleteDefectiveSocks(Socks socks);
}
