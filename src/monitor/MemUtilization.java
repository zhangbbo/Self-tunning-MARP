package monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MemUtilization extends Thread {
	public void run(){
		Process process = null;

		while (currentThread().isAlive()){
			try
			{
				process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "sh ~/selfTuning-mem-controlNode.sh" });

				String er = null;
				BufferedReader error = new BufferedReader(new InputStreamReader(
						process.getErrorStream()));
				while ((er = error.readLine()) != null) {
					System.out.println(er);
				}
				error.close();
				
				Thread.sleep(1001);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
