package cl.intelidata.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import main.LOG;

/**
 * App que descarta clientes para Fija y Movil
 * 
 * @author Maze
 */
public class DescartesEspeciales {
	private static Hashtable<String, String> descartes;
	private static Hashtable<String, String> pordescartar;
	private static Hashtable<String, String> rTransversal;
	private static LOG                       log;
	
	/**
	 * Main process
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		log = new LOG(DescartesEspeciales.class);
		try {
			args[0] = "C:\\Users\\Maze\\Google Drive\\Intelidata\\Gemini's\\488\\06\\OSI2014-22729_Not_SMS\\Test\\Input\\ELECTRONICOS_C200115_Muestras_1_100.dat";
			args[1] = "C:\\Users\\Maze\\Google Drive\\Intelidata\\Gemini's\\488\\06\\OSI2014-22729_Not_SMS\\Test\\Output\\BolTelChile_C0004_07062014.dat.result";
			args[2] = "C:\\Users\\Maze\\Google Drive\\Intelidata\\Gemini's\\488\\06\\OSI2014-22729_Not_SMS\\Test\\Input\\descartados.txt";
			args[3] = "M";
			args[4] = "C:\\Users\\Maze\\Google Drive\\Intelidata\\Gemini's\\488\\06\\OSI2014-22729_Not_SMS\\Test\\Input\\ENROLA_MOVIL_CIC_200115.SVD";
			args[5] = "C:\\Users\\Maze\\Google Drive\\Intelidata\\Gemini's\\488\\06\\OSI2014-22729_Not_SMS\\Test\\Output\\NUEVA_COLUMNA_REPORTE_TRANSVERSAL.csv";
			
			if (args.length != 6) {
				log.ERROR("Error en parametros ingresados.");
				System.exit(1);
			}
			log.INFO("Iniciando proceso.");
			valach(args);
			descartes = llenaDescartes(args[2]);
			if (descartes == null) {
				log.ERROR("No se encuentran descartes ESPECIALES.");
				System.exit(1);
			}
			if (args[3].equals("F")) {
				log.INFO("Procesando Fija.");
				pordescartar = buscarDescartesF(args[4]);
				if (pordescartar == null) {
					log.ERROR("No existen clientes por descartar del proceso.");
					System.exit(1);
				}
				descartarF(args[0], args[1]);
			} else if (args[3].equals("M")) {
				log.INFO("Procesando Movil.");
				pordescartar = buscarDescartesM(args[4]);
				if (pordescartar == null) {
					log.ERROR("No existen clientes por decartar del proceso.");
					System.exit(1);
				}
				descartarM(args[0], args[1]);
				csvWrite(args[5], getTestData(rTransversal), "|");
			} else {
				log.ERROR("Tipo de proceso " + args[3] + " no existe, ingrese F o M.");
				System.exit(1);
			}
		} catch (Exception ex) {
			log.ERROR("Error en proceso.");
			log.EXCEPCION(ex);
		}
	}
	
	/**
	 * Valida los parametros de entrada.
	 * 
	 * @param parametros String[] Todos los parametros de la consola
	 * @throws Exception
	 */
	public static void valach(String[] parametros) throws Exception {
		int error = 0;
		File arch = new File(parametros[0]);
		if (!arch.isFile()) {
			log.ERROR("Archivo Entrada " + parametros[0] + " no existe.");
			error++;
		}
		arch = new File(parametros[1]);
		if (arch.isFile()) {
			arch.delete();
		}
		if (!arch.createNewFile()) {
			log.ERROR("Archivo Salida " + parametros[1] + " no se puede crear.");
			error++;
		}
		arch = new File(parametros[2]);
		if (!arch.isFile()) {
			log.ERROR("Archivo Descartes " + parametros[2] + " no Existe.");
			error++;
		}
		if (error != 0) {
			log.ERROR("Error en parametros ingresados, uno o mas archivos no existen o no se pueden crear.");
			System.exit(error);
		}
	}
	
