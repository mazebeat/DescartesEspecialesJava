package cl.intelidata.main;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

/**
 * Clase que almacena Cadenas en un archivo de texto, para disminir el uso de 
 * memoria y acelera los procesamientos, utilizado para almacenar muchos datos.
 *  
 */
public class FileString {
	private File file;
	private BufferedWriter writer;
	private boolean isClosed;
	private long lineas;
	
	public FileString(long x) throws Exception{
		file=new File("FileString"+x+".tmp");
		if(file.isFile()){
			while(!file.delete()){				
			}
		}
		if(file.exists()){
			file.createNewFile();
		}
		file.deleteOnExit();
		isClosed=true;
		writer = new BufferedWriter(new FileWriter(file,true));
		lineas=0;
	}
	
	/**
	 * Valida si se ha Escrito en el Archivo.
	 * @return true si se ha escrito o False si no se ha escrito en el.
	 */
	public boolean isWriten(){
		if(lineas>0){
			return true;			
		}
		else{
			return false;			
		}
	}
	
	/**
	 * Elimina el archivo.
	 */
	public void deleteFile() throws Exception{
		if(!isClosed){
			closeFile();
		}
		file.delete();
	}
	
	/**
	 * Concatena la cadena ingresada al archivo.
	 * @param texto Texto que quiere concatenar.
	 */
	public void append(String texto) throws Exception{
		isClosed=false;
		writer.write(texto);
		lineas++;
	}
	
	/**
	 * Cierra el Buffer de texto.
	 */
	public void closeFile() throws Exception{
		writer.close();
		isClosed=true;
	}
	
	/**
	 * Obtiene el Archivo.
	 * @return El archivo donde se escribe la cadena. 
	 */
	public File getFile(){
		return file;		
	}
	
	/**
	 * Concatena el Archivo a otro ingresado por parametro.
	 * @param to el Archivo donde se agragara el texto. 
	 */
	public void addTo(File to) throws Exception{
		if(isClosed){
			InputStream in = new FileInputStream(file);
			BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream (to, true));
//			OutputStream out = new FileOutputStream(to);
			byte[] buf = new byte[1024];
			int len;		 
			while ((len = in.read(buf)) > 0) {
			  out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
