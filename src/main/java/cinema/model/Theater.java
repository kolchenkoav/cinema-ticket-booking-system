package cinema.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Theater {
    private final String name;            // Название зала
    private final int rows;               // Количество рядов
    private final int seatsPerRow;        // Количество мест в ряду
    private final List<Seat> allSeats;    // Список всех мест

    public Theater(String name, int rows, int seatsPerRow) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название зала не может быть пустым");
        }
        if (rows <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("Количество рядов и мест должно быть положительным");
        }
        this.name = name;
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
        this.allSeats = initializeSeats();
    }

    private List<Seat> initializeSeats() {
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= rows; row++) {
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                seats.add(new Seat(row, seatNum));
            }
        }
        return seats;
    }

    public List<Seat> getAllSeats() {
        return new ArrayList<>(allSeats);
    }
}
