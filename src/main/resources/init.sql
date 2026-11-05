-- Схема БД піцерії (ЛР №10).
-- Скрипт ідемпотентний: його можна запускати при кожному старті застосунку.

CREATE TABLE IF NOT EXISTS pizzas (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120)    NOT NULL,
    price       NUMERIC(10, 2)  NOT NULL,
    diameter_cm INT             NOT NULL,
    CONSTRAINT pk_pizzas PRIMARY KEY (id),
    CONSTRAINT uq_pizzas_name UNIQUE (name),
    CONSTRAINT ck_pizzas_price CHECK (price > 0),
    CONSTRAINT ck_pizzas_diameter CHECK (diameter_cm > 0)
);

-- Ідентифікатори, призначені Hibernate (GenerationType.IDENTITY), починаються з 1.
-- Стартові піци мають ids 101+, щоб не зіткнутися із генерованими id при додаванні нових записів.
-- Стартові дані меню. MERGE ... KEY(id) не створює дублікатів при повторному запуску.
MERGE INTO pizzas (id, name, price, diameter_cm) KEY (id) VALUES (101, 'Маргарита', 140.00, 32);
MERGE INTO pizzas (id, name, price, diameter_cm) KEY (id) VALUES (102, 'Пепероні', 180.00, 34);
MERGE INTO pizzas (id, name, price, diameter_cm) KEY (id) VALUES (103, 'Чотири сири', 210.00, 30);

-- orders.name — назва піци, яку замовили. Це природний зовнішній ключ
-- на pizzas.name: одна піца з меню може зустрічатися в багатьох замовленнях.
CREATE TABLE IF NOT EXISTS orders (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120)    NOT NULL,
    quantity    INT             NOT NULL,
    extra_sauce BOOLEAN         NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT ck_orders_quantity CHECK (quantity > 0),
    CONSTRAINT fk_orders_pizza FOREIGN KEY (name)
        REFERENCES pizzas (name)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Індекс прискорює ORDER BY name, quantity та перевірку зовнішнього ключа.
CREATE INDEX IF NOT EXISTS idx_orders_name_quantity ON orders (name, quantity);
