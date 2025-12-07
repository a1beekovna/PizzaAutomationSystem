package com.pizzeria.models;

import java.time.LocalDate;
import java.util.UUID;

public class Customer {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private LocalDate registrationDate;
    private int loyaltyPoints;
    private int totalOrders;

    public Customer(String id, String name, String phone, String email, String address,
                    LocalDate registrationDate, int loyaltyPoints, int totalOrders) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.registrationDate = registrationDate;
        this.loyaltyPoints = loyaltyPoints;
        this.totalOrders = totalOrders;
    }

    public Customer(String name, String phone, String email, String address) {
        this(UUID.randomUUID().toString().substring(0, 6),
                name, phone, email, address, LocalDate.now(), 0, 0);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public int getTotalOrders() { return totalOrders; }

    public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }
    public void useLoyaltyPoints(int points) {
        if (points <= loyaltyPoints) {
            loyaltyPoints -= points;
        }
    }
    public void incrementOrders() { this.totalOrders++; }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, phone);
    }
}
