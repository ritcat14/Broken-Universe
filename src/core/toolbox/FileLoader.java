package core.toolbox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import core.Main;

public class FileLoader {
	
	private String file;
	private String[] data = new String[0];
	private String[] dataToWrite = new String[0];
	private boolean writeSuccess = false;
	private boolean dataWritten = false;
	private boolean dataRead = false;
	
	public String[] getData() {
		dataRead = false;
		return data;
	}
	
	public boolean isDataRead() {
		return dataRead;
	}
	
	public boolean isDataWritten() {
		return dataWritten;
	}
	
	public void writeData(String file, String[] data) {
		this.file = file;
		this.dataToWrite = data;
		writeSuccess = write();
		dataWritten = true;
	}
	
	public void readData(String file) {
		this.file = file;
		this.data = read();
		dataRead = true;
	}
	
	public int getLineNum() {
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

}
