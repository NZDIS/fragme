package org.nzdis.fragme.otago.p2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Whenever what kind of file you are sending,
 * you have to serialize file into a FragMeFile first
 * 
 * @author DengFeng , Ning, Abdulla
 * 
 */
public class FragMeFile implements Serializable {
	private String fileName;

	private File sourceFile;

	private byte[] data;

	/**
	 * Constructor, the only thing you need to do is giving file name, then it will 
	 * generate a FragMeFile which is the container of the file you want sending.
	 * 
	 * @param file The file you want send
	 * @throws IOException Can not find the file you specified.
	 */
	public FragMeFile(String file) {
		sourceFile = new File(file);
		fileName = sourceFile.getName();
		this.readData();
	}
	
	
	/**
	 * Loading data from the file
	 * 
	 * Note: ReadData & WriteData can not called simultaneity
	 *
	 */
	public void readData(){
		try {
			data = getBytesFromFile(sourceFile);
		} catch (IOException e) {
			System.err.println("Can not read from file " + fileName);
		}
	}
	
	/**
	 * Write data into new file
	 *
	 * Note: ReadData & WriteData can not called simultaneity
	 */
	public void writeData(){
		try {
			setBytesToFile(new File(fileName), data);
		} catch (IOException e) {
			System.err.println("Can not write from file " + fileName);
		}
	}
	
	/**
	 * 
	 * @param path
	 */
	public void writeData(String path) {
		try {
			setBytesToFile(new File(path), data);
		} catch (IOException e) {
			System.err.println("Can not write to file " + fileName);
		}
	}

	
	/**
	 * Get the file name from systm path
	 * 
	 * @return the source file we need read, only find the right name and extensitions(For exampe .exe, .bmp, .mp3) .
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Get the file data 
	 * 
	 * @return the file or image data as a byte array
	 */
	public byte[] getFileData() {
	    return data;
	}
	
	/**
	 * 
	 * 
	 * @param file The file you want read
	 * @return byte[] contains all the data from file. raw format
	 * @throws IOException
	 */
	private byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			System.err.println("File is too large!");
		}

		byte[] bytes = new byte[(int) length];
		
		int offset = 0;
		int numRead = 0;
		
		while (offset < bytes.length&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+ file.getName());
		}

		is.close();
		return bytes;
	}

	/**
	 * 
	 * @param file The file you want write to
	 * @param info byte[] All the data you need write into file
	 * @throws IOException
	 */
	private void setBytesToFile(File file, byte[] info) throws IOException {
		OutputStream os = new FileOutputStream(file);
		os.write(info);
		os.flush();
		os.close();
	}
}
