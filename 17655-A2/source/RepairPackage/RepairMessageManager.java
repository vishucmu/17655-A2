package RepairPackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import sun.misc.Cleaner;

public class RepairMessageManager implements IRepair {

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		System.out.println("Clean called");
	}

	@Override
	public void repair() {
		runScript("src/RepairPackage/EMStart.sh", "");
	}

	@Override
	public void test() {
		System.out.println("Test called");
	}
	
	public static void runScript(String path, String... args) {
	    try {
	        String[] cmd = new String[args.length + 1];
	        cmd[0] = path;
	        int count = 0;
	        for (String s : args) {
	            cmd[++count] = args[count - 1];
	        }
	        Process process = Runtime.getRuntime().exec(cmd);
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        try {
	            process.waitFor();
	        } catch (Exception ex) {
	            System.out.println(ex.getMessage());
	        }
	        while (bufferedReader.ready()) {
	            System.out.println("Received from script: " + bufferedReader.readLine());
	        }
	    } catch (Exception ex) {
	        System.out.println(ex.getMessage());
	        System.exit(1);
	    }
	}
	
	public static void main(String []a) {
		runScript("src/RepairPackage/EMStart.sh", "");
	}

}
