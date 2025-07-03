package com.narangga.swingapp;

public class PetCareApplication {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        // Start the main menu
        MainMenu mainMenu = new MainMenu();
        mainMenu.setVisible(true);
    }
}
