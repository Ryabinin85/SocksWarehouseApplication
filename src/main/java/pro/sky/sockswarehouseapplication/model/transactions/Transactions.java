package pro.sky.sockswarehouseapplication.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.HashCodeExclude;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import pro.sky.sockswarehouseapplication.customserializer.CustomTimeDeserializer;
import pro.sky.sockswarehouseapplication.customserializer.CustomTimeSerializer;
import pro.sky.sockswarehouseapplication.model.socks.Socks;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions")
@Access(value = AccessType.FIELD)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TransactionsType type;

    @Column(name = "time")
    @JsonDeserialize(using = CustomTimeDeserializer.class)
    @JsonSerialize(using = CustomTimeSerializer.class)
    private LocalDateTime time;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "socks_id")
    @HashCodeExclude
    private Socks socks;

    public Transactions(TransactionsType type, LocalDateTime time, Socks socks) {
        this.type = type;
        this.time = time;
        this.socks = socks;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transactions that = (Transactions) o;
        return Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }
}
