package cinema.service;

import cinema.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketManagementSystem {
    private final Map<String, MovieSession> sessions;  // Мапа сеансов (id -> сеанс)
    private final Map<String, Ticket> tickets;         // Мапа билетов (id -> билет)
    private int ticketCounter;                         // Счетчик для генерации ID билетов
    private static final String SESSION_NOT_FOUND = "Сеанс не найден: ";

    public TicketManagementSystem() {
        this.sessions = new HashMap<>();
        this.tickets = new HashMap<>();
        this.ticketCounter = 1000;
    }

    /**
     * Создает новый киносеанс
     */
    public MovieSession createMovieSession(String movieTitle, Theater theater, LocalDateTime dateTime) {
        String sessionId = "S" + (sessions.size() + 1);
        MovieSession session = new MovieSession(sessionId, movieTitle, theater, dateTime);
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * Возвращает список всех доступных сеансов
     */
    public List<MovieSession> getAvailableSessions() {
        return new ArrayList<>(sessions.values());
    }

    /**
     * Возвращает сеанс по ID
     */
    public MovieSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Покупка билета на указанное место
     */
    public Ticket buyTicket(String sessionId, int row, int seatNumber) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID сеанса не может быть пустым");
        }

        MovieSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(SESSION_NOT_FOUND + sessionId);
        }

        Seat seat = new Seat(row, seatNumber);
        if (session.isSeatNotValid(seat)) {
            throw new IllegalArgumentException("Неверное место: " + seat);
        }

        SeatStatus status = session.getSeatStatus(seat);
        if (status != SeatStatus.FREE) {
            throw new IllegalStateException("Место уже " +
                    (status == SeatStatus.SOLD ? "продано" : "забронировано"));
        }

        String ticketId = generateTicketId();
        Ticket ticket = new Ticket(ticketId, session, seat, false);
        session.setSeatStatus(seat, SeatStatus.SOLD);
        tickets.put(ticketId, ticket);

        return ticket;
    }

    /**
     * Бронирование места
     */
    public Ticket reserveTicket(String sessionId, int row, int seatNumber) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID сеанса не может быть пустым");
        }

        MovieSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(SESSION_NOT_FOUND + sessionId);
        }

        Seat seat = new Seat(row, seatNumber);
        if (session.isSeatNotValid(seat)) {
            throw new IllegalArgumentException("Неверное место: " + seat);
        }

        SeatStatus status = session.getSeatStatus(seat);
        if (status != SeatStatus.FREE) {
            throw new IllegalStateException("Место уже " +
                    (status == SeatStatus.SOLD ? "продано" : "забронировано"));
        }

        String ticketId = generateTicketId();
        Ticket ticket = new Ticket(ticketId, session, seat, true);
        session.setSeatStatus(seat, SeatStatus.RESERVED);
        tickets.put(ticketId, ticket);

        return ticket;
    }

    /**
     * Отмена билета (возврат или отмена брони)
     */
    public void cancelTicket(String ticketId) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Билет не найден: " + ticketId);
        }

        if (!ticket.isActive()) {
            throw new IllegalStateException("Билет уже отменен");
        }

        MovieSession session = ticket.getSession();
        Seat seat = ticket.getSeat();

        session.setSeatStatus(seat, SeatStatus.FREE);
        ticket.setActive(false);
    }

    /**
     * Подтверждение брони (превращение брони в проданный билет)
     */
    public Ticket confirmReservation(String ticketId) {
        Ticket reservationTicket = tickets.get(ticketId);
        if (reservationTicket == null) {
            throw new IllegalArgumentException("Билет не найден: " + ticketId);
        }

        if (!reservationTicket.isActive()) {
            throw new IllegalStateException("Билет недействителен");
        }

        if (!reservationTicket.isReservation()) {
            throw new IllegalStateException("Билет уже продан");
        }

        MovieSession session = reservationTicket.getSession();
        Seat seat = reservationTicket.getSeat();

        // Создаем новый проданный билет на основе брони
        String newTicketId = generateTicketId();
        Ticket soldTicket = new Ticket(newTicketId, session, seat, false);
        tickets.put(newTicketId, soldTicket);

        // Отменяем бронь, но сохраняем в системе для истории
        reservationTicket.setActive(false);

        // Обновляем статус места
        session.setSeatStatus(seat, SeatStatus.SOLD);

        return soldTicket;
    }

    /**
     * Получение списка доступных мест для конкретного сеанса
     */
    public List<Seat> getAvailableSeats(String sessionId) {
        MovieSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(SESSION_NOT_FOUND + sessionId);
        }

        return session.getAvailableSeats();
    }

    /**
     * Получение билета по ID
     */
    public Ticket getTicket(String ticketId) {
        return tickets.get(ticketId);
    }

    /**
     * Получение всех активных билетов
     */
    public List<Ticket> getAllActiveTickets() {
        return tickets.values().stream()
                .filter(Ticket::isActive)
                .toList();
    }

    /**
     * Генерация уникального ID билета
     */
    private String generateTicketId() {
        return "T" + (++ticketCounter);
    }

    /**
     * Выводит на консоль схему мест в зале для указанного сеанса в виде псевдографики
     * @param sessionId ID сеанса
     */
    public void printSeatingChart(String sessionId) {
        // Получаем сеанс по ID
        MovieSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(SESSION_NOT_FOUND + sessionId);
        }

        Theater theater = session.getTheater();
        int rows = theater.getRows();
        int seatsPerRow = theater.getSeatsPerRow();

        // Выводим заголовок с номерами мест
        System.out.print("       ");
        for (int seat = 1; seat <= seatsPerRow; seat++) {
            System.out.printf("%-3d ", seat);
        }
        System.out.println();

        // Выводим каждый ряд
        for (int row = 1; row <= rows; row++) {
            System.out.printf("%-5d ", row);

            for (int seat = 1; seat <= seatsPerRow; seat++) {
                Seat currentSeat = new Seat(row, seat);
                SeatStatus status = session.getSeatStatus(currentSeat);

                char symbol = switch (status) {
                    case FREE -> '.';
                    case SOLD -> '█';
                    case RESERVED -> 'R';
                };

                System.out.printf("%-3c ", symbol);
            }

            System.out.println();
        }
    }

    /**
     * Возвращает список мест в указанном ряду и диапазоне
     *
     * @param sessionId ID сеанса
     * @param row номер ряда
     * @param fromSeat начальный номер места
     * @param toSeat конечный номер места
     * @return список мест в указанном диапазоне
     */
    public List<Seat> getSeatsInRange(String sessionId, int row, int fromSeat, int toSeat) {
        MovieSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(SESSION_NOT_FOUND + sessionId);
        }

        Theater theater = session.getTheater();
        if (row < 1 || row > theater.getRows()) {
            throw new IllegalArgumentException("Неверный номер ряда: " + row);
        }

        if (fromSeat < 1 || toSeat > theater.getSeatsPerRow() || fromSeat > toSeat) {
            throw new IllegalArgumentException(
                    String.format("Неверный диапазон мест: %d-%d", fromSeat, toSeat));
        }

        List<Seat> seats = new ArrayList<>();
        for (int seatNum = fromSeat; seatNum <= toSeat; seatNum++) {
            seats.add(new Seat(row, seatNum));
        }

        return seats;
    }

    /**
     * Помечает свободные места в указанном диапазоне как проданные
     * и возвращает список созданных билетов
     *
     * @param sessionId ID сеанса
     * @param row номер ряда
     * @param fromSeat начальный номер места
     * @param toSeat конечный номер места
     * @return список созданных билетов для проданных мест
     */
    public List<Ticket> buyTicketsInRange(String sessionId, int row, int fromSeat, int toSeat) {
        MovieSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(SESSION_NOT_FOUND + sessionId);
        }

        List<Seat> seatsInRange = getSeatsInRange(sessionId, row, fromSeat, toSeat);
        List<Ticket> soldTickets = new ArrayList<>();

        for (Seat seat : seatsInRange) {
            SeatStatus status = session.getSeatStatus(seat);
            if (status == SeatStatus.FREE) {
                // Создаем билет для свободного места
                String ticketId = generateTicketId();
                Ticket ticket = new Ticket(ticketId, session, seat, false);

                // Помечаем место как проданное
                session.setSeatStatus(seat, SeatStatus.SOLD);
                tickets.put(ticketId, ticket);

                // Добавляем билет в результирующий список
                soldTickets.add(ticket);
            }
        }

        return soldTickets;
    }


}
