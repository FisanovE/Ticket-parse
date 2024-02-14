package service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exeptions.UnsupportedStatusException;
import model.CityTitle;
import model.Ticket;
import model.Wrapper;

public class TicketService {
    String path;
    String departureCity = "Владивосток";
    String arrivalCity = "Тель-Авив";
    Map<String, Long> timeValues = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    File file;
    Wrapper wrapper;
    List<Ticket> tickets;
    double allPrice = 0.0;

    public TicketService(String path) {
        this.path = path;
    }

    public void makeResult() throws IOException {
        file = new File(path);
        mapper.registerModule(new JavaTimeModule());
        wrapper = mapper.readValue(file, Wrapper.class);
        tickets = wrapper.getTickets();

        List<Ticket> selectedTickets = tickets.stream()
                .filter(ticket -> ticket.getOrigin_name().equals(departureCity) && ticket.getDestination_name().equals(arrivalCity))
                .collect(Collectors.toList());

        for (Ticket ticket : selectedTickets) {
            if (!timeValues.containsKey(ticket.getCarrier())
                    || calculateTimeDifference(ticket) < timeValues.get(ticket.getCarrier())) {
                timeValues.put(ticket.getCarrier(), calculateTimeDifference(ticket));
            }
            allPrice += ticket.getPrice();
        }

        StringBuilder stringBuilder = new StringBuilder("Результат");
        stringBuilder.append("\n")
                .append("Минимальное время полета между городами ").append(departureCity).append(" и ").append(arrivalCity)
                .append(" для каждого авиаперевозчика:")
                .append("\n");

        for (String carrier : timeValues.keySet()) {
            stringBuilder.append(carrier)
                    .append(" - ")
                    .append(timeValues.get(carrier))
                    .append("\n");
        }

        stringBuilder.append("Средняя цена билета: ")
                .append(allPrice / selectedTickets.size())
                .append("\n");

        stringBuilder.append("Медианная цена билета: ")
                .append(allPrice / selectedTickets.size())
                .append("\n");

        System.out.println(stringBuilder.toString());

        FileWriter fin = new FileWriter("e:/result.txt");
        fin.write(stringBuilder.toString());
        fin.close();
    }

    private long calculateTimeDifference(Ticket ticket) {

        LocalDateTime departureTimeLocal = LocalDateTime.of(ticket.getDeparture_date(), ticket.getDeparture_time());
        ZonedDateTime departureZoneTime = departureTimeLocal.atZone(ZoneId.of(getZoneOfCity(CityTitle.from(ticket.getOrigin()))));
        long departureTimeToHour = departureZoneTime.toInstant().toEpochMilli() / 1000 / 60;

        LocalDateTime arrivalTimeLocal = LocalDateTime.of(ticket.getArrival_date(), ticket.getArrival_time());
        ZonedDateTime arrivalZoneTime = arrivalTimeLocal.atZone(ZoneId.of(getZoneOfCity(CityTitle.from(ticket.getDestination()))));
        long arrivalTimeToHour = arrivalZoneTime.toInstant().toEpochMilli() / 1000 / 60;

        return arrivalTimeToHour - departureTimeToHour;
    }

    private String getZoneOfCity(CityTitle title) {

        switch (title) {
            case TLV:
                return "Asia/Tel_Aviv";
            case VVO:
                return "Asia/Vladivostok";
            case UFA:
                return "Asia/Yekaterinburg";
            case LRN:
                return "Asia/Nicosia";
            default:
                throw new UnsupportedStatusException("Unknown title: " + title);
        }
    }
}
