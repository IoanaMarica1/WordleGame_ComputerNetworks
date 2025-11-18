# Imaginea de bază Linux cu Python 3.10
FROM python:3.10-slim

# Setează directorul de lucru în container
WORKDIR /usr/src/app

# Copiază și instalează dependențele (requirements.txt este gol, dar e pas standard)
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copiază codul serverului
COPY server_wordle.py .

# Declară portul TCP 8080 ca fiind deschis în container
EXPOSE 8080

# Comanda care rulează când pornește containerul
CMD [ "python", "./server_wordle.py" ]