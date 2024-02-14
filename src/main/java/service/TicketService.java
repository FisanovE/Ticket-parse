package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exeptions.UnsupportedStatusException;
import model.CityTitle;
import model.Ticket;
import model.Wrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TicketService {
    private final String path;
    private final String nameInputFile;
    private final String nameOutputFile;
    private String departureCity = "Владивосток";
    private String arrivalCity = "Тель-Авив";
    private ObjectMapper mapper = new ObjectMapper();
    private File file;
    private Wrapper wrapper;
    private List<Ticket> tickets;
    private List<Ticket> selectedTickets;
    private Map<String, Long> timeValues = new HashMap<>();
    private double middlePrice = 0.0;
    private double medianPrice = 0.0;


    public TicketService(String path, String nameInputFile, String nameOutputFile) {
        this.path = path;
        this.nameInputFile = nameInputFile;
        this.nameOutputFile = nameOutputFile;
    }

    public void makeResult() throws IOException {
        file = new File(path + nameInputFile);
        mapper.registerModule(new JavaTimeModule());
        wrapper = mapper.readValue(file, Wrapper.class);
        tickets = wrapper.getTickets();

        selectedTickets = tickets.stream()
                .filter(ticket -> ticket.getOrigin_name().equals(departureCity) && ticket.getDestination_name().equals(arrivalCity))
                .collect(Collectors.toList());

        for (Ticket ticket : selectedTickets) {
            if (!timeValues.containsKey(ticket.getCarrier())
                    || calculateTimeDifference(ticket) < timeValues.get(ticket.getCarrier())) {
                timeValues.put(ticket.getCarrier(), calculateTimeDifference(ticket));
            }
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

        middlePrice = calculateMiddlePrice(selectedTickets);
        medianPrice = calculateMedianPrice(selectedTickets);
        stringBuilder.append("Средняя цена билета: ")
                .append(middlePrice)
                .append("\n");

        stringBuilder.append("Медианная цена билета: ")
                .append(medianPrice)
                .append("\n");

        stringBuilder.append("Разница между средней и медианой ценой билета: ")
                .append(middlePrice - medianPrice)
                .append("\n");

        System.out.println(stringBuilder.toString());

        FileWriter fin = new FileWriter(path + nameOutputFile);
        fin.write(stringBuilder.toString());
        fin.close();
    }

    private double calculateMiddlePrice(List<Ticket> tickets) {
        double allPrice = 0.0;
        for (Ticket ticket : tickets) {
            allPrice += ticket.getPrice();
        }
        return allPrice / tickets.size();
    }

    private double calculateMedianPrice(List<Ticket> tickets) {
        List<Ticket> sortedTickets = tickets.stream()
                .sorted(Comparator.comparingDouble(Ticket::getPrice))
                .collect(Collectors.toList());
        if (sortedTickets.size() % 2 == 0) {
            double a = sortedTickets.get(sortedTickets.size() / 2 - 1).getPrice();
            double b = sortedTickets.get(sortedTickets.size() / 2).getPrice();
            return (a + b) / 2;
        } else {
            return sortedTickets.get(sortedTickets.size() / 2).getPrice();
        }
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
