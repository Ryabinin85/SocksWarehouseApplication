package pro.sky.sockswarehouseapplication.model.socks;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.util.Objects;


@Entity
@Table(name = "socks")
@Access(value = AccessType.FIELD)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Socks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "color")
    @Enumerated(EnumType.STRING)
    private Color color;

    @Column(name = "size")
    @Enumerated(EnumType.STRING)
    private SocksSize size;

    @Column(name = "cotton_part")
//    @Size(max = 100)
    private int cottonPart;

    @Column(name = "quantity")
    @PositiveOrZero(message = "Количество должно быть больше нуля")
    private int quantity;


    public Socks(Color color, SocksSize size, int cottonPart, int quantity) {
        this.color = color;
        this.size = size;
        this.cottonPart = cottonPart;
        this.quantity = quantity;
    }

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