	/**
	 * Descarta los Clientes Fija B y F.
	 * 
	 * @param Archin String con ruta del archivo de Entrada.
	 * @param Archout String con ruta del Archivo de Salida.
	 */
	public static void descartarF(String Archin, String Archout) throws Exception {
		File in = new File(Archin);
		if (in.getName().toUpperCase().startsWith("NC")) {
			descartarFNC(Archin, Archout);
			return;
		}
		long Qdescartes = 0;
		long Qcli = 0;
		BufferedReader input = getBufferedReader(Archin);
		File output = new File(Archout);
		int index = Archout.lastIndexOf(".");
		File outputDescartes = new File(Archout.substring(0, index) + ".descartados");
		if (outputDescartes.isFile()) {
			outputDescartes.delete();
		}
		outputDescartes.createNewFile();
		String reg = "";
		FileString RegistroCli = new FileString(Qcli);
		String Codcli = "", Cuenta = "";
		while ((reg = input.readLine()) != null) {
			if (reg.startsWith("20100")) {
				Qcli++;
				if (RegistroCli.isWriten()) {
					if (pordescartar.containsKey(Codcli + "-" + Cuenta)) {
						log.INFO("Descartado: " + Codcli + "-" + Cuenta);
						Qdescartes++;
						RegistroCli.closeFile();
						RegistroCli.addTo(outputDescartes);
					} else {
						log.INFO("No Descartado: " + Codcli + "-" + Cuenta);
						RegistroCli.closeFile();
						RegistroCli.addTo(output);
					}
					RegistroCli.deleteFile();
					RegistroCli = new FileString(Qcli);
				}
				Codcli = reg.split("\\|")[3];
			} else if (reg.startsWith("30100")) {
				Cuenta = getCorrigeCliente(reg.split("\\|")[4]);
			}
			RegistroCli.append(reg + "\n");
		}
		if (pordescartar.containsKey(Codcli + "-" + Cuenta)) {
			log.INFO("Descartado: " + Codcli + "-" + Cuenta);
			Qdescartes++;
			RegistroCli.closeFile();
			RegistroCli.addTo(outputDescartes);
		} else {
			log.INFO("No Descartado: " + Codcli + "-" + Cuenta);
			RegistroCli.closeFile();
			RegistroCli.addTo(output);
		}
		RegistroCli.deleteFile();
		RegistroCli = new FileString(Qcli);
		log.INFO("Se descartaron " + Qdescartes + " clientes del proceso.");
		log.INFO("Proceso Terminado.");
	}
	
	/**
	 * Descarta los Clientes Fija NC.
	 * 
	 * @param Archin String con ruta del archivo de Entrada.
	 * @param Archout String con ruta del Archivo de Salida.
	 */
	public static void descartarFNC(String Archin, String Archout) throws Exception {
		long Qdescartes = 0;
		long Qcli = 0;
		BufferedReader input = getBufferedReader(Archin);
		File output = new File(Archout);
		int index = Archout.lastIndexOf(".");
		File outputDescartes = new File(Archout.substring(0, index) + ".descartados");
		if (outputDescartes.isFile()) {
			outputDescartes.delete();
		}
		outputDescartes.createNewFile();
		String reg = "";
		FileString RegistroCli = new FileString(Qcli);
		String Codcli = "", Cuenta = "";
		while ((reg = input.readLine()) != null) {
			if (reg.startsWith("20100")) {
				Qcli++;
				if (RegistroCli.isWriten()) {
					if (pordescartar.containsKey(Codcli + "-" + Cuenta)) {
						log.INFO("Descartado: " + Codcli + "-" + Cuenta);
						Qdescartes++;
						RegistroCli.closeFile();
						RegistroCli.addTo(outputDescartes);
					} else {
						log.INFO("No Descartado: " + Codcli + "-" + Cuenta);
						RegistroCli.closeFile();
						RegistroCli.addTo(output);
					}
					RegistroCli.deleteFile();
					RegistroCli = new FileString(Qcli);
				}
				Codcli = reg.split("\\|")[1];
				Cuenta = reg.split("\\|")[2];
			}
			RegistroCli.append(reg + "\n");
		}
		if (pordescartar.containsKey(Codcli + "-" + Cuenta)) {
			log.INFO("Descartado: " + Codcli + "-" + Cuenta);
			Qdescartes++;
			RegistroCli.closeFile();
			RegistroCli.addTo(outputDescartes);
		} else {
			log.INFO("No Descartado: " + Codcli + "-" + Cuenta);
			RegistroCli.closeFile();
			RegistroCli.addTo(output);
		}
		// RegistroCli.deleteFile();
		RegistroCli = new FileString(Qcli);
		RegistroCli.deleteFile();
		log.INFO("Se descartaron " + Qdescartes + " clientes del proceso.");
		log.INFO("Proceso Terminado.");
	}
	
