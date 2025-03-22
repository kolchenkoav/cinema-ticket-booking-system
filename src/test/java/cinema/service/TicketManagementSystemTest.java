package cinema.service;

import cinema.model.MovieSession;
import cinema.model.Seat;
import cinema.model.Theater;
import cinema.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketManagementSystemTest {

    private TicketManagementSystem ticketSystem;
    private MovieSession session;
    private Theater theater;

    @BeforeEach
    @DisplayName("Установка системы управления билетами и создание сеанса кино")
    void setUp() {
        ticketSystem = new TicketManagementSystem();
        theater = new Theater("Test Theater", 5, 5);
        session = ticketSystem.createMovieSession(
                "Test Movie",
                theater,
                LocalDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Создание сеанса кино")
    void testCreateMovieSession() {
        assertNotNull(session);
        assertEquals("Test Movie", session.getMovieTitle());
        assertEquals(theater, session.getTheater());
        assertEquals(25, session.getAvailableSeats().size()); // 5 rows * 5 seats
    }

    @Test
    @DisplayName("Покупка билета на сеанс кино")
    void testBuyTicketSuccess() {
        Ticket ticket = ticketSystem.buyTicket(session.getSessionId(), 1, 1);

        assertNotNull(ticket);
        assertTrue(ticket.isActive());
        assertFalse(ticket.isReservation());
        assertEquals("Test Movie", ticket.getSession().getMovieTitle());
        assertEquals(new Seat(1, 1), ticket.getSeat());
        assertEquals(24, ticketSystem.getAvailableSeats(session.getSessionId()).size());
    }

    @Test
    @DisplayName("Покупка билета на уже проданное место")
    void testBuyTicketAlreadySold() {
        String sessionId = session.getSessionId();
        ticketSystem.buyTicket(sessionId, 1, 1);
        assertThrows(IllegalStateException.class, () ->
                        ticketSystem.buyTicket(sessionId, 1, 1),
                "Место уже продано"
        );
    }

    @Test
    @DisplayName("Покупка билета на неверное место")
    void testBuyTicketInvalidSeat() {
        String sessionId = session.getSessionId();
        assertThrows(IllegalArgumentException.class, () ->
                        ticketSystem.buyTicket(sessionId, 10, 10),
                "Неверное место: Ряд 10, Место 10"
        );
    }


    @Test
    @DisplayName("Покупка билета на сеанс кино с неверным идентификатором сеанса")
    void testReserveTicketSuccess() {
        Ticket ticket = ticketSystem.reserveTicket(session.getSessionId(), 1, 1);

        assertNotNull(ticket);
        assertTrue(ticket.isActive());
        assertTrue(ticket.isReservation());
        assertEquals(new Seat(1, 1), ticket.getSeat());
        assertEquals(24, ticketSystem.getAvailableSeats(session.getSessionId()).size());
    }

    @Test
    @DisplayName("Покупка билета на уже забронированное место")
    void testConfirmReservation() {
        Ticket reserved = ticketSystem.reserveTicket(session.getSessionId(), 1, 1);
        Ticket confirmed = ticketSystem.confirmReservation(reserved.getTicketId());

        assertNotNull(confirmed);
        assertTrue(confirmed.isActive());
        assertFalse(confirmed.isReservation());
        assertFalse(reserved.isActive()); // Original reservation should be inactive
        assertEquals(reserved.getSeat(), confirmed.getSeat());
    }

    @Test
    @DisplayName("Покупка билета на сеанс кино с неверным идентификатором сеанса")
    void testCancelTicket() {
        Ticket ticket = ticketSystem.buyTicket(session.getSessionId(), 1, 1);
        int initialAvailable = ticketSystem.getAvailableSeats(session.getSessionId()).size();

        ticketSystem.cancelTicket(ticket.getTicketId());

        assertFalse(ticket.isActive());
        assertEquals(initialAvailable + 1,
                ticketSystem.getAvailableSeats(session.getSessionId()).size());
    }

    @Test
    @DisplayName("Покупка билета на сеанс кино с неверным идентификатором билета")
    void testGetAvailableSeats() {
        List<Seat> initialSeats = ticketSystem.getAvailableSeats(session.getSessionId());
        assertEquals(25, initialSeats.size());

        ticketSystem.buyTicket(session.getSessionId(), 1, 1);
        ticketSystem.reserveTicket(session.getSessionId(), 1, 2);

        List<Seat> availableSeats = ticketSystem.getAvailableSeats(session.getSessionId());
        assertEquals(23, availableSeats.size());
        assertFalse(availableSeats.contains(new Seat(1, 1)));
        assertFalse(availableSeats.contains(new Seat(1, 2)));
    }

    @Test
    @DisplayName("Получение списка всех активных билетов")
    void testGetAllActiveTickets() {
        Ticket ticket1 = ticketSystem.buyTicket(session.getSessionId(), 1, 1);
        Ticket ticket2 = ticketSystem.reserveTicket(session.getSessionId(), 1, 2);

        List<Ticket> activeTickets = ticketSystem.getAllActiveTickets();
        assertEquals(2, activeTickets.size());
        assertTrue(activeTickets.contains(ticket1));
        assertTrue(activeTickets.contains(ticket2));

        ticketSystem.cancelTicket(ticket1.getTicketId());
        activeTickets = ticketSystem.getAllActiveTickets();
        assertEquals(1, activeTickets.size());
        assertFalse(activeTickets.contains(ticket1));
    }

    @Test
    @DisplayName("Получение списка всех активных билетов на неверный идентификатор сеанса")
    void testInvalidSessionId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ticketSystem.buyTicket("INVALID_ID", 1, 1)
        );
        assertEquals("Сеанс не найден: INVALID_ID", exception.getMessage());
    }

    @Test
    @DisplayName("Получение списка мест в диапазоне")
    void testGetSeatsInRange() {
        String sessionId = session.getSessionId();
        List<Seat> seats = ticketSystem.getSeatsInRange(sessionId, 2, 3, 5);

        assertEquals(3, seats.size(), "Должно быть получено 3 места");
        assertTrue(seats.contains(new Seat(2, 3)), "Должно содержать место (2,3)");
        assertTrue(seats.contains(new Seat(2, 4)), "Должно содержать место (2,4)");
        assertTrue(seats.contains(new Seat(2, 5)), "Должно содержать место (2,5)");

        // Проверяем, что места из другого ряда или вне диапазона не включены
        assertFalse(seats.contains(new Seat(1, 3)), "Не должно содержать место из другого ряда");
        assertFalse(seats.contains(new Seat(2, 2)), "Не должно содержать место вне диапазона");
        assertFalse(seats.contains(new Seat(2, 6)), "Не должно содержать место вне диапазона");
    }

    @Test
    @DisplayName("Получение списка мест с неверным диапазоном")
    void testGetSeatsInRangeInvalidRange() {
        String sessionId = session.getSessionId();

        // Проверка на неверный ряд
        assertThrows(IllegalArgumentException.class, () ->
                        ticketSystem.getSeatsInRange(sessionId, 10, 1, 3),
                "Должно бросить исключение при неверном номере ряда");

        // Проверка на неверный диапазон мест (начало больше конца)
        assertThrows(IllegalArgumentException.class, () ->
                        ticketSystem.getSeatsInRange(sessionId, 1, 5, 3),
                "Должно бросить исключение при начале диапазона большем чем конец");

        // Проверка на неверный номер места
        assertThrows(IllegalArgumentException.class, () ->
                        ticketSystem.getSeatsInRange(sessionId, 1, 0, 3),
                "Должно бросить исключение при неверном начальном номере места");

        assertThrows(IllegalArgumentException.class, () ->
                        ticketSystem.getSeatsInRange(sessionId, 1, 1, 10),
                "Должно бросить исключение при неверном конечном номере места");
    }

    @Test
    @DisplayName("Успешная покупка билетов в диапазоне")
    void testBuyTicketsInRange() {
        String sessionId = session.getSessionId();

        // Проверяем изначальное количество свободных мест
        int initialAvailableSeats = ticketSystem.getAvailableSeats(sessionId).size();

        // Покупаем билеты в диапазоне
        List<Ticket> soldTickets = ticketSystem.buyTicketsInRange(sessionId, 3, 2, 4);

        // Проверяем, что возвращены правильные билеты
        assertEquals(3, soldTickets.size(), "Должно быть продано 3 билета");

        // Проверяем, что все билеты действительны и не являются бронированием
        for (Ticket ticket : soldTickets) {
            assertTrue(ticket.isActive(), "Билет должен быть активным");
            assertFalse(ticket.isReservation(), "Билет не должен быть бронированием");
            assertEquals(3, ticket.getSeat().getRow(), "Билет должен быть для 3 ряда");
            int seatNumber = ticket.getSeat().getNumber();
            assertTrue(seatNumber >= 2 && seatNumber <= 4,
                    "Номер места должен быть в диапазоне от 2 до 4");
        }

        // Проверяем, что количество свободных мест уменьшилось
        int currentAvailableSeats = ticketSystem.getAvailableSeats(sessionId).size();
        assertEquals(initialAvailableSeats - 3, currentAvailableSeats,
                "Количество свободных мест должно уменьшиться на 3");

        // Проверяем статус мест
        assertFalse(ticketSystem.getAvailableSeats(sessionId).contains(new Seat(3, 2)),
                "Место (3,2) не должно быть свободным");
        assertFalse(ticketSystem.getAvailableSeats(sessionId).contains(new Seat(3, 3)),
                "Место (3,3) не должно быть свободным");
        assertFalse(ticketSystem.getAvailableSeats(sessionId).contains(new Seat(3, 4)),
                "Место (3,4) не должно быть свободным");
    }

    @Test
    @DisplayName("Покупка билетов, когда некоторые места уже заняты")
    void testBuyTicketsInRangeWithSomeOccupiedSeats() {
        String sessionId = session.getSessionId();

        // Сначала покупаем один билет из диапазона
        ticketSystem.buyTicket(sessionId, 4, 3);

        // Затем пытаемся купить билеты в диапазоне, включающем это место
        List<Ticket> soldTickets = ticketSystem.buyTicketsInRange(sessionId, 4, 2, 4);

        // Проверяем, что возвращены только билеты для свободных мест
        assertEquals(2, soldTickets.size(), "Должно быть продано только 2 билета");

        // Проверяем, что купленные билеты именно для свободных мест
        boolean hasTicketForSeat2 = soldTickets.stream()
                .anyMatch(t -> t.getSeat().equals(new Seat(4, 2)));
        boolean hasTicketForSeat4 = soldTickets.stream()
                .anyMatch(t -> t.getSeat().equals(new Seat(4, 4)));

        assertTrue(hasTicketForSeat2, "Должен быть билет для места (4,2)");
        assertTrue(hasTicketForSeat4, "Должен быть билет для места (4,4)");

        // Проверяем, что все места в диапазоне теперь проданы
        assertFalse(ticketSystem.getAvailableSeats(sessionId).contains(new Seat(4, 2)));
        assertFalse(ticketSystem.getAvailableSeats(sessionId).contains(new Seat(4, 3)));
        assertFalse(ticketSystem.getAvailableSeats(sessionId).contains(new Seat(4, 4)));
    }

    @Test
    @DisplayName("Попытка купить билеты для несуществующего сеанса")
    void testBuyTicketsInRangeInvalidSession() {
        assertThrows(IllegalArgumentException.class, () ->
                        ticketSystem.buyTicketsInRange("INVALID_ID", 1, 1, 3),
                "Должно бросить исключение при неверном ID сеанса");
    }
}
