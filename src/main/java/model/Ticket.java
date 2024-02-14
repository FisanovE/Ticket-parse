package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import common.Constants;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

//@JsonIgnoreProperties
@Setter
@Getter
public class Ticket {
    private String origin;
    private String origin_name;
    private String destination;
    private String destination_name;

    @JsonFormat(pattern = Constants.DATA_PATTERN)
    private LocalDate departure_date;

    @JsonFormat(pattern = Constants.TIME_PATTERN)
    private LocalTime departure_time;

    @JsonFormat(pattern = Constants.DATA_PATTERN)
    private LocalDate arrival_date;

    @JsonFormat(pattern = Constants.TIME_PATTERN)
    private LocalTime arrival_time;
    private String carrier;
    private int stops;
    private int price;
}
