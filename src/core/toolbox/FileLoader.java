package core.toolbox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileLoader implements Runnable {
	
	private Thread t;
	private String file;
	private boolean running = false;
	private int readWrite = -1;//read = 0, write = 1
	private String[] data = new String[0];
	private String[] dataToWrite = new String[0];
	private boolean writeSuccess = false;
	private boolean dataWritten = false;
	private boolean dataRead = false;
	
	public FileLoader() {
		start();
	}
	
	public synchronized String[] getData() {
		dataRead = false;
		return data;
	}
	
	public synchronized boolean isDataRead() {
		return dataRead;
	}
	
	public synchronized boolean isDataWritten() {
		return dataWritten;
	}
	
	public synchronized void start() {
		t = new Thread(this, "FileHandler");
		t.start();
		running = true;
	}
	
	public synchronized void stop() {
		try {
			t.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		running = false;
	}
	
	public void writeData(String file, String[] data) {
		readWrite = 1;
		this.file = file;
		this.dataToWrite = data;
	}
	
	public void readData(String file) {
		readWrite = 0;
		this.file = file;
	}
	
	private int getLineNum() {
		int lineNum = 0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.readLine() != null) {
				lineNum++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lineNum;
	}
	
	private String[] read() {
		String[] readData = new String[getLineNum()];
		String line;
		BufferedReader reader;
		try {
			int i = 0;
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine())!=null) {
				readData[i] = line;
				i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readData;
	}
	
	private boolean write() {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			for (String s : dataToWrite) {
				writer.println(s);
			}
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	@Override
	public void run() {
		while (running) {
			if (readWrite == 0) { // Read data
				this.data = read();
				readWrite = -1;
				dataRead = true;
			} else if (readWrite == 1) { //Write data
				writeSuccess = write();
				readWrite = -1;
				dataWritten = true;
			}
		}
	}

}
