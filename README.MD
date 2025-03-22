# 🎬 Cinema Ticket Booking System

## 📌 Описание
**Cinema Ticket Booking System** — это система управления бронированием билетов в кинотеатр. Позволяет пользователям просматривать доступные сеансы, бронировать билеты, подтверждать брони, а также отменять билеты.

## 🚀 Функционал
- Управление киносеансами
- Просмотр доступных мест
- Бронирование и покупка билетов
- Подтверждение брони
- Отмена билетов и возврат мест в продажу
- Покупка билетов в указанном диапазоне
- Вывод на консоль **схему мест в зале** 
- Логирование событий

## 🛠️ Стек технологий
- **Язык**: Java 21
- **Фреймворк**: Spring Boot (планируется)
- **Логирование**: SLF4J
- **Сборка и зависимости**: Maven
- **База данных**: PostgreSQL (планируется)
- **Тестирование**: JUnit 5, Testcontainers (в будущем)

## 📂 Структура проекта
```
📦 cinema-ticket-system
┣ 📂 doc                     # UML-диаграммы классов
┣ 📂 src
┃ ┣ 📂 main
┃ ┃ ┣ 📂 java
┃ ┃ ┃ ┣ 📂 cinema
┃ ┃ ┃ ┃ ┣ 📜 CinemaSystem.java        # Основной класс системы
┃ ┃ ┃ ┃ ┣ 📂 model
┃ ┃ ┃ ┃ ┃ ┣ 📜 MovieSession.java      # Модель киносеанса
┃ ┃ ┃ ┃ ┃ ┣ 📜 Seat.java              # Модель места
┃ ┃ ┃ ┃ ┃ ┣ 📜 SeatStatus.java        # Перечисление статусов мест
┃ ┃ ┃ ┃ ┃ ┣ 📜 Theater.java           # Класс кинозала
┃ ┃ ┃ ┃ ┃ ┣ 📜 Ticket.java            # Класс билета
┃ ┃ ┃ ┃ ┣ 📂 service
┃ ┃ ┃ ┃ ┃ ┣ 📜 TicketManagementSystem.java  # Управление билетами
┃ ┣ 📂 test
┃ ┃ ┣ 📜 TicketManagementSystemTest.java    # Тесты
┣ 📜 pom.xml          # Конфигурация Maven
┣ 📜 README.md        # Описание проекта
```

## 📦 Установка и запуск
### 1️⃣ Клонирование репозитория
```bash
git clone https://github.com/your-repo/cinema-ticket-system.git
cd cinema-ticket-system
```

### 2️⃣ Сборка и запуск
```bash
mvn clean package
java -jar target/cinema-ticket-system-1.0-SNAPSHOT.jar
```

### 3️⃣ Запуск тестов
```bash
mvn test
```

## 🔥 Примеры использования
### ➤ Создание киносеанса
```java
Theater theater = new Theater("IMAX", 10, 20);
MovieSession session = ticketSystem.createMovieSession("Inception", theater, LocalDateTime.now().plusDays(1));
```

### ➤ Покупка билета

```java
Ticket soldTicket = ticketSystem.buyTicket(session.getSessionId(), 5, 10);
```

### ➤ Бронирование и подтверждение билета
```java
Ticket reserved = ticketSystem.reserveTicket(session.getSessionId(), 3, 7);
Ticket confirmed = ticketSystem.confirmReservation(reserved.getTicketId());
```

## 📌 Планы по развитию
- [ ] Добавить Spring Boot для API
- [ ] Подключить базу данных PostgreSQL
- [ ] Интегрировать Kafka и MongoDB для статистики
- [ ] Создать UI для пользователей

## 📜 Лицензия
Этот проект распространяется под лицензией MIT.

---
✨ Разработано с ❤️ для эффективного управления киносеансами!
```

Этот README структурирован, понятен и содержит всю необходимую информацию для новых разработчиков и пользователей проекта. 🚀