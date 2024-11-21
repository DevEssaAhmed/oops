import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class RecipeRecommenderSystem {

    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JTextField userNameField, customDietField, feedbackCommentField, timerDurationField;
    private JComboBox<String> dietChoice, ratingChoice;
    private JLabel recipeLabel, messageLabel, timerLabel;
    private JList<String> groceryListDisplay, favoritesList;
    private DefaultListModel<String> groceryListModel, favoritesListModel;
    private ArrayList<String> loadedRecipes = new ArrayList<>();
    private ArrayList<String> favoriteRecipes = new ArrayList<>();
    private int currentRecipeIndex = -1; // Track the current recipe index
    private Timer cookingTimer;
    private int secondsLeft = 0;
    private Map<String, Integer> recipeRatings = new HashMap<>();
    private String currentUser = "";
    private String savedDietPreference = "";

    public RecipeRecommenderSystem() {
        mainFrame = new JFrame("Recipe Recommender System");
        mainFrame.setSize(600, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        loadRecipesFromFile();
        loadFavoritesFromFile();
        loadUserPreferences();
        setupMainGUI();

        mainFrame.add(contentPanel);
        mainFrame.setVisible(true);
    }

    private void startCookingTimer(int seconds) {
        // Initialize the timer and the remaining time
        secondsLeft = seconds;
        if (cookingTimer != null && cookingTimer.isRunning()) {
            cookingTimer.stop(); // Stop previous timer if running
        }

        // Create a new timer to decrement the time every second
        cookingTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secondsLeft > 0) {
                    secondsLeft--;
                    int minutes = secondsLeft / 60;
                    int remainingSeconds = secondsLeft % 60;
                    timerLabel.setText(String.format("Time Left: %02d:%02d", minutes, remainingSeconds));
                } else {
                    cookingTimer.stop(); // Stop timer when time is up
                    timerLabel.setText("Time's up!");
                    showMessage("Time's up!");
                }
            }
        });

        cookingTimer.start(); // Start the timer
    }

    private void stopCookingTimer() {
        if (cookingTimer != null && cookingTimer.isRunning()) {
            cookingTimer.stop(); // Stop the timer
            timerLabel.setText("Timer Stopped");
            showMessage("Timer stopped.");
        }
    }

    private void showMessage(String message) {
        if (messageLabel == null) {
            messageLabel = new JLabel(message);
            messageLabel.setForeground(Color.RED);
            mainFrame.add(messageLabel, BorderLayout.SOUTH);
        }
        messageLabel.setText(message);
        mainFrame.revalidate();
    }

    private JPanel createLoginScreen(Color bgColor, Color textColor, Color accentColor) {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBackground(bgColor);

        JLabel label = new JLabel("Username:");
        label.setForeground(textColor);
        userNameField = new JTextField(20);
        JButton nextButton = createButton("Login", accentColor);

        nextButton.addActionListener(e -> {
            if (!userNameField.getText().isEmpty()) {
                currentUser = userNameField.getText();
                saveToTextFile("login.txt", "User: " + currentUser);
                cardLayout.next(contentPanel);
            } else {
                showMessage("Please enter a name.");
            }
        });

        loginPanel.add(label);
        loginPanel.add(userNameField);
        loginPanel.add(nextButton);

        return loginPanel;
    }

    private JButton createButton(String label, Color color) {
        JButton button = new JButton(label);
        button.setBackground(color);
        button.setFocusPainted(false);
        return button;
    }

    private void loadRecipesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("recipes.txt"))) {
            String line;
            StringBuilder recipeBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    loadedRecipes.add(recipeBuilder.toString().trim());
                    recipeBuilder.setLength(0);
                } else {
                    recipeBuilder.append(line).append("\n");
                }
            }
            if (recipeBuilder.length() > 0) {
                loadedRecipes.add(recipeBuilder.toString().trim());
            }
        } catch (IOException e) {
            showMessage("Error loading recipes from file.");
        }
    }

    private void saveToTextFile(String filename, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            showMessage("Error saving to file: " + filename);
        }
    }

    private void loadFavoritesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("favorites.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                favoriteRecipes.add(line.trim());
            }
        } catch (IOException e) {
            showMessage("Error loading favorites from file.");
        }
    }

    private void loadUserPreferences() {
        try (BufferedReader reader = new BufferedReader(new FileReader("user_preferences.txt"))) {
            String line;
            if ((line = reader.readLine()) != null) {
                savedDietPreference = line.trim();
            }
        } catch (IOException e) {
            savedDietPreference = "";
        }
    }

    private void setupMainGUI() {
        Color bgColor = Color.BLACK;
        Color textColor = Color.WHITE;
        Color accentColor = Color.RED;

        JPanel loginPanel = createLoginScreen(bgColor, textColor, accentColor);
        contentPanel.add(loginPanel, "Login");

        JPanel dietPanel = createDietaryScreen(bgColor, textColor, accentColor);
        contentPanel.add(dietPanel, "DietaryPreference");

        JPanel recipePanel = createRecipeScreen(bgColor, textColor, accentColor);
        contentPanel.add(recipePanel, "RecipeSuggestion");

        JPanel favoritesPanel = createFavoritesScreen(bgColor, textColor, accentColor);
        contentPanel.add(favoritesPanel, "Favorites");

        JPanel groceryPanel = createGroceryListScreen(bgColor, textColor, accentColor);
        contentPanel.add(groceryPanel, "GroceryList");

        JPanel feedbackPanel = createFeedbackScreen(bgColor, textColor, accentColor);
        contentPanel.add(feedbackPanel, "Feedback");
    }

    private JPanel createRecipeScreen(Color bgColor, Color textColor, Color accentColor) {
        JPanel recipePanel = new JPanel();
        recipePanel.setLayout(new BoxLayout(recipePanel, BoxLayout.Y_AXIS)); // Vertical layout for components
        recipePanel.setBackground(bgColor);

        recipeLabel = new JLabel("Suggested Recipe:");
        recipeLabel.setForeground(textColor);

        // Buttons to navigate through recipes
        JButton nextRecipeButton = createButton("Next Recipe", accentColor);
        JButton previousRecipeButton = createButton("Previous Recipe", accentColor);

        nextRecipeButton.addActionListener(e -> showNextRecipe());
        previousRecipeButton.addActionListener(e -> showPreviousRecipe());

        JLabel ratingLabel = new JLabel("Rate this Recipe:");
        ratingLabel.setForeground(textColor);
        ratingChoice = new JComboBox<>();
        for (int i = 1; i <= 5; i++) {
            ratingChoice.addItem(i + " Stars");
        }

        JButton addToFavoritesButton = createButton("Add to Favorites", accentColor);
        addToFavoritesButton.addActionListener(e -> {
            if (currentRecipeIndex >= 0 && currentRecipeIndex < loadedRecipes.size()) {
                String recipe = loadedRecipes.get(currentRecipeIndex);
                favoriteRecipes.add(recipe);
                saveFavoriteRecipe(recipe);
            }
        });

        JButton startTimerButton = createButton("Start Timer", accentColor);
        JButton stopTimerButton = createButton("Stop Timer", accentColor);
        startTimerButton.addActionListener(e -> {
            String timerInput = timerDurationField.getText();
            try {
                int timerSeconds = Integer.parseInt(timerInput) * 60;
                startCookingTimer(timerSeconds);
            } catch (NumberFormatException ex) {
                showMessage("Invalid time input.");
            }
        });

        stopTimerButton.addActionListener(e -> stopCookingTimer());

        timerDurationField = new JTextField("10", 5);
        timerLabel = new JLabel("Time Left: 00:00");

        // Add components to the recipePanel
        recipePanel.add(recipeLabel);
        recipePanel.add(previousRecipeButton);
        recipePanel.add(nextRecipeButton);
        recipePanel.add(ratingLabel);
        recipePanel.add(ratingChoice);
        recipePanel.add(addToFavoritesButton);
        recipePanel.add(timerDurationField);
        recipePanel.add(startTimerButton);
        recipePanel.add(stopTimerButton);
        recipePanel.add(timerLabel);

        return recipePanel;
    }

    private void showNextRecipe() {
        if (loadedRecipes.size() > 0) {
            currentRecipeIndex = (currentRecipeIndex + 1) % loadedRecipes.size();
            recipeLabel.setText(loadedRecipes.get(currentRecipeIndex));
        }
    }


    private void showPreviousRecipe() {
        if (loadedRecipes.size() > 0) {
            currentRecipeIndex = (currentRecipeIndex - 1 + loadedRecipes.size()) % loadedRecipes.size();
            recipeLabel.setText(loadedRecipes.get(currentRecipeIndex));
        }
    }

    private void saveFavoriteRecipe(String recipe) {
        saveToTextFile("favorites.txt", recipe);
    }

    private JPanel createDietaryScreen(Color bgColor, Color textColor, Color accentColor) {
        JPanel dietPanel = new JPanel();
        dietPanel.setLayout(new BoxLayout(dietPanel, BoxLayout.Y_AXIS));
        dietPanel.setBackground(bgColor);

        JLabel dietLabel = new JLabel("Select Dietary Preference:");
        dietLabel.setForeground(textColor);
        dietChoice = new JComboBox<>(new String[] {"Vegetarian", "Vegan", "Gluten-Free", "Non-Vegetarian", "Custom"});
        dietChoice.setSelectedItem(savedDietPreference.isEmpty() ? "Custom" : savedDietPreference);

        JButton saveDietButton = createButton("Save Dietary Preference", accentColor);
        saveDietButton.addActionListener(e -> {
            savedDietPreference = (String) dietChoice.getSelectedItem();
            saveToTextFile("user_preferences.txt", savedDietPreference);
            showMessage("Dietary preference saved: " + savedDietPreference);
            cardLayout.next(contentPanel);
        });

        dietPanel.add(dietLabel);
        dietPanel.add(dietChoice);
        dietPanel.add(saveDietButton);

        return dietPanel;
    }

    private JPanel createFavoritesScreen(Color bgColor, Color textColor, Color accentColor) {
        JPanel favoritesPanel = new JPanel();
        favoritesPanel.setLayout(new BoxLayout(favoritesPanel, BoxLayout.Y_AXIS));
        favoritesPanel.setBackground(bgColor);

        favoritesListModel = new DefaultListModel<>();
        for (String recipe : favoriteRecipes) {
            favoritesListModel.addElement(recipe);
        }
        favoritesList = new JList<>(favoritesListModel);

        JScrollPane favoritesScrollPane = new JScrollPane(favoritesList);
        JButton removeFromFavoritesButton = createButton("Remove from Favorites", accentColor);
        removeFromFavoritesButton.addActionListener(e -> {
            String selectedRecipe = favoritesList.getSelectedValue();
            if (selectedRecipe != null) {
                favoriteRecipes.remove(selectedRecipe);
                favoritesListModel.removeElement(selectedRecipe);
                saveToTextFile("favorites.txt", "Removed: " + selectedRecipe);
                showMessage("Recipe removed from favorites.");
            } else {
                showMessage("Please select a recipe to remove.");
            }
        });

        favoritesPanel.add(new JLabel("Favorite Recipes:"));
        favoritesPanel.add(favoritesScrollPane);
        favoritesPanel.add(removeFromFavoritesButton);

        return favoritesPanel;
    }

    private JPanel createGroceryListScreen(Color bgColor, Color textColor, Color accentColor) {
        JPanel groceryPanel = new JPanel();
        groceryPanel.setLayout(new BoxLayout(groceryPanel, BoxLayout.Y_AXIS));
        groceryPanel.setBackground(bgColor);

        groceryListModel = new DefaultListModel<>();
        groceryListDisplay = new JList<>(groceryListModel);

        JScrollPane groceryScrollPane = new JScrollPane(groceryListDisplay);
        JButton addToGroceryListButton = createButton("Add to Grocery List", accentColor);
        addToGroceryListButton.addActionListener(e -> {
            if (currentRecipeIndex >= 0 && currentRecipeIndex < loadedRecipes.size()) {
                String recipe = loadedRecipes.get(currentRecipeIndex);
                // For simplicity, we'll assume the grocery list is just the recipe name for now
                groceryListModel.addElement(recipe);
                showMessage("Recipe added to grocery list.");
            }
        });

        groceryPanel.add(new JLabel("Grocery List:"));
        groceryPanel.add(groceryScrollPane);
        groceryPanel.add(addToGroceryListButton);

        return groceryPanel;
    }

    private JPanel createFeedbackScreen(Color bgColor, Color textColor, Color accentColor) {
        JPanel feedbackPanel = new JPanel();
        feedbackPanel.setLayout(new BoxLayout(feedbackPanel, BoxLayout.Y_AXIS));
        feedbackPanel.setBackground(bgColor);

        JLabel feedbackLabel = new JLabel("Provide Feedback:");
        feedbackLabel.setForeground(textColor);

        feedbackCommentField = new JTextField(20);
        JButton submitFeedbackButton = createButton("Submit Feedback", accentColor);

        submitFeedbackButton.addActionListener(e -> {
            String feedback = feedbackCommentField.getText().trim();
            if (!feedback.isEmpty()) {
                saveToTextFile("feedback.txt", "User: " + currentUser + " - Feedback: " + feedback);
                feedbackCommentField.setText(""); // Clear input field
                showMessage("Thank you for your feedback!");
            } else {
                showMessage("Please enter feedback before submitting.");
            }
        });

        feedbackPanel.add(feedbackLabel);
        feedbackPanel.add(feedbackCommentField);
        feedbackPanel.add(submitFeedbackButton);

        return feedbackPanel;
    }

    public static void main(String[] args) {
        new RecipeRecommenderSystem();
    }
}
