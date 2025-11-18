package org.example;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;

public class GuessRow extends HBox {
    private Label[] boxes = new Label[5];

    public GuessRow() {
        this.setSpacing(5); // Spațiu între pătrățele
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(5));

        for (int i = 0; i < 5; i++) {
            boxes[i] = new Label("");
            boxes[i].setMinSize(50, 50); // Dimensiunea pătrățelului
            boxes[i].setAlignment(Pos.CENTER);

            // Stilul inițial (Margine gri deschis, font bold)
            boxes[i].setStyle("-fx-border-color: #d3d6da; -fx-font-size: 20; -fx-font-weight: bold;");
            boxes[i].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            this.getChildren().add(boxes[i]);
        }
    }

    /**
     * Actualizează vizual rândul pe baza cuvântului și a codului de feedback de la server (ex: "20102").
     * Serverul Python trimite: 2=Verde, 1=Galben, 0=Gri.
     */
    public void updateRow(String guess, String feedback) {
        for (int i = 0; i < 5; i++) {
            char code = feedback.charAt(i);
            Color color;

            switch (code) {
                case '2': // Litera și poziția corectă
                    color = Color.rgb(106, 170, 100); // Verde (cod Wordle)
                    break;
                case '1': // Litera corectă, poziție greșită
                    color = Color.rgb(201, 180, 88); // Galben (cod Wordle)
                    break;
                case '0': // Litera incorectă
                default:
                    color = Color.rgb(120, 124, 126); // Gri închis (cod Wordle)
                    break;
            }

            boxes[i].setText(String.valueOf(guess.charAt(i)));
            boxes[i].setTextFill(Color.WHITE); // Textul alb
            boxes[i].setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    /**
     * Afișează temporar literele pe rândul curent înainte de a primi feedback (fără a colora).
     */
    public void setGuess(String guess) {
        for (int i = 0; i < 5; i++) {
            // Setează litera sau șterge dacă nu există (pentru cuvinte mai scurte)
            boxes[i].setText(i < guess.length() ? String.valueOf(guess.charAt(i)).toUpperCase() : "");
            // Resetează la culoarea albă, dar cu margine vizibilă
            boxes[i].setTextFill(Color.BLACK);
            boxes[i].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            boxes[i].setStyle("-fx-border-color: #555555; -fx-font-size: 20; -fx-font-weight: bold;");
        }
    }
}