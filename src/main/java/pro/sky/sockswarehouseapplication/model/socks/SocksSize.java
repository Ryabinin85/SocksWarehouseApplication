package pro.sky.sockswarehouseapplication.model.socks;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum SocksSize {

    XXS, XS, S, M, L, XL, XXL,
    IMMEASURABLE;

    public static SocksSize getSize(double size) {
        if (27 <= size && size <= 29.5) return SocksSize.XXS;
        if (30 <= size && size <= 32.5) return SocksSize.XS;
        if (33 <= size && size <= 35.5) return SocksSize.S;
        if (36 <= size && size <= 38.5) return SocksSize.M;
        if (39 <= size && size <= 41.5) return SocksSize.L;
        if (42 <= size && size <= 44.5) return SocksSize.XL;
        if (45 <= size && size <= 47.5) return SocksSize.XXL;
        else return SocksSize.IMMEASURABLE;
    }
}
