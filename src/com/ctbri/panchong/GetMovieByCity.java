package com.ctbri.panchong;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.telecom.poi.util.NameUtil;

public class GetMovieByCity {

	public static void main(String[] args) {
		if(args.length ==3)
		{
			mergeMovie(args[0], args[1],args[2]);
		}
		else if(args.length ==4)
		{
			String temp_suff = args[0]+System.currentTimeMillis();
			mergeMovie(temp_suff, args[2],args[3]);
			mergeMovie(args[0], temp_suff,args[1]);
		}
		else			
		{
			mergeMovie("merge_movielist", "wanzhu_movielist.csv", "zhizhu_movielist.csv");
			String temp_suff = ""+System.currentTimeMillis();
			mergeMovie("merge_movielist"+temp_suff, "wanzhu_movielist.csv", "zhizhu_movielist.csv");
			mergeMovie("merge_movielist", "merge_movielist"+temp_suff,"wpw_movielist.csv");
			
		}
			
//		mergeMovie("test.movie", "zhizhu_movielist.csv", "wanzhu_movielist.csv");
	}
	
	/**
	 * 先将第二个文件读一遍放到内存中 然后在读第一个文件
	 * @param outpath
	 * @param path1
	 * @param path2
	 */
	public static void mergeMovie(String outpath,String path1,String path2)
	{
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		BufferedWriter bw = null;
		try{
			br1 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path1)),"GBK"));
			br2 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path2)),"GBK"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outpath)),"GBK"));
			
			Set<String> path2City = new HashSet<String>();
			Map<String,String> path2Content = new TreeMap<String,String>();
			
			
			
			String content1 = null;
			String content2 = null;
			while((content2 = br2.readLine())!= null)
			{
				int index = content2.indexOf("$");
				String citycode = content2.substring(0,index);
				String co = content2.substring(index+1);
				path2City.add(citycode);
				path2Content.put(citycode, co);
				
			}
			while((content1 = br1.readLine())!= null)
			{
				int index = content1.indexOf("$");
				String citycode = content1.substring(0,index);
				if(path2City.contains(citycode))
				{
					System.out.println("merge "+ citycode);
					String str1 = content1.substring(index+1);
					String str2 = path2Content.get(citycode);
					String re = parseMovies(str1 ,str2);
					StringBuffer sb = new StringBuffer();
					sb.append(content1.substring(0,index+1));
					sb.append(re);
					bw.write(sb.toString());
					bw.newLine();
					path2Content.remove(citycode);
				}
				else
				{
					bw.write(content1);
					bw.newLine();
				}
			}
			
			Set<String> keyset = path2Content.keySet();
			for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
				
				String string = (String) iterator.next();
				System.out.println("map have citycode "+ string);
				bw.write(string+"$"+path2Content.get(string));
				bw.newLine();
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				if(br1!= null) br1.close();
				if(br2!= null) br2.close();
				if(bw!= null) bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


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
					if(!s1_name[i].contains("IMAX") && !s1_name[i].contains("imax"))
						index = s1_name[i].indexOf("(");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println(s1_name);
					e.printStackTrace();
				}
				String s1_new_name =  s1_name[i];
				if(index>0)					
					s1_new_name= s1_name[i].substring(0,index);
				
				if(s2_name[j] == null) continue;
				int index2 = s2_name[j].indexOf("(");
				String s2_new_name =  s2_name[j];
				if(index2>0)					
					s2_new_name= s2_name[j].substring(0,index2);
				double score = NameUtil.diffScore(s1_new_name, s2_new_name);
//				System.out.println("diff "+s1_new_name+" , "+s2_new_name +" "+ score);
				if(score>0.8)
				{
					s1_code[i] = s1_code[i]+","+s2_code[j];
					s2_name[j] = null;
					break;
					
				}
			}
		}
		
		for (int i = 0; i < s1_name.length; i++) {
			if(s1_name[i] == null) continue;
//			if(s1_name[i].contains("控制"))
//				System.out.println("ddd ");
			for (int j = i+1; j < s1_name.length; j++) {
				int index =-1;
				try {
					if(!s1_name[i].contains("IMAX") && !s1_name[i].contains("imax"))
						index = s1_name[i].indexOf("(");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println(s1_name);
					e.printStackTrace();
				}
				String s1_new_name =  s1_name[i];
				if(index>0)					
					s1_new_name= s1_name[i].substring(0,index);
				
				if(s1_name[j] == null) continue;
				int index2 = -1;
				if(!s1_name[j].contains("IMAX") && !s1_name[j].contains("imax"))
					index2 = s1_name[j].indexOf("(");
				String s2_new_name =  s1_name[j];
				if(index2>0)					
					s2_new_name= s1_name[j].substring(0,index2);

//				System.out.println("self diff "+s1_new_name+" , "+s2_new_name +" ");
				double score = NameUtil.diffScore(s1_new_name, s2_new_name);
//				System.out.println("self diff "+s1_new_name+" , "+s2_new_name +" "+ score);
				if(score>0.8)
				{
					s1_code[i] = s1_code[i]+","+s1_code[j];
					s1_name[j] = null;
//					break;
					
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s1_name.length; i++) {
			if(s1_name[i] != null)
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



}
