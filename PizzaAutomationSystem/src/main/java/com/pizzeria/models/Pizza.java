package com.pizzeria.models;

import java.util.List;

public class Pizza {
    private String id;
    private String name;
    private String description;
    private List<String> ingredients;
    private PizzaSize size;
    private double price;
    private int preparationTime;
    private Category category;
    private boolean isAvailable;

    public enum PizzaSize {
        SMALL(25), MEDIUM(30), LARGE(35), XXL(40);

        private final int diameter;

        PizzaSize(int diameter) {
            this.diameter = diameter;
        }

        public int getDiameter() {
            return diameter;
        }
    }

    public enum Category {
        CLASSIC("Классические"),
        SPECIAL("Специальные"),
        VEGETARIAN("Вегетарианские"),
        SPICY("Острые"),
        PREMIUM("Премиум");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Pizza(String id, String name, String description, List<String> ingredients,
                 PizzaSize size, double price, int preparationTime,
                 Category category, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.size = size;
        this.price = price;
        this.preparationTime = preparationTime;
        this.category = category;
        this.isAvailable = isAvailable;
    }

    // Упрощенный конструктор
    public Pizza(String id, String name, List<String> ingredients, PizzaSize size,
                 double price, Category category) {
        this(id, name, "Вкусная пицца " + name, ingredients, size, price,
                20, category, true);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getIngredients() { return ingredients; }
    public PizzaSize getSize() { return size; }
    public double getPrice() { return price; }
    public int getPreparationTime() { return preparationTime; }
    public Category getCategory() { return category; }
    public boolean isAvailable() { return isAvailable; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setSize(PizzaSize size) { this.size = size; }
    public void setPrice(double price) { this.price = price; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }
    public void setCategory(Category category) { this.category = category; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getFormattedPrice() {
        return String.format("%.2f₸", price);
    }

    public String getIngredientsString() {
        return String.join(", ", ingredients);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", name, size, getFormattedPrice());
    }
}
