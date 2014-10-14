package Utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class IO {
	public static ArrayList<String> loadFile(String path) throws Exception{
		ArrayList<String> content = new ArrayList<String>();
		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		while((line = br.readLine())!=null){
			content.add(line);
		}
		br.close();
		isr.close();
		fis.close();
		return content;
	}
	
	public static void writeFile(StringBuffer sb, String path) throws Exception{
		File f = new File(path);
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(sb.toString());
		bw.close();
		osw.close();
		fos.close();
	}
	
	public static void appendFile(StringBuffer sb, String path) throws Exception{
		File f = new File(path);
		FileOutputStream fos = new FileOutputStream(f, true);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(sb.toString());
		bw.close();
		osw.close();
		fos.close();
	}
	
	public static void main(String[] args) throws Exception {
		ArrayList<String> content = IO.loadFile("E:\\Xiaochi Wei\\Py space\\Baike Parser\\part-r-00000_dir_text\\0.txt");
		for(int i=0;i<content.size();i++){
			System.out.println(content.get(i));
		}
	}
}
