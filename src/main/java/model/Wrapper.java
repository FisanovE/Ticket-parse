package model;

import lombok.Getter;
import lombok.Setter;
import model.Ticket;

import java.util.List;

@Setter
@Getter
public class Wrapper {
    private List<Ticket> tickets;
}
