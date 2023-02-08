package pro.sky.sockswarehouseapplication.model.socks;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Socks {

    private Color color;
    private SocksSize size;

    @Size(max = 100)
    private int cottonPart;

    @PositiveOrZero(message = "Количество должно быть больше нуля")
    private int quantity;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Socks socks = (Socks) o;
        return cottonPart == socks.cottonPart && color.equals(socks.color) && size.equals(socks.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, size, cottonPart);
    }
}


