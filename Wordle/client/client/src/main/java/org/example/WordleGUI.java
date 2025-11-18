package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.application.Platform;
import java.io.IOException;

public class WordleGUI extends Application {
    private static final int MAX_ATTEMPTS = 6;
    private GuessRow[] grid = new GuessRow[MAX_ATTEMPTS];
    private TextField inputField;
    private WordleClient client;
    private int currentRow = 0;
    private Label statusLabel;
    private Label validationLabel; // NOU: Eticheta pentru erori de validare

    // ATENȚIE: Am eliminat checkAttemptsButton, deoarece nu mai este necesar!

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // --- 1. Încercare Conexiune și Threading ---
        try {
            client = new WordleClient();
            new ClientThread(client, this).start();
        } catch (IOException e) {
            statusLabel = new Label("Eroare: Nu se poate conecta la serverul Wordle.");
            primaryStage.setTitle("Wordle - FĂRĂ CONEXIUNE");
            primaryStage.setScene(new Scene(new VBox(20, statusLabel), 400, 150));
            primaryStage.show();
            return;
        }

        // --- 2. Configurare GUI ---
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        // Creează grila
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            grid[i] = new GuessRow();
            root.getChildren().add(grid[i]);
        }

        // Câmpul de Intrare
        inputField = new TextField();
        inputField.setMaxWidth(300);
        inputField.setPromptText("Introdu cuvântul de 5 litere și apasă Enter");
        // Apelăm handleGuess la apăsarea Enter
        inputField.setOnAction(e -> handleGuess(inputField.getText()));

        // Eticheta de Validare (pentru erorile de dicționar)
        validationLabel = new Label("");
        validationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");

        // Eticheta de Status General (rămâne de la server/mesaje)
        statusLabel = new Label("Așteaptă mesaj de bun venit de la server...");

        // Adăugăm elementele la rădăcină
        VBox inputContainer = new VBox(5);
        inputContainer.setAlignment(Pos.CENTER);
        inputContainer.getChildren().addAll(inputField, validationLabel); // Fără buton UDP

        root.getChildren().addAll(inputContainer, statusLabel);

        // --- 3. Afișare Fereastră ---
        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Wordle - TCP & UDP");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> client.close());
        primaryStage.show();
    }

    // NOUA LOGICĂ: Verifică UDP, apoi trimite TCP (dacă e valid)
    private void handleGuess(String guess) {
        String validatedGuess = guess.trim().toUpperCase();

        if (validatedGuess.length() != 5) {
            validationLabel.setText("Eroare: Cuvântul trebuie să aibă exact 5 litere.");
            inputField.clear();
            return;
        }

        if (currentRow >= MAX_ATTEMPTS) {
            statusLabel.setText("Joc terminat.");
            inputField.setDisable(true);
            return;
        }

        // Dezactivăm input-ul în timp ce așteptăm răspunsul UDP/TCP
        inputField.setDisable(true);
        validationLabel.setText("Verificare dicționar...");

        // Rulăm verificarea UDP pe un alt thread
        new Thread(() -> {
            boolean isValid = client.checkWordValidityUDP(validatedGuess);

            Platform.runLater(() -> {
                if (isValid) {
                    // Cazul 1: Cuvânt VALID în dicționar (Continuăm cu TCP)
                    validationLabel.setText("");
                    grid[currentRow].setGuess(validatedGuess);
                    client.sendGuess(validatedGuess); // Trimitem TCP (consumă încercare)
                    // Input-ul rămâne dezactivat până la primirea răspunsului TCP
                } else {
                    // Cazul 2: Cuvânt INVALID (Nu consumă încercare)
                    validationLabel.setText("Cuvântul nu există în dicționar. Încearcă altă opțiune.");
                    inputField.setDisable(false); // Reactivează input-ul
                    inputField.clear();
                }
            });
        }).start();
    }

    // Metoda de Actualizare a GUI (Apelată de ClientThread după răspunsul TCP)
    public void updateGUI(String serverResponse) {
        Platform.runLater(() -> {

            // Afișează statusul general (ex: mesajul de bun venit)
            if (!serverResponse.startsWith("Feedback: ") && !serverResponse.startsWith("VICTORY")) {
                statusLabel.setText(serverResponse.trim());
            }

            if (serverResponse.startsWith("Feedback: ")) {

                // Logica de extragere corectă a feedback-ului
                String prefix = "Feedback: ";
                int startIndex = prefix.length();
                String feedbackCode = "";
                if (serverResponse.length() >= startIndex + 5) {
                    feedbackCode = serverResponse.substring(startIndex, startIndex + 5);
                }

                String guess = inputField.getText().trim().toUpperCase();

                if (currentRow < MAX_ATTEMPTS) {
                    grid[currentRow].updateRow(guess, feedbackCode);
                    currentRow++;
                }

                // Reactivează input-ul pentru următoarea ghicitoare
                if (feedbackCode.equals("22222")) {
                    statusLabel.setText("FELICITĂRI! Ai ghicit cuvântul!");
                    inputField.setDisable(true);
                } else if (currentRow >= MAX_ATTEMPTS) {
                    // Serverul ar trebui să trimită deja cuvântul secret la final
                    statusLabel.setText(serverResponse.trim());
                    inputField.setDisable(true);
                } else {
                    statusLabel.setText(serverResponse.trim());
                    inputField.setDisable(false);
                    inputField.clear();
                }

            } else if (serverResponse.startsWith("VICTORY")) {
                // Finalul jocului în caz de victorie
                if (currentRow < MAX_ATTEMPTS) {
                    String secret = serverResponse.substring(serverResponse.indexOf("Ai ghicit: ") + 11, serverResponse.indexOf(". Feedback")).trim();
                    grid[currentRow].updateRow(secret, "22222");
                    currentRow++;
                }
                statusLabel.setText(serverResponse.trim());
                inputField.setDisable(true);
            }
        });
    }
}