	/**
	 * Descarta los Clientes Movil.
	 * 
	 * @param Archin String con ruta del archivo de Entrada.
	 * @param Archout String con ruta del Archivo de Salida.
	 */
	public static void descartarM(String Archin, String Archout) throws Exception {
		long Qdescartes = 0;
		long Qcli = 0;
		BufferedReader input = getBufferedReader(Archin);
		File output = new File(Archout);
		int index = Archout.lastIndexOf(".");
		File outputDescartes = new File(Archout.substring(0, index) + ".descartados");
		if (outputDescartes.isFile()) {
			outputDescartes.delete();
		}
		outputDescartes.createNewFile();
		String reg = "";
		FileString RegistroCli = new FileString(Qcli);
		String Codcli = "";
		rTransversal = new Hashtable<String, String>();
		rTransversal.put("FA", "0");
		rTransversal.put("BO", "0");
		rTransversal.put("AP", "0");
		while ((reg = input.readLine()) != null) {
			if (reg.startsWith("A1000")) {
				String codigo = reg.split(" ")[1];
				Qcli++;
				if (RegistroCli.isWriten()) {
					if (pordescartar.containsKey(Codcli)) {
						log.INFO("Descartado: " + Codcli);
						Qdescartes++;
						setReporteTransversal(codigo, Long.toString(Qdescartes));
						RegistroCli.closeFile();
						RegistroCli.addTo(outputDescartes);
					} else {
						log.INFO("No Descartado: " + Codcli);
						RegistroCli.closeFile();
						RegistroCli.addTo(output);
					}
					RegistroCli.deleteFile();
					RegistroCli = new FileString(Qcli);
				}
			} else if (reg.startsWith("A1300")) {
				Codcli = getCorrigeCliente(reg.substring(25, 35));
			}
			RegistroCli.append(reg + "\n");
		}
		if (pordescartar.containsKey(Codcli)) {
			log.INFO("Descartado: " + Codcli);
			Qdescartes++;
			RegistroCli.closeFile();
			RegistroCli.addTo(outputDescartes);
		} else {
			log.INFO("No Descartado: " + Codcli);
			RegistroCli.closeFile();
			RegistroCli.addTo(output);
		}
		RegistroCli.deleteFile();
		RegistroCli = new FileString(Qcli);
		log.INFO("Se descartaron " + Qdescartes + " clientes del proceso.");
		log.INFO("Proceso Terminado.");
	}
	
	/**
	 * Obtiene las cuentas de los clientes del archivo de enrolamiento que tengan correos del archivo de descarte (Fijo).
	 * 
	 * @param Archin String con ruta del archivo de Entrada.
	 * @return Hash con valores si se encuentran clientes, o Hash null.
	 */
	public static Hashtable<String, String> buscarDescartesF(String Archin) throws Exception {
		Hashtable<String, String> salida = new Hashtable<String, String>();
		String linea = "";
		BufferedReader leer = getBufferedReader(Archin);
		int contadescartes = 0;
		while ((linea = leer.readLine()) != null) {
			String[] data = new String[3];
			data[0] = linea.substring(0, 9).trim();
			data[1] = linea.substring(9, 18).trim();
			data[2] = linea.substring(18, linea.length() - 1).trim();
			if (descartes.containsKey(data[2].trim())) {
				if (!salida.containsKey(data[0] + "-" + data[1])) {
					salida.put(data[0] + "-" + data[1], "");
					contadescartes++;
				} else {
					contadescartes++;
				}
			}
		}
		if (salida.size() == 0) {
			salida = null;
		} else {
			log.INFO("Se descartaran " + contadescartes + " clientes.");
		}
		return salida;
	}
	
