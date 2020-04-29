import java.io.PrintWriter;
import java.io.File;
import javax.script.*;

public class createHtml{
	private static String nom;
	private static String age;
	public static void main(String[] args){
		if(args.length!=1){
			System.out.println("Usage : <parametre>");
			System.exit(0);
		}else{
			try{
				System.out.println("DEMARRAGE CREATION HTML");
				ScriptEngineManager manager=new ScriptEngineManager();
				ScriptEngine engine=manager.getEngineByName("JavaScript");
				createHtml result=new createHtml();
				String str=args[0].replace(";","\'; ");
				str=str.replace("=","=\'");
				str=str.replace("\"","");
				System.out.println("STR: "+str);
				String []tabCom=new String[2];
				for (String commande:str.split(" ")){
					System.out.println(commande);
					engine.eval(commande);
				}
				nom=engine.get("nom").toString();
				age=engine.get("age").toString();
				File file=new File("reponse.html");
				PrintWriter writer=new PrintWriter("reponse.html");
				writer.println("<!doctype html>");
				writer.println("<html lang='fr'>");
				writer.println("<head><meta charset='utf-8'><title>REPONSE</title></head>");
				writer.println("<body><p>Bonjour Mr "+nom+", vous avez "+age+" ans.</p></body></html>");
				writer.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		
		}
	
	}
}
