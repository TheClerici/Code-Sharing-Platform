package platform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "Codes")
@NoArgsConstructor
public class CodeModel {

    @Id
    @JsonIgnore
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    @JsonProperty
    private String code;

    @Getter
    @Setter
    private long time;

    @Getter
    @Setter
    private long views;

    @Getter
    @Setter
    @JsonIgnore
    private boolean restrictedView;

    @Getter
    @Setter
    @JsonIgnore
    private boolean restrictedTime;

    @Getter
    @Setter
    @JsonIgnore
    private LocalDateTime localDateTime;

    @Getter
    @Setter
    private String date;
}
