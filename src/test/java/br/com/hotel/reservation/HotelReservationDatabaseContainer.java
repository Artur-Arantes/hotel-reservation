package br.com.hotel.reservation;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
public class HotelReservationDatabaseContainer extends MySQLContainer<HotelReservationDatabaseContainer> {

    private static final String MYSQL_VERSION = "mysql:8.0";
    private static final String DB_NAME = "hotel_reservation";
    private static HotelReservationDatabaseContainer container;

    private HotelReservationDatabaseContainer() {
        super(MYSQL_VERSION);
        withDatabaseName(DB_NAME);
        withUsername(DB_NAME);
        withPassword(DB_NAME);
        withReuse(false);
    }

    public static HotelReservationDatabaseContainer getInstance() {
        if (container == null) {
            container = new HotelReservationDatabaseContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USER", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
        System.setProperty("SECRET_KEY", "test-secret-key-hotel-reservation");
    }

    @Override
    public void stop() {
    }
}
