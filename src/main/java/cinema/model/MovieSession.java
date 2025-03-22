package cinema.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MovieSession {
    private final String sessionId;                  // Идентификатор сеанса
    private final String movieTitle;                 // Название фильма
    private final Theater theater;                   // Кинозал
    private final LocalDateTime dateTime;            // Дата и время сеанса
    private final Map<Seat, SeatStatus> seatStatusMap;  // Карта статусов мест

    public MovieSession(String sessionId, String movieTitle, Theater theater, LocalDateTime dateTime) {
        if (sessionId == null || movieTitle == null || theater == null || dateTime == null) {
            throw new IllegalArgumentException("Все параметры должны быть указаны");
        }
        this.sessionId = sessionId;
        this.movieTitle = movieTitle;
        this.theater = theater;
        this.dateTime = dateTime;
        this.seatStatusMap = initializeSeatStatus();
    }

    private Map<Seat, SeatStatus> initializeSeatStatus() {
        Map<Seat, SeatStatus> statusMap = new HashMap<>();
        for (Seat seat : theater.getAllSeats()) {
            statusMap.put(seat, SeatStatus.FREE);
        }
        return statusMap;
    }

    public boolean isSeatNotValid(Seat seat) {
        return seat.getRow() < 1 || seat.getRow() > theater.getRows() ||
               seat.getNumber() < 1 || seat.getNumber() > theater.getSeatsPerRow();
    }

    public SeatStatus getSeatStatus(Seat seat) {
        if (isSeatNotValid(seat)) {
            throw new IllegalArgumentException("Неверное место: " + seat);
        }
        return seatStatusMap.getOrDefault(seat, SeatStatus.FREE);
    }

    public void setSeatStatus(Seat seat, SeatStatus status) {
        if (isSeatNotValid(seat)) {
            throw new IllegalArgumentException("Неверное место: " + seat);
        }
        seatStatusMap.put(seat, status);
    }

    public List<Seat> getAvailableSeats() {
        return seatStatusMap.entrySet().stream()
                .filter(entry -> entry.getValue() == SeatStatus.FREE)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Seat> getReservedSeats() {
        return seatStatusMap.entrySet().stream()
                .filter(entry -> entry.getValue() == SeatStatus.RESERVED)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Seat> getSoldSeats() {
        return seatStatusMap.entrySet().stream()
                .filter(entry -> entry.getValue() == SeatStatus.SOLD)
                .map(Map.Entry::getKey)
                .toList();
    }
}
