import socket
import threading
import random
import time

# Configurațiile Serverului
HOST = '0.0.0.0'
TCP_PORT = 8080
UDP_PORT = 8081

#Citirea Dicționarului
def load_word_list(filename="words.txt"):
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            words = {line.strip().upper() for line in f if len(line.strip()) == 5 and line.strip().isalpha()}
            return words
    except FileNotFoundError:
        print(f"[EROARE] Fisierul '{filename}' nu a fost gasit.")
        return set()
WORD_LIST = load_word_list()
if not WORD_LIST:
    print("Nu s-au putut incarca cuvinte.")
    exit()

SECRET_WORD = random.choice(list(WORD_LIST))
ATTEMPTS_LEFT = 6


# Logica Wordle
def wordle_feedback(guess, secret):
    feedback = [0] * len(secret)
    secret_list = list(secret)
    #Identifică literele verzi (2)
    for i in range(len(secret)):
        if guess[i] == secret[i]:
            feedback[i] = 2
            secret_list[i] = None
            #Identifică literele galbene (1)
    for i in range(len(secret)):
        if feedback[i] == 0:
            if guess[i] in secret_list and guess[i] is not None:
                try:
                    index_in_secret = secret_list.index(guess[i])
                    feedback[i] = 1
                    secret_list[index_in_secret] = None
                except ValueError:
                    pass

    return "".join(map(str, feedback))


#Funcția Handler TCP
def client_handler(conn, addr):
    #Handler pentru TCP: Primesc ghicitoare validată, dau feedback și scad încercarea.
    global ATTEMPTS_LEFT
    print(f"\n[INFO-TCP] Thread nou pornit pentru clientul: {addr}")
    ATTEMPTS_LEFT = 6
    try:
        conn.sendall(
            f"Bun venit! Cuvantul secret are {len(SECRET_WORD)} litere. Incercari: {ATTEMPTS_LEFT}\n".encode('utf-8'))
        while True:
            data = conn.recv(1024)
            if not data:
                break
            guess = data.decode('utf-8').strip().upper()

            #Presupunem ca ghicitoarea primita prin TCP este deja validata de client via UDP.

            if ATTEMPTS_LEFT <= 0:
                response = f"Joc terminat. Cuvântul era {SECRET_WORD}.\n"
                conn.sendall(response.encode('utf-8'))
                break

            # Logica jocului - scadere nr incercari
            feedback_code = wordle_feedback(guess, SECRET_WORD)
            ATTEMPTS_LEFT -= 1

            if feedback_code == "22222":
                response = f"VICTORY! Ai ghicit: {SECRET_WORD}. Feedback: {feedback_code}\n"
                conn.sendall(response.encode('utf-8'))
                break
            else:
                response = f"Feedback: {feedback_code} (Încercări rămase: {ATTEMPTS_LEFT})\n"

            conn.sendall(response.encode('utf-8'))

    except Exception as e:
        print(f"[EROARE-TCP] Eroare cu clientul {addr}: {e}")
    finally:
        conn.close()
        print(f"[INFO-TCP] Conexiune cu {addr} închisă.")


#Funcția pentru Serverul UDP
def start_udp_server():
    """ Handler pentru UDP. """
    try:
        udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        udp_socket.bind((HOST, UDP_PORT))

        while True:
            data, addr = udp_socket.recvfrom(1024)
            message = data.decode('utf-8').strip().upper()

            # Răspunde la cererea de verificare a cuvântului (Noul rol al UDP)
            if message.startswith("CHECK_WORD:"):
                word_to_check = message[len("CHECK_WORD:"):].strip()

                # Verifică rapid existența în set
                if word_to_check in WORD_LIST:
                    response = "WORD_VALID:TRUE"
                else:
                    response = "WORD_VALID:FALSE"
            else:
                response = "UDP command not recognized."

            udp_socket.sendto(response.encode('utf-8'), addr)

    except Exception as e:
        print(f"[EROARE-UDP] Eroare server UDP: {e}")


#Funcția Main
def start_servers():
    print(f"[INFO] Server Wordle pornit. Ascultă pe TCP {HOST}:{TCP_PORT} si UDP {HOST}:{UDP_PORT}")
    print(f"[INFO] Cuvantul secret ales din {len(WORD_LIST)} cuvinte este: {SECRET_WORD}")

    udp_thread = threading.Thread(target=start_udp_server)
    udp_thread.start()

    start_tcp_server()


def start_tcp_server():
    """ Inițializează și rulează serverul TCP principal """
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, TCP_PORT))
        s.listen(5)

        while True:
            conn, addr = s.accept()
            client_thread = threading.Thread(target=client_handler, args=(conn, addr))
            client_thread.start()


if __name__ == '__main__':
    start_servers()