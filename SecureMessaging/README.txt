Istruzioni per l'avvio e il corretto funzionamento

0) IMPORTANTE: Assicurarsi di avere installato sul proprio server Tomcat la web app CertificateServer, necessaria per il download dei certificati (localhost, porta 8080).

1) Importare il progetto SecureMessaging nell'ambiente Eclipse.
2) Compilare il progetto.
	 
	 2.1) Nota: La cartella certificates\ClientSender\receiverCertificate è la cartella in cui il mittente scarica il certificato del ricevente.
	 E' inizialmente vuota in modo che al primo avvio i certificati vengono scaricati direttamente dal server.
	 Lo stesso discorso vale per la cartella certificates\\lientReceiver\senderCertificate (lato ricevente, però).

3) Per avviare l'applicazione, c'è bisogno di avviare i due main: MainSender.java e MainReceiver.java.
   Avviare uno dei due da riga di comando, l'altro da Eclipse (o come si preferisce).
   
   3.1) Per avviare velocemente da riga di comando: avviare il main da eclipse, poi stopparlo, e andare nella prospettiva debug.
   Una volta nella prospettiva debug, tasto destro>Properties sull'ultima esecuzione effettuata (terminated), nel riquadro command line copiare tutto il testo (tranne la prima riga).
   Dopodichè aprire il cmd, posizionarsi nel path del progetto (...\SecureMessaging), e scrivere java; poi incollare nella shell quanto era stato copiato precedentemente e dare invio.
   
4) Il primo main da avviare è MainReceiver.java, successivamente si avvia MainSender.java.
5) Dalla riga di comando ed (eventualmente) dalla console Eclipse viene mostrato a video l'avanzamento; al termine, le cartelle sopra citate conterranno i certificati scaricati dal server.
   