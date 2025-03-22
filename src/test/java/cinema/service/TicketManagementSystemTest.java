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
}
