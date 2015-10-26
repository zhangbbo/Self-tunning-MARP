package effectuator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Effectuator {
	public static void effectuator(String policy, double num){
		String command = "sh ~/selfTuning-CapacityScheduler.sh " + policy + " " + num;
		Process process = null;
		
		try {
			process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", command});
			
			String er = null;
			BufferedReader error = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			while ((er = error.readLine()) != null) {
				System.out.println(er);
			}
			error.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
