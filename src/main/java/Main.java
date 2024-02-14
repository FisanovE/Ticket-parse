import service.TicketService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "E:/tickets.json";
        TicketService ticketService = new TicketService(path);
        ticketService.makeResult();
    }


}
