CREATE TABLE hub_operating_hours (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    opening_time TIME(0) NOT NULL,
    closing_time TIME(0) NOT NULL,
    CONSTRAINT ck_hub_operating_hours_interval CHECK (closing_time > opening_time),
    CONSTRAINT ck_hub_operating_hours_minutes CHECK (
        EXTRACT(SECOND FROM opening_time) = 0 AND EXTRACT(SECOND FROM closing_time) = 0
        AND closing_time < TIME '24:00'
    )
);

CREATE TABLE hub_operating_days (
    hub_id INTEGER NOT NULL REFERENCES hub_operating_hours(id),
    day_of_week VARCHAR(9) NOT NULL,
    PRIMARY KEY (hub_id, day_of_week),
    CONSTRAINT ck_hub_operating_days CHECK (
        day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')
    )
);

INSERT INTO hub_operating_hours (id, opening_time, closing_time) VALUES (1, '08:00', '18:00');
INSERT INTO hub_operating_days (hub_id, day_of_week)
VALUES (1, 'MONDAY'), (1, 'TUESDAY'), (1, 'WEDNESDAY'), (1, 'THURSDAY'), (1, 'FRIDAY');
