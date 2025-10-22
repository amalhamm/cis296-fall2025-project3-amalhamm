//conncts UI-backend:use monopoly gave and update the UI
package com.amal.project3;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Project3controller {

    //  START SCREEN
    @FXML
    private TextField numPlayersField;
    @FXML private VBox playerNamesBox;
    @FXML private Button startGameButton;
    @FXML private Button loadGameButton;
    @FXML private Label errorLabel;

    // GAME SCREEN
    @FXML private GridPane boardGrid;
    @FXML private Label currentPlayerLabel;
    @FXML private Label playerMoneyLabel;
    @FXML private Label diceResultLabel;
    @FXML private Button rollDiceButton;
    @FXML private Button buyPropertyButton;
    @FXML private Button buildHouseButton;
    @FXML private Button endTurnButton;
    @FXML private Button saveGameButton;

    //GAME STATE
    private MonopolyGame game;
    private Random rand = new Random();
    private boolean rolledThisTurn = false;
    private final List<TextField> playerNameFields = new ArrayList<>();

    // Color mapping
    private static final Map<String, Color> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put("Brown", Color.rgb(139, 69, 19));
        COLOR_MAP.put("Light Blue", Color.LIGHTBLUE);
        COLOR_MAP.put("Pink", Color.PINK);
        COLOR_MAP.put("Orange", Color.ORANGE);
        COLOR_MAP.put("Red", Color.RED);
        COLOR_MAP.put("Yellow", Color.YELLOW);
        COLOR_MAP.put("Green", Color.GREEN);
        COLOR_MAP.put("Dark Blue", Color.DARKBLUE);
        COLOR_MAP.put("Railroad", Color.BLACK);
        COLOR_MAP.put("Utility", Color.LIGHTGRAY);
    }

    @FXML
    public void initialize() {
        if (loadGameButton != null) {//save the game
            File saveFile = new File("monopoly_save.dat");
            loadGameButton.setDisable(!saveFile.exists());
        }
        if (boardGrid != null && game != null) {
            initializeBoard();
            updateUI();
        }
    }

    //START SCREEN METHODS
    @FXML
    public void onGeneratePlayerFields() {
        try {
            int numPlayers = Integer.parseInt(numPlayersField.getText());
            if (numPlayers < 2 || numPlayers > 8) {
                errorLabel.setText("Please enter 2-8 players");
                return;
            }

            errorLabel.setText("");
            playerNamesBox.getChildren().clear();
            playerNameFields.clear();

            for (int i = 1; i <= numPlayers; i++) {//show text fields equal to nb of players inserted
                Label label = new Label("Player " + i + " Name:");
                TextField textField = new TextField();
                textField.setPromptText("Enter name");
                playerNameFields.add(textField);
                playerNamesBox.getChildren().addAll(label, textField);
            }
            startGameButton.setDisable(false);
        } catch (NumberFormatException e) {
            errorLabel.setText("Please enter a valid number");
        }
    }

    @FXML
    public void onStartGame() {
        try {
            List<String> playerNames = new ArrayList<>();
            for (TextField field : playerNameFields) {
                String name = field.getText().trim();
                if (name.isEmpty()) {
                    errorLabel.setText("All player names must be filled");
                    return;
                }
                playerNames.add(name);
            }
            game = new MonopolyGame(playerNames);
            loadGameScreen();
        } catch (Exception e) {
            errorLabel.setText("Error starting game: " + e.getMessage());
        }
    }

    @FXML
    public void onLoadGame() {
        try {
            game = MonopolyGame.loadGame("monopoly_save.dat");
            loadGameScreen();
        } catch (Exception e) {
            errorLabel.setText("Error loading game: " + e.getMessage());
        }
    }

    private void loadGameScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 900);//size of game screen
        Project3controller controller = loader.getController();
        controller.setGame(game);
        Stage stage = (Stage) startGameButton.getScene().getWindow();//continue to window 2 for the game
        stage.setScene(scene);
    }

    //  GAME SCREEN METHODS
    public void setGame(MonopolyGame game) {//links the game logic to the controller to know what monopolygame object to use
        this.game = game;
        initializeBoard();
        updateUI();
    }


    private void initializeBoard() {
        boardGrid.getChildren().clear();

        // Create 40-space board layout (11x11 grid)
        for (int i = 0; i <= 10; i++)
            addBoardSpace(i, 10, i);
        for (int i = 1; i <= 10; i++)
            addBoardSpace(10, 10 - i, 10 + i);
        for (int i = 1; i <= 10; i++)
            addBoardSpace(10 - i, 0, 20 + i);
        for (int i = 1; i < 10; i++)
            addBoardSpace(0, i, 30 + i);

        // Center logo
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: black;");
        Label titleLabel = new Label("MONOPOLY");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        centerBox.getChildren().add(titleLabel);
        GridPane.setRowSpan(centerBox, 9);
        GridPane.setColumnSpan(centerBox, 9);
        boardGrid.add(centerBox, 1, 1);
    }

    private void addBoardSpace(int col, int row, int position) {
        if (position >= game.getBoard().size())
            return;
        MonopolyGame.Property property = game.getBoard().getPropertyAt(position);
        VBox spaceBox = new VBox(2);
        spaceBox.setAlignment(Pos.CENTER);
        spaceBox.setPadding(new Insets(2));
        spaceBox.setPrefSize(70, 70);
        spaceBox.setStyle("-fx-border-color: black; -fx-background-color: white;");

        // Color bar for properties
        if (property.colorGroup != null && !property.colorGroup.equals("Special")) {
            Rectangle colorBar = new Rectangle(65, 12);
            colorBar.setFill(COLOR_MAP.getOrDefault(property.colorGroup, Color.GRAY));
            spaceBox.getChildren().add(colorBar);
        }

        // Property name
        Label nameLabel = new Label(property.name);
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font(7));
        nameLabel.setMaxWidth(65);
        spaceBox.getChildren().add(nameLabel);

        // Price
        if (property.purchasePrice > 0) {
            Label priceLabel = new Label("$" + property.purchasePrice);
            priceLabel.setFont(Font.font(6));
            spaceBox.getChildren().add(priceLabel);
        }

        // Owner
        Label ownerLabel = new Label();
        ownerLabel.setFont(Font.font(6));
        ownerLabel.setId("owner-" + position);
        spaceBox.getChildren().add(ownerLabel);

        // Houses/Hotel
        HBox housesBox = new HBox(1);
        housesBox.setAlignment(Pos.CENTER);
        housesBox.setId("houses-" + position);
        spaceBox.getChildren().add(housesBox);

        // Player tokens
        HBox tokensBox = new HBox(2);
        tokensBox.setAlignment(Pos.CENTER);
        tokensBox.setId("tokens-" + position);
        spaceBox.getChildren().add(tokensBox);

        boardGrid.add(spaceBox, col, row);
    }

    private void updateUI() {
        MonopolyGame.Player currentPlayer = game.getCurrentPlayer();
        currentPlayerLabel.setText("Current Player: " + currentPlayer.name);
        playerMoneyLabel.setText("Money: $" + currentPlayer.money);

        // Update all positions
        for (int i = 0; i < game.getBoard().size(); i++) {
            // Update tokens
            HBox tokensBox = (HBox) boardGrid.lookup("#tokens-" + i);
            if (tokensBox != null) {
                tokensBox.getChildren().clear();
                for (int p = 0; p < game.getPlayers().size(); p++) {
                    if (game.getPlayers().get(p).position == i) {
                        Circle token = new Circle(4, getPlayerColor(p));
                        tokensBox.getChildren().add(token);
                    }
                }
            }

            // Update ownership and houses
            MonopolyGame.Property property = game.getBoard().getPropertyAt(i);
            Label ownerLabel = (Label) boardGrid.lookup("#owner-" + i);
            if (ownerLabel != null) {
                ownerLabel.setText(property.owner != null ?
                        property.owner.name.substring(0, Math.min(3, property.owner.name.length())) : "");
            }

            HBox housesBox = (HBox) boardGrid.lookup("#houses-" + i);
            if (housesBox != null) {
                housesBox.getChildren().clear();
                if (property.houses > 0 && property.houses < 5) {
                    for (int h = 0; h < property.houses; h++) {
                        housesBox.getChildren().add(new Rectangle(6, 6, Color.GREEN));
                    }
                } else if (property.houses == 5) {
                    housesBox.getChildren().add(new Rectangle(12, 12, Color.RED));
                }
            }
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        MonopolyGame.Player player = game.getCurrentPlayer();
        MonopolyGame.Property prop = game.getBoard().getPropertyAt(player.position);

        rollDiceButton.setDisable(rolledThisTurn);
        buyPropertyButton.setDisable(!(rolledThisTurn && prop.owner == null &&
                prop.purchasePrice > 0 && player.money >= prop.purchasePrice));

        boolean canBuild = false;
        if (rolledThisTurn) {
            for (MonopolyGame.Property p : game.getBoard().properties) {
                if (p.canBuildHouse(player, game.getBoard()) && player.money >= p.getHouseCost()) {
                    canBuild = true;
                    break;
                }
            }
        }
        buildHouseButton.setDisable(!canBuild);
        endTurnButton.setDisable(!rolledThisTurn);
    }

    @FXML
    public void onRollDice() {
        MonopolyGame.Player player = game.getCurrentPlayer();

        // Handle jail
        if (player.stuckInJail) {
            if (player.money >= 50) {
                player.money -= 50;
                player.stuckInJail = false;
                showMessage(player.name + " paid $50 to get out of jail");
            } else {
                showMessage(player.name + " can't afford jail. Turn skipped.");
                endTurn();
                return;
            }
        }

        // Roll dice
        int die1 = rand.nextInt(6) + 1;
        int die2 = rand.nextInt(6) + 1;
        int total = die1 + die2;
        diceResultLabel.setText("Rolled: " + die1 + " + " + die2 + " = " + total);

        // Move player
        int oldPos = player.position;
        player.position = (player.position + total) % game.getBoard().size();

        // Pass GO
        if (player.position < oldPos) {
            player.money += 200;
            showMessage(player.name + " passed GO! Collected $200");
        }

        MonopolyGame.Property landedProp = game.getBoard().getPropertyAt(player.position);
        showMessage(player.name + " landed on " + landedProp.name);
        rolledThisTurn = true;

        handleLandedProperty(landedProp);
        updateUI();
    }

    private void handleLandedProperty(MonopolyGame.Property prop) {
        MonopolyGame.Player player = game.getCurrentPlayer();

        if (prop.name.equals("Go to Jail")) {
            showMessage(player.name + " goes to jail!");
            player.position = 10;
            player.stuckInJail = true;
        } else if (prop.name.equals("Chance")) {
            MonopolyGame.Card card = game.drawChanceCard();
            showMessage("Chance: " + card.description);
            card.action.execute(player, game.getBoard(), game.getPlayers());
        } else if (prop.name.equals("Community Chest")) {
            MonopolyGame.Card card = game.drawCommunityChestCard();
            showMessage("Community Chest: " + card.description);
            card.action.execute(player, game.getBoard(), game.getPlayers());
        } else if (prop.name.equals("Income Tax")) {
            player.money -= 200;
            showMessage(player.name + " paid $200 income tax");
        } else if (prop.name.equals("Luxury Tax")) {
            player.money -= 100;
            showMessage(player.name + " paid $100 luxury tax");
        } else if (prop.owner != null && prop.owner != player) {
            int rent = prop.getCurrentRent();
            player.money -= rent;
            prop.owner.money += rent;
            showMessage(player.name + " paid $" + rent + " rent to " + prop.owner.name);

            if (player.money <= 0) {
                showMessage(player.name + " is bankrupt!");
                game.removePlayer(player);
                if (game.getPlayers().size() == 1) {
                    showAlert("Game Over", "Winner!",
                            game.getPlayers().get(0).name + " wins!");
                }
            }
        }
        updateUI();
    }

    @FXML
    public void onBuyProperty() {
        MonopolyGame.Player player = game.getCurrentPlayer();
        MonopolyGame.Property prop = game.getBoard().getPropertyAt(player.position);

        if (prop.owner == null && prop.purchasePrice > 0 && player.money >= prop.purchasePrice) {
            player.money -= prop.purchasePrice;
            prop.owner = player;
            showMessage(player.name + " bought " + prop.name + " for $" + prop.purchasePrice);
            updateUI();
        }
    }

    @FXML
    public void onBuildHouse() {
        MonopolyGame.Player player = game.getCurrentPlayer();
        List<MonopolyGame.Property> buildable = new ArrayList<>();

        for (MonopolyGame.Property p : game.getBoard().properties) {
            if (p.canBuildHouse(player, game.getBoard()) && player.money >= p.getHouseCost()) {
                buildable.add(p);
            }
        }

        if (buildable.isEmpty()) return;

        ChoiceDialog<MonopolyGame.Property> dialog = new ChoiceDialog<>(buildable.get(0), buildable);
        dialog.setTitle("Build House/Hotel");
        dialog.setHeaderText("Select property to build on");
        dialog.showAndWait().ifPresent(prop -> {
            int cost = prop.getHouseCost();
            if (player.money >= cost) {
                player.money -= cost;
                prop.houses++;
                showMessage(player.name + " built a " + (prop.houses == 5 ? "hotel" : "house") +
                        " on " + prop.name + " for $" + cost);
                updateUI();
            }
        });
    }

    @FXML
    public void onEndTurn() {
        endTurn();
    }

    private void endTurn() {
        rolledThisTurn = false;
        diceResultLabel.setText("Click Roll Dice");
        game.nextPlayer();
        updateUI();
    }

    @FXML
    public void onSaveGame() {
        try {
            game.saveGame("monopoly_save.dat");
            showAlert("Save Game", null, "Game saved successfully!");
        } catch (IOException e) {
            showAlert("Save Error", "Failed to save", e.getMessage());
        }
    }

    private Color getPlayerColor(int index) {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.PURPLE, Color.ORANGE, Color.CYAN, Color.MAGENTA};
        return colors[index % colors.length];
    }

    private void showMessage(String message) {
        System.out.println(message); // Print to console
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}