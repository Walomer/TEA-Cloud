import java.net.*;
import javax.net.ssl.*;
import java.io.*;
import java.util.*;
import java.lang.*;

//La classe Client implémente l'interface runnable afin d'etre executer dans un thread.
//Sa méthode run permet d'attendre une connexion de la part d'un client puis lance le traitement  de la requête reçu par celui-ci.
//La majorité des fonctions sont synchronisés afin qu'un client ne prenne pas le dessus sur un autre, cela permet à chaque client d'avoir le même temps consacré à son traitement.

class Client implements Runnable {
    private ServerSocket socketserver;
    private Socket sck;
    static int nbSessions = 1; // comptage du nombre de sessions   
    static String serverLine = "Server: Simple Serveur de TP ULR"; // chaines de caracteres formant la reponse HTTP     
    static String statusLine = null;
    static String contentTypeLine = null;
    static String entityBody = null;
    static String contentLengthLine = null;
    static final int DEBUG = 255; // constante a positionner pour controler le niveau d'impressions // de controle (utilisee dans la methode debug(s,n)


    public Client(ServerSocket s) {
        socketserver = s;
    }

    public void run() {
        DataOutputStream os = null;
        BufferedReader br = null;
        try {
            System.out.println("Le client numéro " + nbSessions + " est connecté !");
            while (true) {
                sck = socketserver.accept(); // Un client se connecte on l'accepte
                os = new DataOutputStream(sck.getOutputStream());
                br = new BufferedReader(new InputStreamReader(sck.getInputStream()));
                processRequest(br, os);
                sck.close();
            }
        } catch (IOException e) {
            System.out.println("ERREUR IO" + e);
            System.out.println("ARRET DU SERVEUR");
        }
    }


    public static synchronized String readLine(String p, BufferedReader br) throws IOException {
        String s;
        s = br.readLine();
        return s;
    }

    public static synchronized void processRequest(BufferedReader br, DataOutputStream dos) throws IOException {
        /*       Cette methode lit des lignes sur br (utiliser readLine) et recherche une ligne commencant par GET ou par POST.
        Si la ligne commence par GET,          
        	- on extrait le nom de fichier demande dans la ligne et on appelle la methode retourFichier.          
        	- Si le suffixe du nom de fichier est .htm ou .html (utiliser la methode contentType)         
        	- on lit ensuite toutes les lignes qui suivent jusqu'à en trouver une vide, nulle ou contenant juste "\n\r"         
        */
        String nom = new String();
        String texte = "";
        String ligne = readLine(nom, br);
        String nomFichier = new String();
        boolean requetGET = false;
        boolean requetPOST = false;

        //SI METHODE GET
        if (ligne.contains("GET")) {
            String ligneSplit[] = ligne.split(" ");
            for (String mot: ligneSplit) {
                if (new File(mot).exists()) {
                    nomFichier = mot;
                    retourFichier(nomFichier, dos);
                    contentTypeLine = "other";
                }
                if (contentType(nomFichier).equals("text/html")) {
                    contentTypeLine = "Content-Type: text/html";
                    requetGET = true;
                }
            }

            //SI METHODE POST
        } else if (ligne.contains("POST")) {
            String ligneSplit[] = ligne.split(" ");
            for (String mot: ligneSplit) {
                if (new File(mot).exists()) {
                    nomFichier = mot;
                    contentTypeLine = "script";
                    requetPOST = true;
                }
                if (contentType(mot).equals("text/html")) {
                    contentTypeLine = "Content-Type: text/html";
                    requetPOST = false;
                    retourFichier(nomFichier, dos);
                }
            }
        }
        //Si méthode POST et suffixe différent de .htm ou .html
        if (requetPOST && !requetGET) {
            retourCGIPOST(nomFichier, br, dos);
        }
        //Si méthode GET et suffixe égale à .html ou .htm
        while ((requetGET && !requetPOST) && !ligne.contains("\n\r") && ligne != null && !ligne.isEmpty()) {
            ligne = readLine(nom, br);
            texte = texte + ligne;
        }

        /*
        Si la ligne commence par POST         
        	- on extrait le nom de fichier demande dans la ligne et on appelle la methode retourFichier.          
        	- Si le suffixe du nom de fichier est .htm ou .html, on fait la meme chose que ci-dessus pour GET         
        	- Si le suffixe est autre, on appelle la methode retourCGIPOST       */
    }
    private static synchronized void retourFichier(String f, DataOutputStream dos) throws IOException {
        File file = new File(f);
        if (file.exists() && !file.isDirectory()) {
            // ENTETE
            statusLine = "HTTP/1.1 200 OK";
            contentLengthLine = "Content-Length: " + file.length() + "";
            FileInputStream fileInStream = new FileInputStream(file);
            entete(dos);
            //FICHIER
            envoiFichier(fileInStream, dos);
            dos.flush();
        } else {
            statusLine = "Error, " + f + " not found";
            contentTypeLine = "unknown format";
            entete(dos);
        }
        /*       
        - Si le fichier existe, on prepare les infos status, retourFichiercontentType, contentLineLength qui conviennent on les envoit, et on envoit le fichier (methode envoiFichier)      
        - Si le fichier n'existe pas on prepare les infos qui conviennent et on les envoit
        */
    } //retourFichier


