package cinema;

import cinema.model.MovieSession;
import cinema.model.Theater;
import cinema.model.Ticket;
import cinema.service.TicketManagementSystem;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class CinemaSystem {
    private final TicketManagementSystem ticketSystem;

    public CinemaSystem() {
        this.ticketSystem = new TicketManagementSystem();
    }

    public TicketManagementSystem getTicketSystem() {
        return ticketSystem;
    }

    // Демонстрация работы системы
    public static void main(String[] args) {
        // Создаем систему кинотеатра
        CinemaSystem cinema = new CinemaSystem();
        TicketManagementSystem ticketSystem = cinema.getTicketSystem();

        // Создаем кинозал
        Theater theater = new Theater("Большой зал", 10, 15);

        // Создаем сеанс
        MovieSession session = ticketSystem.createMovieSession(
                "Мстители: Финал",
                theater,
                LocalDateTime.of(2023, 5, 15, 18, 30)
        );

        try {
            // Получаем информацию о сеансе
            log.info("\nСеанс: {}", session.getMovieTitle());
            log.info("Доступно свободных мест: {}\n",
                    ticketSystem.getAvailableSeats(session.getSessionId()).size());

            // Покупаем билет
            Ticket soldTicket = ticketSystem.buyTicket(session.getSessionId(), 5, 10);
            log.info("Куплен билет: {} \n", soldTicket);

            // Бронируем билет
            Ticket reservedTicket = ticketSystem.reserveTicket(session.getSessionId(), 5, 11);
            log.info("\nЗабронирован билет: {} \n", reservedTicket);

//            // Подтверждаем бронь
//            Ticket confirmedTicket = ticketSystem.confirmReservation(reservedTicket.getTicketId());
//            log.info("\nПодтвержден билет из брони: {} \n", confirmedTicket);

            // Отменяем купленный билет (возврат)
            ticketSystem.cancelTicket(soldTicket.getTicketId());
            log.info("\nОтменен билет с id: {} \n", soldTicket.getTicketId());

//            // Покупаем билет
//            soldTicket = ticketSystem.buyTicket(session.getSessionId(), 5, 12);
//            log.info("Куплен билет: {} \n", soldTicket);

            // Бронируем билеты
            ticketSystem.reserveTicket(session.getSessionId(), 1, 1);
            ticketSystem.reserveTicket(session.getSessionId(), 5, 7);
            ticketSystem.reserveTicket(session.getSessionId(), 5, 10);
            ticketSystem.buyTicket(session.getSessionId(), 5, 8);
            ticketSystem.buyTicket(session.getSessionId(), 5, 9);

            List<Ticket> tickets = ticketSystem.buyTicketsInRange(session.getSessionId(), 5, 1, 15);
            tickets.forEach(x -> log.info(String.valueOf(x.getTicketId())));
            tickets = ticketSystem.buyTicketsInRange(session.getSessionId(), 7, 2, 14);
            tickets.forEach(x -> log.info(String.valueOf(x.getTicketId())));
            tickets = ticketSystem.buyTicketsInRange(session.getSessionId(), 2, 6, 10);
            tickets.forEach(x -> log.info(String.valueOf(x.getTicketId())));

            // Проверяем свободные места
            log.info("\nДоступно свободных мест: {} \n",
                    ticketSystem.getAvailableSeats(session.getSessionId()).size());

            // Получаем информацию о купленных билетах
            log.info("\nКупленные билеты:");
            ticketSystem.getAllActiveTickets().forEach(x -> log.info(String.valueOf(x.getTicketId())));

            ticketSystem.printSeatingChart(session.getSessionId());

        } catch (Exception e) {
            log.error("Ошибка: {} ", e.getMessage());
        }
    }
}
