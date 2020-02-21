import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.io.FileReader;
import java.io.BufferedReader;

public class BasicHttpServerExample {
        public static void main(String[] args) throws IOException {
            HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
            HttpContext context = server.createContext("/");
            context.setHandler(BasicHttpServerExample::handleRequest);
            server.start();
        }

        private static void handleRequest(HttpExchange exchange) throws IOException {
            BufferedReader in = new BufferedReader(new FileReader("index.html"));
			String line;
            String response = "";
			while ((line = in.readLine()) != null)
			{
		      // Afficher le contenu du fichier
   			  response+= line;
			}
			in.close();
            exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
