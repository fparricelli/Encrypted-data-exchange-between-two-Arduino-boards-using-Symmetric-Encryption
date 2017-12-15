Il File config.dat contiene gli URL al quale l'applicazione si connette per le operazioni di routine.
Tale file è stato inserito in questa cartella a puro scopo dimostrativo.
Vista l'impossibilità (e l'inutilità) di adottare una cifratura dello stesso (dal momento che l'applicazione deve essere
rilasciata agli utenti, dovremmo con essa rilasciare anche la chiave di de-cifratura necessaria all'applicazione) tale file
deve essere protetto utilizzando gli strumenti messi a disposizione, per esempio, dal sistema operativo.
Si può infatti pensare ad esempio che, in fase di installazione, si restringa l'accesso al file config.dat al solo 
utente/processo che usa/rappresenta l'applicazione, oppure fare in modo che, sempre in fase di installazione, si generi
una chiave randomica ad-hoc per quell'installazione che viene memorizzata in maniera 'sicura' dal sistema operativo, con
la quale possiamo cifrare il contenuto del file.
