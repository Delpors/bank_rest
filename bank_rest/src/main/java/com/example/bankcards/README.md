Bank Cards Service
RESTful сервис для управления банковскими картами пользователей с разделением ролей (ADMIN/USER).

Стек технологий
Java 17

Spring Boot 3.x

Spring Security + JWT

Spring Data JPA

PostgreSQL

Maven

Lombok

Jakarta Validation

Функциональность
Роли пользователей
ADMIN - полное управление картами и пользователями

USER - управление только своими картами

Возможности ADMIN
✅ Создание карт

✅ Блокировка/активация карт

✅ Удаление карт

✅ Просмотр всех карт (с поиском)

✅ Управление пользователями (блокировка/активация/удаление)

✅ Просмотр всех пользователей

Возможности USER
👤 Просмотр своих карт

👤 Блокировка своих карт

👤 Просмотр общего баланса

👤 Переводы между своими картами

API Endpoints
Публичные endpoints
text
POST /api/auth/register - регистрация (первый пользователь получает роль ADMIN)
POST /api/auth/login - вход в систему
Admin endpoints (требуют роль ADMIN)
text
POST   /api/admin/cards           - создание карты
PUT    /api/admin/cards/{id}/block  - блокировка карты
PUT    /api/admin/cards/{id}/activate - активация карты
DELETE /api/admin/cards/{id}      - удаление карты
GET    /api/admin/cards/all        - все карты (с поиском)

GET    /api/admin/users            - все пользователи
POST   /api/admin/users            - создание пользователя
PUT    /api/admin/users/{id}/block - блокировка пользователя
PUT    /api/admin/users/{id}/activate - активация пользователя
DELETE /api/admin/users/{id}       - удаление пользователя
User endpoints (требуют роль USER)
text
GET    /api/user/cards          - мои карты
POST   /api/user/cards/{id}/block - заблокировать мою карту
GET    /api/user/cards/balance  - общий баланс
POST   /api/user/cards/transfer - перевод между картами
Инструкция по запуску
Предварительные требования
JDK 17 или выше

PostgreSQL (локально или Docker)

Maven

IntelliJ IDEA (рекомендуется)

1. Клонирование репозитория
   bash
   git clone <https://github.com/Delpors/bank_rest.git>
   cd bank-cards-service
2. Настройка базы данных
   Вариант А: Через Docker (проще)
   bash
# Запуск PostgreSQL в Docker
docker run --name bank-cards-db \
-e POSTGRES_DB=bank_cards \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=root \
-p 5432:5432 \
-d postgres:15

Создайте базу данных:

sql
CREATE DATABASE bank_cards;
3. Настройка приложения
   В файле src/main/resources/application.properties

properties
# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/bank_cards
spring.datasource.username=postgres
spring.datasource.password=root

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
server.port=8080

4. Сборка и запуск
   bash
# Очистка и сборка проекта
mvn clean install

# Запуск приложения
mvn spring-boot:run
Приложение запустится на http://localhost:8080

5. Проверка работоспособности
   Регистрация первого пользователя (станет ADMIN)
   bash
   curl -X POST http://localhost:8080/api/auth/register \
   -H "Content-Type: application/json" \
   -d '{
   "userName": "admin",
   "password": "admin123",
   "fullName": "Admin User",
   "email": "admin@example.com"
   }'
   Логин
   bash
   curl -X POST http://localhost:8080/api/auth/login \
   -H "Content-Type: application/json" \
   -d '{
   "userName": "admin",
   "password": "admin123"
   }'
   Сохраните полученный JWT токен для дальнейших запросов.

Примеры запросов
Создание карты (ADMIN)
bash
curl -X POST http://localhost:8080/api/admin/cards \
-H "Content-Type: application/json" \
-H "Authorization: Bearer <ваш-токен>" \
-d '{
"cardNumber": "1234567890823471",
"cardHolderName": "admin",
"expireDate": "2027-12-31",
"balance": 4000,
"userId": 1,
"status": "ACTIVE"
}'
Получение всех карт (ADMIN)
bash
curl -X GET "http://localhost:8080/api/admin/cards/all?page=0&size=10" \
-H "Authorization: Bearer <ваш-токен>"
Перевод между картами (USER)
bash
curl -X POST http://localhost:8080/api/user/cards/transfer \
-H "Content-Type: application/json" \
-H "Authorization: Bearer <ваш-токен>" \
-d '{
"fromCardId": 1,
"toCardId": 2,
"amount": 100.00
}'
Структура проекта
text
src/main/java/com/example/bankcards/
├── config/           # Конфигурации Spring Security
├── controller/       # REST контроллеры
├── dto/              # Data Transfer Objects
├── entity/           # JPA сущности
├── exception/        # Обработка ошибок
├── repository/       # JPA репозитории
├── security/         # JWT и security компоненты
└── service/          # Бизнес-логика
