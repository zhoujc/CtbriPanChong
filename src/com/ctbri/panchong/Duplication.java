package com.ctbri.panchong;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import au.com.bytecode.opencsv.CSVParser;

import com.telecom.poi.util.NameUtil;


// java -classpath ".;search-zhoujc-pica_20130625_153430.jar;commons-logging-1.0.4.jar;log4j-1.2.11.jar" com.ctbri.srhcore.C -1 -2
/**
 * 合并电影院
 * @author zhoujc
 *
 */
public class Duplication {
	
	
	private File file1;
	
	private File file2;
	
	private Set<String> dupId = new HashSet<String>();
	
	
	public void dup(String csvfileName,String resultfileName)
	{
		CSVParser cp = new CSVParser();
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		
		try {
			br1 = new BufferedReader(new InputStreamReader(new FileInputStream(this.file1),"GBK"));
			
//			bw1  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("dup.result")),"GBK"));
//			bw2  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("cinema_last.csv")),"GBK"));
			bw1  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(resultfileName)),"GBK"));
			bw2  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(csvfileName)),"GBK"));
			String content1 = "";
			String content2 = "";
			int count1 = 0;
			int count2 = 0;
			while((content1 = br1.readLine()) != null)
			{
				String[] ss1 = cp.parseLine(content1);
				br2 = new BufferedReader(new InputStreamReader(new FileInputStream(this.file2),"GBK"));
				while((content2 = br2.readLine()) != null)
				{
					count2++;
					String[] ss2 = cp.parseLine(content2);
					if(!ss1[3].equals(ss2[3])) continue;
					double score = NameUtil.diffScore(ss1[4], ss2[4]);

					if(score >0.7)
					{
						bw1.write(score+"");
						bw1.newLine();
						bw1.write("1-- "+content1);
						bw1.newLine();
						bw1.write("2-- "+ content2);
						bw1.newLine();
						bw1.newLine();
						dupId.add(ss2[0]);
						ss1[1] += ";"+ss2[1];
						ss1[14] =  parseMovies(ss1[14], ss2[14]);
						
					}					
				}
				ss1[0] = fomatId(ss1[0],"925");
				bw2.write(parse2CSVLine(ss1));
				bw2.newLine();
			}
			
			br2 = new BufferedReader(new InputStreamReader(new FileInputStream(this.file2),"GBK"));
			System.out.println(count2);
			while((content2 = br2.readLine()) != null)
			{
				String[] ss2 = cp.parseLine(content2);
				if(dupId.contains(ss2[0])) continue;
				ss2[0] = fomatId(ss2[0],"925");
				bw2.write(parse2CSVLine(ss2));
				bw2.newLine();
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				if(br1 != null) br1.close();
				if(br2 != null) br2.close();
				if(bw1 != null) bw1.close();
				if(bw2 != null) bw2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		//第一个文件选择是网票网的数据 网票网的数据还是比价好的。
		String filepath1 = "wanzhu_cinema.csv";
		String filepath2 = "zhizhu_cinema.csv";
		String filepath3 = "wpw_cinema.csv";
		if(args!= null && args.length ==2)
		{
			filepath1 = args[0];
			filepath2 = args[1];
		}
		if(args.length ==3)
			filepath3 = args[2];
		
		
		String tempFileName = "temp.csv";
		String lastFileName = "cinema_last.csv";
		if(args.length ==4)
			lastFileName =  args[3];
		String result1 = "dup1.result";
		String result2 = "dup2.result";
//		String resultfile = System.currentTimeMillis()+".result";
		
		
		Duplication dup = new Duplication();
		dup.setFile1(new File(filepath1));
		dup.setFile2(new File(filepath2));
		dup.dup(tempFileName,result1);
		
		
		dup.setFile1(new File(tempFileName));
		dup.setFile2(new File(filepath3));
		dup.dup(lastFileName, result2);
		

//		String ss = parseMovies("", "");
		String ss = parseMovies("诡魇（3d）|2-784477;高铁英雄|2-7876", "诡魇|3-7877;高铁英雄|3-7876;测试|3-sdfklj");
		System.out.println(ss);
		
	}
	
	public static String fomatId(String id,String pre)
	{
		int len = id.length();
		if(len>=9) return id;
		int ex = 9- pre.length() -len;//id总长是9  -前缀长度是 -已有长度 就是要补齐的长度
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ex; i++) {
			sb.append("0");
		}
		
		return pre+sb.toString()+id;
	}
	
	public static String parse2CSVLine(String[] source)
	{
		int len = source.length;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append("\"").append(source[i]).append("\"").append(",");
		}
		return sb.substring(0, sb.length()-1);
	}
	
	public static String parseMovies(String s1 ,String s2)
	{
		if(s1 == null &&s2== null) return "";
		if(s1.trim().length() ==0 && s2.trim().length() ==0) return "";
		if(s1.trim().length() ==0 && s2.trim().length() !=0) return s2;
		if(s2.trim().length() ==0 && s1.trim().length() !=0) return s1;
		String[] ss1 = s1.split(";");
		String[] ss2 = s2.split(";");
//		if(ss1.length< ss2.length)
//		{
//			String[] temp = ss1;
//			ss1 = ss2;
//			ss2 = temp;
//		}
		String[] s1_name = new String[ss1.length];
		String[] s1_code = new String[ss1.length];
		String[] s2_name = new String[ss2.length];
		String[] s2_code = new String[ss2.length];
		
		
		for (int i = 0; i < ss1.length; i++) {
			String[] temp = ss1[i].split("\\|");
			if(temp.length!=2)
			{
				continue;
			}
			s1_name[i] = temp[0];
			s1_code[i] = temp[1];
		}
		
		for (int i = 0; i < ss2.length; i++) {
			String[] temp = ss2[i].split("\\|");
			if(temp.length!=2)
			{
				continue;
			}
			s2_name[i] = temp[0];
			s2_code[i] = temp[1];
		}
		
		for (int i = 0; i < s1_name.length; i++) {
			for (int j = 0; j < s2_name.length; j++) {
				int index =-1;
				try {
					index = s1_name[i].indexOf("（");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println(s1_name[i]);
					e.printStackTrace();
				}
				String s1_new_name =  s1_name[i];
				if(index>0)					
					s1_new_name= s1_name[i].substring(0,index);
				
				if(s2_name[j] == null) continue;
				int index2 = s2_name[j].indexOf("（");
				String s2_new_name =  s2_name[j];
				if(index2>0)					
					s2_new_name= s2_name[j].substring(0,index2);
				
				double score = NameUtil.diffScore(s1_new_name, s2_new_name);
				if(score>0.8)
				{
					s1_code[i] = s1_code[i]+","+s2_code[j];
					s2_name[j] = null;
					break;
					
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s1_name.length; i++) {
			sb.append(s1_name[i]).append("|").append(s1_code[i]).append(";");
		}
		for (int i = 0; i < s2_name.length; i++) {
			if(s2_name[i]!= null)
			{
				sb.append(s2_name[i]).append("|").append(s2_code[i]).append(";");
			}
		}
		if(sb.length()>0)
			return sb.substring(0,sb.length()-1);
		
		
		return "";
	}
	
	public File getFile1() {
		return file1;
	}

	public void setFile1(File file1) {
		this.file1 = file1;
	}

	public File getFile2() {
		return file2;
	}

	public void setFile2(File file2) {
		this.file2 = file2;
	}
	
	

	
	

}
