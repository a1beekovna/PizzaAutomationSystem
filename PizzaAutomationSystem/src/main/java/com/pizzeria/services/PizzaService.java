package com.pizzeria.services;

import com.pizzeria.dao.PizzaDAODB;
import com.pizzeria.models.Pizza;
import java.util.List;
import java.util.Optional;

public class PizzaService {
    private final PizzaDAODB pizzaDAO;
    
    public PizzaService() {
        this.pizzaDAO = new PizzaDAODB();
    }
    
    public void initializeData() {
        // БД сама создает тестовые данные при инициализации
        System.out.println("🔄 Инициализация данных...");
    }
    
    public List<Pizza> getAllPizzas() {
        return pizzaDAO.getAllPizzas();
    }
    
    public List<Pizza> getAvailablePizzas() {
        return pizzaDAO.getAllPizzas().stream()
                .filter(Pizza::isAvailable)
                .toList();
    }
    
    public List<Pizza> getPizzasByCategory(Pizza.Category category) {
        return pizzaDAO.getPizzasByCategory(category);
    }
    
    public Optional<Pizza> getPizzaById(String id) {
        return pizzaDAO.getPizzaById(id);
    }
    
    public boolean addPizza(Pizza pizza) {
        return pizzaDAO.addPizza(pizza);
    }
    
    public boolean updatePizza(Pizza pizza) {
        return pizzaDAO.updatePizza(pizza);
    }
    
    public boolean deletePizza(String id) {
        return pizzaDAO.deletePizza(id);
    }
    
    public List<Pizza> searchPizzas(String query) {
        String lowerQuery = query.toLowerCase();
        return pizzaDAO.getAllPizzas().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerQuery) ||
                            p.getDescription().toLowerCase().contains(lowerQuery))
                .toList();
    }
}
