import service.TicketService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "E:/";
        String nameInputFile = "tickets.json";
        String nameOutputFile = "result.txt";
        TicketService ticketService = new TicketService(path, nameInputFile, nameOutputFile);
        ticketService.makeResult();
    }


}
