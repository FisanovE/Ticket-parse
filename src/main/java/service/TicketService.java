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
    private final String pathDirectory;
    private final String nameInputFile;
    private final String nameOutputFile;
    private final CityTitle departureCityTitle;
    private final CityTitle arrivalCityTitle;
    private List<Ticket> tickets;
    private List<Ticket> selectedTickets;
    private Map<String, Long> timeValues = new HashMap<>();
    private double middlePrice = 0.0;
    private double medianPrice = 0.0;


    public TicketService(String pathDirectory, String nameInputFile, String nameOutputFile, CityTitle departureCityTitle,
                         CityTitle arrivalCityTitle) {
        this.pathDirectory = pathDirectory;
        this.nameInputFile = nameInputFile;
        this.nameOutputFile = nameOutputFile;
        this.departureCityTitle = departureCityTitle;
        this.arrivalCityTitle = arrivalCityTitle;
    }

    public void makeResult() throws IOException {
        tickets = parseJson();
        selectedTickets = selectTickets(tickets);

        for (Ticket ticket : selectedTickets) {
            if (!timeValues.containsKey(ticket.getCarrier())
                    || calculateDifferenceOfTimeInMinutes(ticket) < timeValues.get(ticket.getCarrier())) {
                timeValues.put(ticket.getCarrier(), calculateDifferenceOfTimeInMinutes(ticket));
            }
        }

        StringBuilder stringBuilder = new StringBuilder("Результат");
        stringBuilder.append("\n")
                .append("Минимальное время полета между городами ").append(getNameOfCity(departureCityTitle))
                .append(" и ")
                .append(getNameOfCity(arrivalCityTitle))
                .append(" для каждого авиаперевозчика:")
                .append("\n");

        for (String carrier : timeValues.keySet()) {
            stringBuilder.append(carrier)
                    .append(" - ")
                    .append(timeValues.get(carrier) / 60)
                    .append(" час ")
                    .append(timeValues.get(carrier) % 60)
                    .append(" мин")
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

        System.out.println(stringBuilder);

        FileWriter fin = new FileWriter(pathDirectory + nameOutputFile);
        fin.write(stringBuilder.toString());
        fin.close();
    }

    private List<Ticket> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        File file = new File(pathDirectory + nameInputFile);
        Wrapper wrapper = mapper.readValue(file, Wrapper.class);
        return wrapper.getTickets();
    }

    private List<Ticket> selectTickets(List<Ticket> tickets) {
        return tickets.stream()
                .filter(ticket -> ticket.getOrigin().equals(departureCityTitle)
                        && ticket.getDestination().equals(arrivalCityTitle))
                .collect(Collectors.toList());
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

    private long calculateDifferenceOfTimeInMinutes(Ticket ticket) {

        LocalDateTime departureTimeLocal = LocalDateTime.of(ticket.getDeparture_date(), ticket.getDeparture_time());
        ZonedDateTime departureZoneTime = departureTimeLocal.atZone(ZoneId.of(getZoneOfCity(departureCityTitle)));
        long departureTimeInHour = departureZoneTime.toInstant().toEpochMilli() / 1000 / 60;

        LocalDateTime arrivalTimeLocal = LocalDateTime.of(ticket.getArrival_date(), ticket.getArrival_time());
        ZonedDateTime arrivalZoneTime = arrivalTimeLocal.atZone(ZoneId.of(getZoneOfCity(arrivalCityTitle)));
        long arrivalTimeInHour = arrivalZoneTime.toInstant().toEpochMilli() / 1000 / 60;

        return arrivalTimeInHour - departureTimeInHour;
    }

    private String getZoneOfCity(CityTitle title) {
        switch (title) {
            case LRN:
                return "Asia/Nicosia";
            case TLV:
                return "Asia/Tel_Aviv";
            case UFA:
                return "Asia/Yekaterinburg";
            case VVO:
                return "Asia/Vladivostok";
            default:
                throw new UnsupportedStatusException("Unknown title: " + title);
        }
    }

    private String getNameOfCity(CityTitle title) {
        switch (title) {
            case LRN:
                return "Ларнака";
            case TLV:
                return "Тель-Авив";
            case UFA:
                return "Уфа";
            case VVO:
                return "Владивосток";
            default:
                throw new UnsupportedStatusException("Unknown title: " + title);
        }
    }
}