	/**
	 * Obtiene las cuentas de los clientes del archivo de enrolamiento que tengan correos del archivo de descarte (Movil).
	 * 
	 * @param Archin String con ruta del archivo de Entrada.
	 * @return Hash con valores si se encuentran clientes, o Hash null.
	 */
	public static Hashtable<String, String> buscarDescartesM(String Archin) throws Exception {
		Hashtable<String, String> salida = new Hashtable<String, String>();
		String linea = "";
		BufferedReader leer = getBufferedReader(Archin);
		int contadescartes = 0;
		while ((linea = leer.readLine()) != null) {
			String[] data = linea.split(";");
			if (descartes.containsKey(data[1])) {
				if (!salida.containsKey(getCorrigeCliente(data[0]))) {
					salida.put(getCorrigeCliente(data[0]), "");
					contadescartes++;
				} else {
					contadescartes++;
				}
			}
		}
		if (salida.size() == 0) {
			salida = null;
		} else {
			log.INFO("Se descartaran " + contadescartes + " clientes.");
		}
		return salida;
	}
	
	/**
	 * Obtiene los correos del archivo de descartes.
	 * 
	 * @param Archin String con ruta del archivo de Entrada.
	 * @return Hash con valores si se encuentran clientes, o Hash null.
	 */
	public static Hashtable<String, String> llenaDescartes(String Archin) throws Exception {
		Hashtable<String, String> salida = new Hashtable<String, String>();
		String linea = "";
		BufferedReader leer = getBufferedReader(Archin);
		log.INFO("LLenando lista de descartes:");
		while ((linea = leer.readLine()) != null) {
			salida.put(linea, "");
			log.INFO(linea);
		}
		if (salida.size() == 0) {
			salida = null;
		}
		log.INFO("Terminado, lista ok.");
		return salida;
	}
	
	/**
	 * Instancia un BufferedReader con codificacion "ISO-8859-1".
	 * 
	 * @param File Ruta del archivo de Entrada.
	 * @return BufferedReader del archivo Archin.
	 */
	public static BufferedReader getBufferedReader(String File) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(File), "ISO-8859-1"));
		return br;
	}
	
	/**
	 * Corrige el codigo cliente eliminando los 0 a la izquierda.
	 * 
	 * @param cliente String con codigo cliente.
	 * @return String con codigo cliente corregido.
	 */
	public static String getCorrigeCliente(String cliente) {
		int cod = -1;
		try {
			cod = Integer.parseInt(cliente);
			return String.valueOf(cod);
		} catch (Exception ex) {}
		return cliente.trim();
	}
	
	/**
	 * Setea valores del hashtable correspondiente a la nueva columna que se debe agregar al reporte transversal Gemini 558 / OSI-23272
	 * 
	 * @param key
	 * @param value
	 */
	public static void setReporteTransversal(String key, String value) {
		String name = getNameCode(key);
		rTransversal.put(name, value);
	}
	
	/**
	 * Hace el match entre el codigo recibido del archivo de movil al nombre correspondiente al reporte transversal
	 * 
	 * @param code
	 * @return
	 */
	public static String getNameCode(String code) {
		String name = "";
		switch (code) {
			case "23":
				name = "FA";
				break;
			case "53":
				name = "AP";
				break;
			case "54":
				name = "BO";
				break;
			case "72":
				name = "NC";
				break;
		}
		
		return name;
	}
	
	/**
	 * Convierte y genera data necesaria para crear archivo csv de salida
	 * 
	 * @param Hashtable <String, String> hashtable
	 * @return ArrayList<String[]>
	 */
	public static ArrayList<String[]> getTestData(Hashtable<String, String> hashtable) {
		List<String[]> data = new ArrayList<String[]>();
		int total = 0;
		
		data.add(new String[] { "CONCEPTO", "TOTAL DESCARTADOS" });
		for (String key : hashtable.keySet()) {
			total = total + Integer.parseInt(hashtable.get(key));
			data.add(new String[] { key, hashtable.get(key) });
		}
		data.add(new String[] { "TOTAL", Integer.toString(total) });
		return (ArrayList<String[]>) data;
	}
	
	/**
	 * Genera archivo de salida args[5] que se utilizará para modificar el archivo de reporte transversal
	 * 
	 * @param file
	 * @param data
	 * @param delimiter
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void csvWrite(String file, ArrayList<String[]> data, String delimiter) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		
		OutputStream fout = new FileOutputStream(file);
		OutputStreamWriter out = new OutputStreamWriter(fout, "UTF8");
		
		for (int i = 0; i < data.size(); i++) {
			
			String[] fila = data.get(i);
			
			for (int j = 0; j < fila.length; j++) {
				out.write(fila[j] + delimiter);
			}
			
			out.write("\n");
		}
		out.close();
		fout.close();
	}
	
}
