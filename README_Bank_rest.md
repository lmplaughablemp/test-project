# Система управления банковскими картами



## 1. Запуск через Docker
```bash
docker-compose up --build
2. Ручная установка
bash
# Установите MySQL 8.0
# Создайте базу: CREATE DATABASE bank_cards_db;
mvn clean package
java -jar target/bank-cards.jar
 API Endpoints

 Аутентификация

POST /api/auth/register - Регистрация

POST /api/auth/login - Авторизация

GET /api/auth/profile - Профиль

 Карты
GET /api/cards - Мои карты

POST /api/cards - Создать карту (ADMIN)

GET /api/cards/{id} - Получить карту

POST /api/cards/{id}/block - Заблокировать карту

 Переводы
POST /api/transfers - Перевод между картами

GET /api/transfers - История переводов

 Пользователи
GET /api/users/me - Мой профиль

GET /api/users - Все пользователи (ADMIN)

 Тестирование
bash
Запуск тестов
mvn test

Запуск с отчетом
mvn jacoco:report
 Документация
Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI: http://localhost:8080/v3/api-docs

 Docker
bash
Сборка образа
docker build -t bank-cards-api .

Запуск
docker run -p 8080:8080 bank-cards-api