    private static synchronized void envoiFichier(FileInputStream fis, DataOutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
        envoi("\r\n", os);
    } // envoiFichier 


    private static synchronized String executer(String f) throws IOException {
        String R = "";
        try {
            Process proc = Runtime.getRuntime().exec("java -jar " + f);
            InputStream stream = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader buffer = new BufferedReader(isr);
            while ((R = buffer.readLine()) != null) {
                System.out.println(R);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*    Lance l'execution de la commande "f", et lit toutes les lignes    qui lui sont retournees par l'execution de cette commande. On    lit ligne ‡ ligne jusqu'a avoir une valeur de chaine null.    Toutes ces lignes sont accumulees dans une chaine qui    est retournee en fin d'execution.     */
        return R;
    } // executerprivate



    static synchronized void retourCGIPOST(String f, BufferedReader br, DataOutputStream dos) throws IOException {
        String tmp = "CGIPOST";
        String ligne = readLine(tmp, br);
        int nbCaractere = 0;
        String texte = "";

        //On cherche la ligne comment par Content-Length
        while (!ligne.startsWith("Content-Length")) {
            ligne = readLine(tmp, br);
        }
        //On récupère le nombre de caractère à lire
        if (ligne.startsWith("Content-Length")) {
            String nombre = ligne.replace("Content-Length: ", "");
            nbCaractere = Integer.parseInt(nombre);
        }
        //On cherche la ligne vide avant les données
        while (ligne.length() > 0) {
            ligne = readLine("cherche vide", br);
        }
        String param = "";
        char buff[] = new char[nbCaractere];
        //On lit les carctères formant les paramètres à envoyer
        br.read(buff, 0, nbCaractere);
        for (char c: buff) {
            if (c != '&') {
                param = param + c;
            } else {
                param = param + ";";
            }

        }
        //On forme la chaine à executer
        param = param + ";";
        f = f + " \"" + param + "\"";
        texte = executer(f);
        //On récupère le fichier .html puis, s'il existe, on l'envoie au client.
        File file = new File("reponse.html");
        if (file.exists() && !file.isDirectory()) {
            statusLine = "HTTP/1.1 200 OK";
            contentLengthLine = "Content-Length: " + file.length() + "";
            entete(dos);
            FileInputStream fileInStream = new FileInputStream(file);
            envoiFichier(fileInStream, dos);
        }

        //envoi(texte,dos);
        dos.flush();


        /*    On lit toutes les lignes jusqu'a trouver une ligne commencant par Content-Length    Lorsque cette ligne est trouvee, on extrait le nombre qui suit(nombre    de caracteres a lire).    On lit une ligne vide    On lit les caracteres dont le nombre a ete trouve ci-dessus    on les range dans une chaine,     On appelle la methode 'executer' en lui donnant comme parametre une chaine qui est la concatenation du nom de fichier, d'un espace    et de la chaine de parametres.    'executer' retourne une chaine qui est la reponse ‡ renvoyer     au client, apres avoir envoye les infos status, contentTypeLine, ....     */
    }


    private static synchronized void envoi(String m, DataOutputStream dos) throws IOException {
        dos.write(m.getBytes());
    } //envoi     
    private static void entete(DataOutputStream dos) throws IOException {
        envoi(statusLine + "\r\n", dos);
        envoi(serverLine + "\r\n", dos);
        envoi(contentTypeLine + "\r\n", dos);
        envoi(contentLengthLine + "\r\n", dos);
        envoi("\r\n", dos);
    } // entete     
    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        return "";
    } // contentType

}