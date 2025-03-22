package cinema.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Ticket {
    private final String ticketId;            // Идентификатор билета
    private final MovieSession session;       // Сеанс
    private final Seat seat;                  // Место
    private final LocalDateTime issueTime;    // Время выдачи билета
    private boolean isActive;                 // Активен ли билет
    private final boolean isReservation;      // Бронирование или продажа

    public Ticket(String ticketId, MovieSession session, Seat seat, boolean isReservation) {
        if (ticketId == null || session == null || seat == null) {
            throw new IllegalArgumentException("Все параметры должны быть указаны");
        }
        this.ticketId = ticketId;
        this.session = session;
        this.seat = seat;
        this.issueTime = LocalDateTime.now();
        this.isActive = true;
        this.isReservation = isReservation;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isReservation() {
        return isReservation;
    }

    @Override
    public String toString() {
        String statusReservation = isReservation ? "Забронирован" : "Продан";
        String status = isActive ? statusReservation : "Недействителен";
        return "Билет № " + ticketId + "\n" +
                "Фильм: " + session.getMovieTitle() + "\n" +
                "Сеанс: " + session.getDateTime() + "\n" +
                "Место: " + seat + "\n" +
                "Статус: " + status;
    }
}

