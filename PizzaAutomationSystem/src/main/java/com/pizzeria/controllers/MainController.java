package com.pizzeria.controllers;

import com.pizzeria.models.*;
import com.pizzeria.models.Order.OrderStatus;
import com.pizzeria.models.Order.DeliveryType;
import com.pizzeria.models.Payment.PaymentMethod;
import com.pizzeria.services.PizzaService;
import com.pizzeria.services.OrderService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MainController {

    // ==================== MENU TAB ====================
    @FXML private TabPane mainTabPane;
    @FXML private ListView<Pizza> menuListView;
    @FXML private TextArea pizzaDetailsArea;
    @FXML private ComboBox<Pizza.Category> categoryFilter;

    // ==================== NEW ORDER TAB ====================
    @FXML private TextField searchPizzaField;
    @FXML private ComboBox<Pizza.Category> orderCategoryFilter;
    @FXML private ListView<Pizza> availablePizzasListView;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addToCartBtn;
    @FXML private ListView<OrderItem> cartListView;
    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerEmailField;
    @FXML private RadioButton pickupRadio;
    @FXML private RadioButton deliveryRadio;
    @FXML private TextField deliveryAddressField;
    @FXML private ComboBox<PaymentMethod> paymentMethodCombo;
    @FXML private TextArea orderNotesArea;
    @FXML private Label totalLabel;
    @FXML private Button placeOrderBtn;

    // ==================== ORDERS TAB ====================
    @FXML private ComboBox<String> orderStatusFilter;
    @FXML private Label ordersCountLabel;
    @FXML private TableView<Order> ordersTableView;
    @FXML private TableColumn<Order, String> orderIdColumn;
    @FXML private TableColumn<Order, String> orderTimeColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, String> orderTotalColumn;
    @FXML private TableColumn<Order, String> orderTypeColumn;
    @FXML private TextArea orderDetailsArea;

    // ==================== STATISTICS TAB ====================
    @FXML private Label totalOrdersLabel;
    @FXML private Label todayOrdersLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label todayRevenueLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label confirmedCountLabel;
    @FXML private Label preparingCountLabel;
    @FXML private Label bakingCountLabel;
    @FXML private Label readyCountLabel;
    @FXML private Label deliveringCountLabel;
    @FXML private Label completedCountLabel;
    @FXML private Label cancelledCountLabel;
    @FXML private ListView<String> popularPizzasListView;

    // ==================== SETTINGS TAB ====================
    @FXML private ListView<Pizza> settingsPizzaListView;
    @FXML private Label pizzaFormTitle;
    @FXML private TextField pizzaNameField;
    @FXML private TextArea pizzaDescField;
    @FXML private TextArea pizzaIngredientsField;
    @FXML private ComboBox<Pizza.PizzaSize> pizzaSizeCombo;
    @FXML private TextField pizzaPriceField;
    @FXML private TextField pizzaTimeField;
    @FXML private ComboBox<Pizza.Category> pizzaCategoryCombo;
    @FXML private CheckBox pizzaAvailableCheck;
    @FXML private Button savePizzaBtn;
    @FXML private CheckBox darkModeCheck;
    @FXML private CheckBox soundsCheck;
    @FXML private ComboBox<String> languageCombo;

    // ==================== SERVICES & DATA ====================
    private final PizzaService pizzaService;
    private final OrderService orderService;
    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();
    private ToggleGroup deliveryToggleGroup;
    private Pizza editingPizza = null;

    public MainController() {
        this.pizzaService = new PizzaService();
        this.orderService = new OrderService(pizzaService);
    }

    @FXML
    public void initialize() {
        try {
            initializeData();
            setupMenuTab();
            setupNewOrderTab();
            setupOrdersTab();
            setupStatisticsTab();
            setupSettingsTab();
            System.out.println("All tabs initialized");
        } catch (Exception e) {
            showError("Initialization error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeData() throws IOException {
        pizzaService.initializeData();
        System.out.println("Pizza data initialized");
    }

    // ==================== MENU TAB METHODS ====================

    private void setupMenuTab() throws IOException {
        categoryFilter.setItems(FXCollections.observableArrayList(Pizza.Category.values()));
        categoryFilter.getSelectionModel().selectFirst();
        loadPizzas();

        categoryFilter.setOnAction(event -> {
            try { loadPizzas(); } catch (IOException e) { showError("Error", e.getMessage()); }
        });

        menuListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> showPizzaDetails(newVal));
    }

    private void loadPizzas() throws IOException {
        Pizza.Category selectedCategory = categoryFilter.getValue();
        var pizzas = (selectedCategory == null)
                ? pizzaService.getAllPizzas()
                : pizzaService.getPizzasByCategory(selectedCategory);
        menuListView.setItems(FXCollections.observableArrayList(pizzas));
    }

    private void showPizzaDetails(Pizza pizza) {
        if (pizza == null) { pizzaDetailsArea.clear(); return; }

        StringBuilder details = new StringBuilder();
        details.append("Pizza: ").append(pizza.getName()).append("\n\n")
                .append("Description: ").append(pizza.getDescription()).append("\n\n")
                .append("Price: ").append(pizza.getFormattedPrice()).append("\n\n");

        if (pizza.getSize() != null) {
            details.append("Size: ").append(pizza.getSize())
                    .append(" (").append(pizza.getSize().getDiameter()).append(" cm)\n\n");
        }

        details.append("Prep time: ").append(pizza.getPreparationTime()).append(" min\n\n")
                .append("Category: ").append(pizza.getCategory().getDisplayName()).append("\n\n")
                .append("Ingredients:\n");

        for (String ing : pizza.getIngredients()) {
            details.append("  - ").append(ing).append("\n");
        }

        details.append("\nStatus: ")
                .append(pizza.isAvailable() ? "Available" : "Not available");

        pizzaDetailsArea.setText(details.toString());
    }

    // ==================== NEW ORDER TAB METHODS ====================

    private void setupNewOrderTab() {
        orderCategoryFilter.setItems(FXCollections.observableArrayList(Pizza.Category.values()));
        orderCategoryFilter.setPromptText("Все категории");
        orderCategoryFilter.setOnAction(e -> filterAvailablePizzas());

        searchPizzaField.textProperty().addListener((obs, oldVal, newVal) -> filterAvailablePizzas());

        availablePizzasListView.setItems(FXCollections.observableArrayList(pizzaService.getAvailablePizzas()));

        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        cartListView.setItems(cartItems);
        cartListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrderItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s x%d = %s",
                            item.getPizza().getName(),
                            item.getQuantity(),
                            item.getFormattedTotal()));
                }
            }
        });

        deliveryToggleGroup = new ToggleGroup();
        pickupRadio.setToggleGroup(deliveryToggleGroup);
        deliveryRadio.setToggleGroup(deliveryToggleGroup);
        pickupRadio.setSelected(true);

        deliveryToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            deliveryAddressField.setDisable(pickupRadio.isSelected());
        });

        paymentMethodCombo.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodCombo.getSelectionModel().selectFirst();

        updateCartTotal();
    }

    private void filterAvailablePizzas() {
        String query = searchPizzaField.getText().toLowerCase();
        Pizza.Category category = orderCategoryFilter.getValue();

        List<Pizza> filtered = pizzaService.getAvailablePizzas().stream()
                .filter(p -> (query.isEmpty() || p.getName().toLowerCase().contains(query) ||
                        p.getDescription().toLowerCase().contains(query)))
                .filter(p -> (category == null || p.getCategory() == category))
                .toList();

        availablePizzasListView.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleAddToCart() {
        Pizza selected = availablePizzasListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select pizza", "Please select a pizza from the list");
            return;
        }

        int quantity = quantitySpinner.getValue();

        Optional<OrderItem> existing = cartItems.stream()
                .filter(item -> item.getPizza().getId().equals(selected.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
            cartListView.refresh();
        } else {
            cartItems.add(new OrderItem(selected, quantity));
        }

        updateCartTotal();
        showInfo("Added", selected.getName() + " x" + quantity + " added to cart");
    }

    @FXML
    private void handleRemoveFromCart() {
        OrderItem selected = cartListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            updateCartTotal();
        }
    }

    @FXML
    private void handleClearCart() {
        cartItems.clear();
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        totalLabel.setText(String.format("Total: %.2f T", total));
    }

    @FXML
    private void handlePlaceOrder() {
        if (cartItems.isEmpty()) {
            showWarning("Cart empty", "Add pizzas to cart to place an order");
            return;
        }

        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            showWarning("Fill in details", "Enter customer name and phone");
            return;
        }

        if (deliveryRadio.isSelected() && deliveryAddressField.getText().trim().isEmpty()) {
            showWarning("Enter address", "Delivery address is required");
            return;
        }

        try {
            Customer customer = new Customer(name, phone,
                    customerEmailField.getText().trim(),
                    deliveryAddressField.getText().trim());

            DeliveryType deliveryType = pickupRadio.isSelected() ? DeliveryType.PICKUP : DeliveryType.DELIVERY;
            String address = deliveryRadio.isSelected() ? deliveryAddressField.getText().trim() : "Pickup";

            Order order = orderService.createOrder(
                    customer,
                    new ArrayList<>(cartItems),
                    orderNotesArea.getText(),
                    address,
                    deliveryType
            );

            if (order != null) {
                showInfo("Order created!",
                        "Order #" + order.getOrderId() + "\n" +
                                "Total: " + order.getFormattedTotal() + "\n" +
                                "Ready in approx " + order.calculateTotalPreparationTime() + " min");

                clearOrderForm();
                refreshOrdersTable();
                refreshStatistics();
            } else {
                showError("Error", "Could not create order");
            }

        } catch (Exception e) {
            showError("Order creation error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearOrderForm() {
        cartItems.clear();
        customerNameField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        deliveryAddressField.clear();
        orderNotesArea.clear();
        pickupRadio.setSelected(true);
        updateCartTotal();
    }

    // ==================== ORDERS TAB METHODS ====================

    private void setupOrdersTab() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList("All");
        for (OrderStatus status : OrderStatus.values()) {
            statusOptions.add(status.getDisplayName());
        }
        orderStatusFilter.setItems(statusOptions);
        orderStatusFilter.getSelectionModel().selectFirst();
        orderStatusFilter.setOnAction(e -> refreshOrdersTable());

        orderIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderId()));
        orderTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOrderTime().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))));
        orderStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStatus().getDisplayName()));
        orderTotalColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFormattedTotal()));
        orderTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDeliveryType() != null ?
                        data.getValue().getDeliveryType().getDisplayName() : "-"));

        ordersTableView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> showOrderDetails(newVal));

        refreshOrdersTable();
    }

    @FXML
    private void handleRefreshOrders() {
        refreshOrdersTable();
    }

    private void refreshOrdersTable() {
        try {
            String filterValue = orderStatusFilter.getValue();
            List<Order> orders;

            if (filterValue == null || "All".equals(filterValue)) {
                orders = new ArrayList<>(orderService.getActiveOrders());
                orders.addAll(orderService.getOrdersByStatus(OrderStatus.COMPLETED));
                orders.addAll(orderService.getOrdersByStatus(OrderStatus.CANCELLED));
            } else {
                OrderStatus status = null;
                for (OrderStatus s : OrderStatus.values()) {
                    if (s.getDisplayName().equals(filterValue)) {
                        status = s;
                        break;
                    }
                }
                orders = status != null ? orderService.getOrdersByStatus(status) : new ArrayList<>();
            }

            ordersTableView.setItems(FXCollections.observableArrayList(orders));
            ordersCountLabel.setText("Orders: " + orders.size());

        } catch (Exception e) {
            showError("Error loading orders", e.getMessage());
        }
    }

    private void showOrderDetails(Order order) {
        if (order == null) {
            orderDetailsArea.clear();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("===================================\n");
        details.append("           ORDER #").append(order.getOrderId()).append("\n");
        details.append("===================================\n\n");

        details.append("Time: ").append(order.getOrderTime().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n");
        details.append("Status: ").append(order.getStatus().getDisplayName()).append("\n");
        details.append("Type: ").append(order.getDeliveryType() != null ?
                order.getDeliveryType().getDisplayName() : "-").append("\n");

        if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isEmpty()) {
            details.append("Address: ").append(order.getDeliveryAddress()).append("\n");
        }

        details.append("\n---------------------------------\n");
        details.append("ORDER ITEMS:\n");
        details.append("---------------------------------\n");

        if (order.getItems().isEmpty()) {
            details.append("  (items not loaded)\n");
        } else {
            for (OrderItem item : order.getItems()) {
                details.append(String.format("  - %s x%d = %s\n",
                        item.getPizza().getName(),
                        item.getQuantity(),
                        item.getFormattedTotal()));
            }
        }

        details.append("\n---------------------------------\n");
        details.append("TOTAL: ").append(order.getFormattedTotal()).append("\n");
        details.append("---------------------------------\n");

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            details.append("\nNotes: ").append(order.getNotes());
        }

        orderDetailsArea.setText(details.toString());
    }

    @FXML private void handleStatusConfirmed() { changeOrderStatus(OrderStatus.CONFIRMED); }
    @FXML private void handleStatusPreparing() { changeOrderStatus(OrderStatus.PREPARING); }
    @FXML private void handleStatusBaking() { changeOrderStatus(OrderStatus.BAKING); }
    @FXML private void handleStatusReady() { changeOrderStatus(OrderStatus.READY); }
    @FXML private void handleStatusDelivering() { changeOrderStatus(OrderStatus.DELIVERING); }
    @FXML private void handleStatusCompleted() { changeOrderStatus(OrderStatus.COMPLETED); }

    @FXML
    private void handleCancelOrder() {
        changeOrderStatus(OrderStatus.CANCELLED);
    }

    private void changeOrderStatus(OrderStatus newStatus) {
        Order selected = ordersTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select order", "Please select an order from the table");
            return;
        }

        try {
            boolean success = orderService.updateOrderStatus(selected.getOrderId(), newStatus);
            if (success) {
                showInfo("Status changed", "Order #" + selected.getOrderId() +
                        " is now: " + newStatus.getDisplayName());
                refreshOrdersTable();
                refreshStatistics();
            } else {
                showError("Error", "Could not change status");
            }
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    // ==================== STATISTICS TAB METHODS ====================

    private void setupStatisticsTab() {
        refreshStatistics();
    }

    @FXML
    private void handleRefreshStatistics() {
        refreshStatistics();
    }

    private void refreshStatistics() {
        try {
            int totalOrders = orderService.getTotalOrdersCount();
            double totalRevenue = orderService.getTotalRevenue();

            totalOrdersLabel.setText(String.valueOf(totalOrders));
            todayOrdersLabel.setText(String.valueOf(orderService.getTodayOrders().size()));
            totalRevenueLabel.setText(String.format("%.0f T", totalRevenue));
            todayRevenueLabel.setText(String.format("%.0f T", calculateTodayRevenue()));

            pendingCountLabel.setText(String.valueOf(countByStatus(OrderStatus.PENDING)));
            confirmedCountLabel.setText(String.valueOf(countByStatus(OrderStatus.CONFIRMED)));
            preparingCountLabel.setText(String.valueOf(countByStatus(OrderStatus.PREPARING)));
            bakingCountLabel.setText(String.valueOf(countByStatus(OrderStatus.BAKING)));
            readyCountLabel.setText(String.valueOf(countByStatus(OrderStatus.READY)));
            deliveringCountLabel.setText(String.valueOf(countByStatus(OrderStatus.DELIVERING)));
            completedCountLabel.setText(String.valueOf(countByStatus(OrderStatus.COMPLETED)));
            cancelledCountLabel.setText(String.valueOf(countByStatus(OrderStatus.CANCELLED)));

            popularPizzasListView.setItems(FXCollections.observableArrayList(
                    "1. Пепперони",
                    "2. Маргарита",
                    "3. Четыре сыра",
                    "4. Гавайская",
                    "5. Мясная"
            ));

        } catch (Exception e) {
            showError("Statistics error", e.getMessage());
        }
    }

    private int countByStatus(OrderStatus status) {
        try {
            return orderService.getOrdersByStatus(status).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private double calculateTodayRevenue() {
        try {
            return orderService.getTodayOrders().stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .mapToDouble(Order::calculateTotal)
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== SETTINGS TAB METHODS ====================

    private void setupSettingsTab() {
        refreshSettingsPizzaList();

        settingsPizzaListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        loadPizzaToForm(newVal);
                    }
                });

        pizzaSizeCombo.setItems(FXCollections.observableArrayList(Pizza.PizzaSize.values()));
        pizzaSizeCombo.getSelectionModel().selectFirst();

        pizzaCategoryCombo.setItems(FXCollections.observableArrayList(Pizza.Category.values()));
        pizzaCategoryCombo.getSelectionModel().selectFirst();

        languageCombo.setItems(FXCollections.observableArrayList("Russian", "Kazakh", "English"));
        languageCombo.getSelectionModel().selectFirst();
    }

    private void refreshSettingsPizzaList() {
        settingsPizzaListView.setItems(FXCollections.observableArrayList(pizzaService.getAllPizzas()));
    }

    private void loadPizzaToForm(Pizza pizza) {
        editingPizza = pizza;
        pizzaFormTitle.setText("Редактировать");

        pizzaNameField.setText(pizza.getName());
        pizzaDescField.setText(pizza.getDescription());
        pizzaIngredientsField.setText(pizza.getIngredientsString());
        pizzaSizeCombo.setValue(pizza.getSize());
        pizzaPriceField.setText(String.valueOf(pizza.getPrice()));
        pizzaTimeField.setText(String.valueOf(pizza.getPreparationTime()));
        pizzaCategoryCombo.setValue(pizza.getCategory());
        pizzaAvailableCheck.setSelected(pizza.isAvailable());
    }

    @FXML
    private void handleAddPizza() {
        handleResetPizzaForm();
    }

    @FXML
    private void handleEditPizza() {
        Pizza selected = settingsPizzaListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadPizzaToForm(selected);
        } else {
            showWarning("Select pizza", "Select a pizza to edit");
        }
    }

    @FXML
    private void handleDeletePizza() {
        Pizza selected = settingsPizzaListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select pizza", "Select a pizza to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText("Delete pizza?");
        confirm.setContentText("Are you sure you want to delete \"" + selected.getName() + "\"?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (pizzaService.deletePizza(selected.getId())) {
                showInfo("Deleted", "Pizza successfully deleted");
                refreshSettingsPizzaList();
                handleResetPizzaForm();
            } else {
                showError("Error", "Could not delete pizza");
            }
        }
    }

    @FXML
    private void handleSavePizza() {
        String name = pizzaNameField.getText().trim();
        if (name.isEmpty()) {
            showWarning("Fill fields", "Enter pizza name");
            return;
        }

        double price;
        int prepTime;
        try {
            price = Double.parseDouble(pizzaPriceField.getText().trim());
            prepTime = Integer.parseInt(pizzaTimeField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Invalid format", "Price and time must be numbers");
            return;
        }

        List<String> ingredients = List.of(pizzaIngredientsField.getText().split(",\\s*"));

        if (editingPizza != null) {
            editingPizza.setName(name);
            editingPizza.setDescription(pizzaDescField.getText().trim());
            editingPizza.setIngredients(ingredients);
            editingPizza.setSize(pizzaSizeCombo.getValue());
            editingPizza.setPrice(price);
            editingPizza.setPreparationTime(prepTime);
            editingPizza.setCategory(pizzaCategoryCombo.getValue());
            editingPizza.setAvailable(pizzaAvailableCheck.isSelected());

            if (pizzaService.updatePizza(editingPizza)) {
                showInfo("Saved", "Pizza successfully updated");
            } else {
                showError("Error", "Could not update pizza");
            }
        } else {
            String id = "P" + UUID.randomUUID().toString().substring(0, 3).toUpperCase();
            Pizza newPizza = new Pizza(
                    id, name, pizzaDescField.getText().trim(), ingredients,
                    pizzaSizeCombo.getValue(), price, prepTime,
                    pizzaCategoryCombo.getValue(), pizzaAvailableCheck.isSelected()
            );

            if (pizzaService.addPizza(newPizza)) {
                showInfo("Added", "New pizza successfully added");
            } else {
                showError("Error", "Could not add pizza");
            }
        }

        refreshSettingsPizzaList();
        try { loadPizzas(); } catch (Exception ignored) {}
        handleResetPizzaForm();
    }

    @FXML
    private void handleResetPizzaForm() {
        editingPizza = null;
        pizzaFormTitle.setText("Add Pizza");
        pizzaNameField.clear();
        pizzaDescField.clear();
        pizzaIngredientsField.clear();
        pizzaSizeCombo.getSelectionModel().selectFirst();
        pizzaPriceField.clear();
        pizzaTimeField.clear();
        pizzaCategoryCombo.getSelectionModel().selectFirst();
        pizzaAvailableCheck.setSelected(true);
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleNewOrder() {
        mainTabPane.getSelectionModel().select(1);
    }

    @FXML
    private void handleViewOrders() {
        mainTabPane.getSelectionModel().select(2);
        refreshOrdersTable();
    }

    @FXML
    private void handleStatistics() {
        mainTabPane.getSelectionModel().select(3);
        refreshStatistics();
    }

    @FXML
    private void handleSettings() {
        mainTabPane.getSelectionModel().select(4);
    }

    // ==================== DIALOGS ====================

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
