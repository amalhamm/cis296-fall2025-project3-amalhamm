// how Monopoly works, the backend,  brain of the app
package com.amal.project3;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MonopolyGame implements Serializable {

    // Player class to represent each payer in the game
    public static class Player implements Serializable {
        String name;
        int money;
        int position;//where on board
        boolean stuckInJail = false;//true if in jail

        Player(String name, int money) {
            this.name = name;
            this.money = money;
            this.position = 0;
        }
    }

    // Property class:represent a space on board
    public static class Property implements Serializable {
        String name;
        int purchasePrice;
        int baseRent;
        String colorGroup;
        Player owner;
        int houses = 0; // 0-4 houses, 5 = hotel
//if not owned, player can buy it
        Property(String name, int purchasePrice, int baseRent, String colorGroup) {
            this.name = name;
            this.purchasePrice = purchasePrice;
            this.baseRent = baseRent;
            this.colorGroup = colorGroup;
            this.owner = null;
        }
        // SIMPLIFIED: Rent = baseRent × (houses + 1)
        // 0 houses = baseRent × 1
        // 1 house = baseRent × 2
        // 2 houses = baseRent × 3
        // 3 houses = baseRent × 4
        // 4 houses = baseRent × 5
        // Hotel = baseRent × 10
        public int getCurrentRent() {// to calculate how much rent to charge
            if (owner == null)
                return 0;
            if (houses == 0)
                return baseRent;
            if (houses == 5)
                return baseRent * 10; // Hotel
            return baseRent * (houses + 1); // Houses
        }

        // Can build if you own all properties in color group
        public boolean canBuildHouse(Player player, Board board) {
            //null if in jail and special for community chest or chance
            if (owner != player || colorGroup == null || colorGroup.equals("Special"))
                return false;
            if (houses >= 5)
                return false; // Already has hotel
            // Check if player owns all properties in color group
            List<Property> groupProperties = board.getPropertiesInGroup(colorGroup);
            for (Property p : groupProperties) {
                if (p.owner != player)
                    return false;
            }
            return true;//if player owns all the properties in the color grp
        }
        //Fixed house cost
        public int getHouseCost() {
            if (colorGroup == null) //jail
                return 0;
            return 100; // All houses cost $100
        }
    }
    // Board class
    public static class Board implements Serializable {
        List<Property> properties;

        Board() {
            properties = new ArrayList<>();
            initializeBoard();
        }

        private void initializeBoard() {
            // 40 monopoly spaces, knows which property is at which index,purchase and rent prices, and which belongs to each color group.

            properties.add(new Property("GO", 0, 0, "Special"));
            properties.add(new Property("Mediterranean Ave", 60, 10, "Brown"));
            properties.add(new Property("Community Chest", 0, 0, "Special"));
            properties.add(new Property("Baltic Ave", 80, 15, "Brown"));
            properties.add(new Property("Income Tax", 0, 0, "Special"));

            properties.add(new Property("Reading Railroad", 200, 50, "Railroad"));
            properties.add(new Property("Oriental Ave", 100, 20, "Light Blue"));
            properties.add(new Property("Chance", 0, 0, "Special"));
            properties.add(new Property("Vermont Ave", 100, 20, "Light Blue"));
            properties.add(new Property("Connecticut Ave", 120, 25, "Light Blue"));

            properties.add(new Property("Jail", 0, 0, "Special"));
            properties.add(new Property("St. Charles Place", 140, 30, "Pink"));
            properties.add(new Property("Electric Company", 150, 40, "Utility"));
            properties.add(new Property("States Ave", 140, 30, "Pink"));
            properties.add(new Property("Virginia Ave", 160, 35, "Pink"));

            properties.add(new Property("Pennsylvania Railroad", 200, 50, "Railroad"));
            properties.add(new Property("St. James Place", 180, 40, "Orange"));
            properties.add(new Property("Community Chest", 0, 0, "Special"));
            properties.add(new Property("Tennessee Ave", 180, 40, "Orange"));
            properties.add(new Property("New York Ave", 200, 45, "Orange"));

            properties.add(new Property("Free Parking", 0, 0, "Special"));
            properties.add(new Property("Kentucky Ave", 220, 50, "Red"));
            properties.add(new Property("Chance", 0, 0, "Special"));
            properties.add(new Property("Indiana Ave", 220, 50, "Red"));
            properties.add(new Property("Illinois Ave", 240, 55, "Red"));

            properties.add(new Property("B&O Railroad", 200, 50, "Railroad"));
            properties.add(new Property("Atlantic Ave", 260, 60, "Yellow"));
            properties.add(new Property("Ventnor Ave", 260, 60, "Yellow"));
            properties.add(new Property("Water Works", 150, 40, "Utility"));
            properties.add(new Property("Marvin Gardens", 280, 65, "Yellow"));

            properties.add(new Property("Go to Jail", 0, 0, "Special"));
            properties.add(new Property("Pacific Ave", 300, 70, "Green"));
            properties.add(new Property("North Carolina Ave", 300, 70, "Green"));
            properties.add(new Property("Community Chest", 0, 0, "Special"));
            properties.add(new Property("Pennsylvania Ave", 320, 75, "Green"));

            properties.add(new Property("Short Line Railroad", 200, 50, "Railroad"));
            properties.add(new Property("Chance", 0, 0, "Special"));
            properties.add(new Property("Park Place", 350, 80, "Dark Blue"));
            properties.add(new Property("Luxury Tax", 0, 0, "Special"));
            properties.add(new Property("Boardwalk", 400, 100, "Dark Blue"));
        }

        int size() {//returns how many spaces on board
            return properties.size();//returns 40
        }
//position 0-39
        Property getPropertyAt(int pos) {//get property/space at the index starts with pos=o returns Go
            return properties.get(pos);
        }
//finds and returns all properties that share that color group(red, green, utility)
        //used to check if a player owns all properties in a color group
        List<Property> getPropertiesInGroup(String colorGroup) {
            List<Property> groupProps = new ArrayList<>();
            for (Property p : properties) {
                if (colorGroup.equals(p.colorGroup)) {
                    groupProps.add(p);
                }
            }
            return groupProps;//a list containing properties in same color grp
        }
    }

    //  Card class:Represents Chance and Community Chest cards
    public static class Card implements Serializable {
        String description;
        CardAction action;

        Card(String description, CardAction action) {//constructor
            this.description = description;
            this.action = action;
        }
    }
//Each card performs an action (gain money, go to jail...), for chest and comm chest cards
    public interface CardAction extends Serializable {
        void execute(Player player, Board board, List<Player> players);
    }

    // Game state
    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private List<Card> chanceCards;
    private List<Card> communityChestCards;
    private int chanceCardIndex;
    private int communityChestCardIndex;

    public MonopolyGame(List<String> playerNames) {
        board = new Board();
        players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name, 1500));//each player starts with 1500 money
        }
        currentPlayerIndex = 0;
        initializeCards();
    }

    //  Only 4 cards each
    private void initializeCards() {
        chanceCards = new ArrayList<>();
        communityChestCards = new ArrayList<>();

        // Chance cards(4)
        chanceCards.add(new Card("Advance to GO! Collect $200", (p, b, ps) -> {
            p.position = 0;
            p.money += 200;
        }));

        chanceCards.add(new Card("Go to Jail", (p, b, ps) -> {
            p.position = 10;
            p.stuckInJail = true;
        }));

        chanceCards.add(new Card("Bank pays you $50", (p, b, ps) -> {
            p.money += 50;
        }));

        chanceCards.add(new Card("Pay $50 tax", (p, b, ps) -> {
            p.money -= 50;
        }));

        // Community Chest cards(4)
        communityChestCards.add(new Card("Bank error in your favor! Collect $200", (p, b, ps) -> {
            p.money += 200;
        }));

        communityChestCards.add(new Card("Go to Jail", (p, b, ps) -> {
            p.position = 10;
            p.stuckInJail = true;
        }));

        communityChestCards.add(new Card("Receive $100", (p, b, ps) -> {
            p.money += 100;
        }));

        communityChestCards.add(new Card("Pay doctor's fee $50", (p, b, ps) -> {
            p.money -= 50;
        }));
//mix cards randomly in different way
        Collections.shuffle(chanceCards);
        Collections.shuffle(communityChestCards);
        chanceCardIndex = 0;
        communityChestCardIndex = 0;
    }

    public Card drawChanceCard() {
        Card card = chanceCards.get(chanceCardIndex);
        chanceCardIndex = (chanceCardIndex + 1) % chanceCards.size();
        return card;
    }

    public Card drawCommunityChestCard() {
        Card card = communityChestCards.get(communityChestCardIndex);
        communityChestCardIndex = (communityChestCardIndex + 1) % communityChestCards.size();//move players
        return card;
    }

    // Getters
    public Board getBoard() {
        return board; }
    public List<Player> getPlayers() {
        return players; }
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex); }
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex; }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
    public void removePlayer(Player player) {
        for (Property p : board.properties) {
            if (p.owner == player) {//then set the players owns to null and houses to 0
                p.owner = null;
                p.houses = 0;
            }
        }
        players.remove(player);
        if (currentPlayerIndex >= players.size()) {//player index cannot be greater than or equal the players size
            currentPlayerIndex = 0;
        }
    }

    // Save and load
    //saving players, money, and board states  to a file
    public void saveGame(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(Paths.get(filename)))) {
            out.writeObject(this);
        }
    }

    public static MonopolyGame loadGame(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(Paths.get(filename)))) {
            return (MonopolyGame) in.readObject();
        }
    }
}