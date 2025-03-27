package org.example;

import java.util.Date;

public class SportsCar extends Vehicle {
    @Override
    public boolean rent(Date startDate, Date endDate) {
        // Sports cars cannot be rented according to business rules
        return false;
    }
}
