import model.CityTitle;
import service.TicketService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String pathToDirectory = "E:/";
        String nameInputFile = "tickets.json";
        String nameOutputFile = "result.txt";
        CityTitle departureCityTitle = CityTitle.VVO;
        CityTitle arrivalCityTitle = CityTitle.TLV;
        TicketService ticketService = new TicketService(pathToDirectory, nameInputFile, nameOutputFile,
                departureCityTitle, arrivalCityTitle);
        ticketService.makeResult();
    }


}
