package pro.sky.sockswarehouseapplication.model.socks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum Color {
    white,
    black,
    red,
    blue,
    green
}
