package cl.intelidata.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

import main.LOG;

/**
 * App que descarta clientes para Fija y Movil
 */
public class DescartesEspeciales {
	private static Hashtable<String, String> descartes;
	private static Hashtable<String, String> pordescartar;
	private static LOG log;
	private static DescartesEspeciales desc = new DescartesEspeciales();

	public static void main(String[] args) throws Exception {
		desc.log = new LOG(DescartesEspeciales.class);
		try {
			if (args.length != 5) {
				log.ERROR("Error en parametros ingresados.");
				System.exit(1);
			}
			log.INFO("Inciando proceso.");
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
	 * @param parametros
	 *            Array de String con todos los parametros de la consola
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
	 * @param Archin
	 *            String con ruta del archivo de Entrada.
	 * @param Archout
	 *            String con ruta del Archivo de Salida.
	 */
	public static void descartarF(String Archin, String Archout) throws Exception {
		File in=new File(Archin);
		if(in.getName().toUpperCase().startsWith("NC")){
			descartarFNC(Archin,Archout);
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
		// String RegistroCli="",Codcli="",Cuenta="";
		String reg = "";
		boolean write = false;
		// while((reg=input.readLine())!=null){
		// if(reg.startsWith("20100")){
		// RegistroCli=reg+"\n";
		// write=false;
		// }
		// else if(reg.startsWith("30100")){
		// RegistroCli=RegistroCli+reg+"\n";
		// Cuenta=reg.split("\\|")[4];
		// if(pordescartar.containsKey(Codcli+"-"+Cuenta)){
		// Qdescartes++;
		// Escribe(outputDescartes,RegistroCli);
		// write=true;
		// RegistroCli="";
		// }
		// else{
		// Escribe(output,RegistroCli);
		// RegistroCli="";
		// }
		// }
		// else{
		// if(write){
		// Escribe(outputDescartes,reg+"\n");
		// }
		// else{
		// if(!RegistroCli.equals("")){
		// RegistroCli=RegistroCli+reg+"\n";
		// }
		// else{
		// Escribe(output,reg+"\n");
		// }
		// }
		// }
		// }
		FileString RegistroCli = new FileString(Qcli);
		String Codcli = "", Cuenta = "";
		while ((reg = input.readLine()) != null) {
			if (reg.startsWith("20100")) {
				Qcli++;
				Codcli = reg.split("\\|")[3];
				// Codcli=getCorrigeCliente(reg.substring(25,35));
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
	 * @param Archin
	 *            String con ruta del archivo de Entrada.
	 * @param Archout
	 *            String con ruta del Archivo de Salida.
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
		boolean write = false;
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
		RegistroCli.deleteFile();
		RegistroCli = new FileString(Qcli);
		log.INFO("Se descartaron " + Qdescartes + " clientes del proceso.");
		log.INFO("Proceso Terminado.");
	}
	/**
	 * Descarta los Clientes Movil.
	 * 
	 * @param Archin
	 *            String con ruta del archivo de Entrada.
	 * @param Archout
	 *            String con ruta del Archivo de Salida.
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
		// String RegistroCli="",Codcli="";
		String reg = "";
		boolean write = false;
		// while((reg=input.readLine())!=null){
		// if(reg.startsWith("A1000")){
		// RegistroCli=reg+"\n";
		// write=false;
		// }
		// else if(reg.startsWith("A1300")){
		// RegistroCli=RegistroCli+reg+"\n";
		// Codcli=getCorrigeCliente(reg.substring(25,35));
		// if(pordescartar.containsKey(Codcli)){
		// Qdescartes++;
		// Escribe(outputDescartes,RegistroCli);
		// write=true;
		// RegistroCli="";
		// }
		// else{
		// Escribe(output,RegistroCli);
		// RegistroCli="";
		// }
		// }
		// else{
		// if(write){
		// Escribe(outputDescartes,reg+"\n");
		// }
		// else{
		// if(!RegistroCli.equals("")){
		// RegistroCli=RegistroCli+reg+"\n";
		// }
		// else{
		// Escribe(output,reg+"\n");
		// }
		// }
		// }
		// }
		FileString RegistroCli = new FileString(Qcli);
		String Codcli = "";
		while ((reg = input.readLine()) != null) {
			if (reg.startsWith("A1000")) {
				Qcli++;
				if (RegistroCli.isWriten()) {
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
	 * Obtiene las cuentas de los clientes del archivo de enrolamiento que
	 * tengan correos del archivo de descarte (Fijo).
	 * 
	 * @param Archin
	 *            String con ruta del archivo de Entrada.
	 * @return Hash con valores si se encuentran clientes, o Hash null.
	 */
	public static Hashtable<String, String> buscarDescartesF(String Archin) throws Exception {
		Hashtable<String, String> salida = new Hashtable<String, String>();
		String linea = "";
		BufferedReader leer = getBufferedReader(Archin);
		int contadescartes = 0;
		while ((linea = leer.readLine()) != null) {
			String[] data = new String[3];
			data[0] = linea.substring(0, 9);
			data[1] = linea.substring(10, 18);
			data[2] = linea.substring(18, linea.length() - 1);
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
	 * Obtiene las cuentas de los clientes del archivo de enrolamiento que
	 * tengan correos del archivo de descarte (Movil).
	 * 
	 * @param Archin
	 *            String con ruta del archivo de Entrada.
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
	 * @param Archin
	 *            String con ruta del archivo de Entrada.
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
	 * @param File
	 *            Ruta del archivo de Entrada.
	 * @return BufferedReader del archivo Archin.
	 */
	public static BufferedReader getBufferedReader(String File) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(File), "ISO-8859-1"));
		return br;
	}

	/**
	 * Corrige el codigo cliente eliminando los 0 a la izquierda.
	 * 
	 * @param cliente
	 *            String con codigo cliente.
	 * @return String con codigo cliente corregido.
	 */
	public static String getCorrigeCliente(String cliente) {
		int cod = -1;
		try {
			cod = Integer.parseInt(cliente);
			return String.valueOf(cod);
		} catch (Exception ex) {
		}
		return cliente.trim();
	}
}
