package org.example;// ClientThread.java

import org.example.WordleClient;
import org.example.WordleGUI;

import java.io.IOException;

// Clasa care rulează într-un thread separat pentru a primi datele de la server
//nu vreau sa blochez gui
public class ClientThread extends Thread {
    private WordleClient client;
    private WordleGUI gui;

    // Constructorul trebuie să accepte DOUĂ argumente pentru a actualiza GUI-ul
    public ClientThread(WordleClient client, WordleGUI gui) {
        this.client = client;
        this.gui = gui; // Stochează referința la interfața grafică
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = client.receiveMessage()) != null) {
                // Trimite mesajul primit la interfața grafică pentru actualizare
                if (gui != null) {
                    gui.updateGUI(serverMessage);
                } else {
                    System.out.println("[SERVER] " + serverMessage);
                }
            }
        } catch (IOException e) {
            System.err.println("Conexiune pierdută: " + e.getMessage());
        } finally {
            client.close();
        }
    }
}