package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jama.Matrix;
import jkalman.JKalman;
import effectuator.Effectuator;
import monitor.ControlNodeMonitor;
import monitor.MemUtilization;

public class Controller extends Thread {

	private static int sleeptime = 1001;

	private static final float memMaxThreshold = (float) 0.95;
	private static final float memMinThreshold = (float) 0.9;
	
	private static String backup = null;

	public static void main(String[] args)
	{
		new Controller().start();
	}

	public void run(){

		//launch controlnode monitors
		Thread cnm = new ControlNodeMonitor();
		Thread mu = new MemUtilization();

		cnm.start();
		mu.start();

		Process process = null;
		float memUtilization = 0;

		try{
			JKalman jk = new JKalman(1, 1);
			
			while (currentThread().isAlive()) {
				try
				{
					process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "tail -n 5 controlNodeLog | grep 'pending' | cut -d ' ' -f 6" });

					String str = null;
					BufferedReader infoReader = new BufferedReader(new InputStreamReader(
							process.getInputStream()));

					str = infoReader.readLine();
					infoReader.close();

					
					for (int i = 0; i < 10; i++) {
						memUtilization = totalMemUtilization(jk);
						
						if (memUtilization < memMinThreshold)
							break;
						Thread.sleep(sleeptime);
					}
					
					if (str != null) {
						if (Integer.parseInt(str) > 0)
							tuneCapacityScheduler(memUtilization);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				Thread.sleep(sleeptime);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			System.out.println("End work!");
		}
		finally{
			cnm.interrupt();
			mu.interrupt();
		}
	}

	private static void tuneCapacityScheduler(float memUtilization) throws IOException, InterruptedException{
		if ( memUtilization < memMinThreshold) {		
			upTuning(memUtilization, memMinThreshold);
		}else if (memUtilization > memMaxThreshold){
			downTuning(memUtilization, memMaxThreshold);
		}
	}
	
	private static void upTuning(float mem, float threshold){
		double val = (double)(Math.sqrt((threshold-mem)*100)/100);
		
		if (val > 0.01)
			Effectuator.effectuator("up", val);
		else
			Effectuator.effectuator("up", 0.01);
	}

	private static void downTuning(float mem, float threshold){
		double val = (double)(Math.sqrt((mem-threshold)*100)/100);
		
		if (val > 0.01)
			Effectuator.effectuator("down", val);
		else
			Effectuator.effectuator("down", 0.01);
	}

	private static float totalMemUtilization(JKalman jk) throws IOException{
		Process process = null;

		process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "tail -n 7 memLog | grep 'absolute' | sed -e 's=,==g' | cut -d ' ' -f 2" });

		String str = null;
		BufferedReader infoReader = new BufferedReader(new InputStreamReader(
				process.getInputStream()));

		str = infoReader.readLine();
		infoReader.close();
		
		if (str != null){		
			if (str.contains(","))
				str = backup;
			else
				backup = str;
			
			jk.Correct(new Matrix(1,1,Double.parseDouble(str)));
			return (float)jk.Predict().get(0, 0);
		}else
			return 0;
	}
}
