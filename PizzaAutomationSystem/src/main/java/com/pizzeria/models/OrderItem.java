package com.pizzeria.models;

public class OrderItem {
    private Pizza pizza;
    private int quantity;
    private String specialInstructions;

    public OrderItem(Pizza pizza, int quantity) {
        this.pizza = pizza;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return pizza.getPrice() * quantity;
    }

    public int getPreparationTime() {
        return pizza.getPreparationTime();
    }

    public Pizza getPizza() { return pizza; }
    public int getQuantity() { return quantity; }
    public String getSpecialInstructions() { return specialInstructions; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getFormattedTotal() {
        return String.format("%.2f₸", getTotalPrice());
    }
}
