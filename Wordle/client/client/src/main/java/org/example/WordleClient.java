package org.example;

import java.io.*;
import java.net.*;

public class WordleClient {
    private static final String SERVER_HOST = "172.23.144.1";
    private static final int TCP_PORT = 8080;
    private static final int UDP_PORT = 8081;
    private static final int TIMEOUT_MS = 1000; //asteptare raspuns

    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private InetAddress serverAddress;

    private PrintWriter out;
    private BufferedReader in;//flux intrare iesire tcp

    public WordleClient() throws IOException {
        //1. Inițializare TCP
        tcpSocket = new Socket(SERVER_HOST, TCP_PORT);
        out = new PrintWriter(tcpSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

        //2. Inițializare UDP
        serverAddress = InetAddress.getByName(SERVER_HOST);
        udpSocket = new DatagramSocket();//socket local trimitere primire
        udpSocket.setSoTimeout(TIMEOUT_MS);
    }

    public void sendGuess(String guess) {
        if (out != null) {
            out.println(guess);
        }
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }


    public boolean checkWordValidityUDP(String word) {
        String command = "CHECK_WORD:" + word;
        byte[] sendData = command.getBytes();
        byte[] receiveData = new byte[1024];

        try {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, UDP_PORT);
            udpSocket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();

            return response.equals("WORD_VALID:TRUE");

        } catch (SocketTimeoutException e) {
            // Dacă UDP dă timeout, presupunem că nu este valid sau arătăm eroare
            System.err.println("Timeout UDP la verificare cuvant. Considerat invalid.");
            return false; // Mai sigur să nu validăm
        } catch (IOException e) {
            System.err.println("Eroare UDP la verificare cuvant.");
            return false;
        }
    }
    //inchidem conexiuni
    public void close() {
        try {
            if (tcpSocket != null && !tcpSocket.isClosed()) {
                tcpSocket.close();
            }
            if (udpSocket != null) {
                udpSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Eroare la închiderea clientului: " + e.getMessage());
        }
    }
}