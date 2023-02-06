package pro.sky.sockswarehouseapplication.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.HashCodeExclude;
import pro.sky.sockswarehouseapplication.customserializer.CustomTimeDeserializer;
import pro.sky.sockswarehouseapplication.customserializer.CustomTimeSerializer;
import pro.sky.sockswarehouseapplication.model.socks.Socks;

import java.time.LocalDateTime;
import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transactions {

    private TransactionsType type;

    @JsonDeserialize(using = CustomTimeDeserializer.class)
    @JsonSerialize(using = CustomTimeSerializer.class)
    private LocalDateTime time;

    @HashCodeExclude
    private Socks socks;

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
