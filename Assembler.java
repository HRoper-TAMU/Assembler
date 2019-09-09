/*
Hayden Roper
926004150
*/

import java.util.*;
import java.io.*;

public class Assembler 
{
	public static void main(String[] args)
	{
		//preload the symbol tables into maps
		Map<String,String> comp = new HashMap<String,String>();
		comp.put("0","0101010");
		comp.put("1","0111111");
		comp.put("-1","0111010");
		comp.put("D","0001100");
		comp.put("A","0110000");
		comp.put("!D","0001101");
		comp.put("!A","0110001");
		comp.put("-D","0001111");
		comp.put("-A","0110011");
		comp.put("D+1","0011111");
		comp.put("A+1","0110111");
		comp.put("D-1","0001110");
		comp.put("A-1","0110010");
		comp.put("D+A","0000010");
		comp.put("D-A","0010011");
		comp.put("A-D","0000111");
		comp.put("D&A","0000000");
		comp.put("D|A","0010101");
		comp.put("M","1110000");
		comp.put("!M","1110001");
		comp.put("-M","1110011");
		comp.put("M+1","1110111");
		comp.put("M-1","1110010");
		comp.put("D+M","1000010");
		comp.put("D-M","1010011");
		comp.put("M-D","1000111");
		comp.put("D&M","1000000");
		comp.put("D|M","1010101");
		
		Map<String,String> dest = new HashMap<String,String>();
		dest.put("null","000");
		dest.put("M","001");
		dest.put("D","010");
		dest.put("A","100");
		dest.put("MD","011");
		dest.put("AM","101");
		dest.put("AD","110");
		dest.put("AMD","111");
		
		Map<String,String> jump = new HashMap<String,String>();
		jump.put("null","000");
		jump.put("JGT","001");
		jump.put("JEQ","010");
		jump.put("JGE","011");
		jump.put("JLT","100");
		jump.put("JNE","101");
		jump.put("JLE","110");
		jump.put("JMP","111");
		
		//default symbols, will add custom labels to in first pass
		Map<String,String> symbols = new HashMap<String,String>();
		symbols.put("SP","0");
		symbols.put("LCL","1");
		symbols.put("ARG","2");
		symbols.put("THIS","3");
		symbols.put("THAT","4");
		symbols.put("SCREEN","16384");
		symbols.put("KBD","24576");
		symbols.put("R0","0");
		symbols.put("R1","1");
		symbols.put("R2","2");
		symbols.put("R3","3");
		symbols.put("R4","4");
		symbols.put("R5","5");
		symbols.put("R6","6");
		symbols.put("R7","7");
		symbols.put("R8","8");
		symbols.put("R9","9");
		symbols.put("R10","10");
		symbols.put("R11","11");
		symbols.put("R12","12");
		symbols.put("R13","13");
		symbols.put("R14","14");
		symbols.put("R15","15");
		
		//get file/make new file from command line
		String name = args[0];	
		File fin = new File(name);	
		
		String na = name.substring(0,name.length()-4);
		na += ".hack";
		File fout = new File(na);
		Scanner in;
		PrintWriter pw;
		int lines=0;
		int index = 16;
		
		//first pass
		try
		{	
		
			in = new Scanner(fin);
			pw = new PrintWriter(fout);
			
			while(in.hasNextLine())
			{
				String line = in.nextLine();
				line = line.replaceAll("\\s+","");
				String key = "";	
				//skips comments/empty lines	
				if(line!=null&& line.length()>0&& line.charAt(0)!= '/')
				{
					//counts lines that have valid commands
					lines++;
					
					if(line.charAt(0)=='(') //finds the start of a label
					{
						key = line.substring(1,line.length()-1);
						lines--; //this is not a valid line
					
						if(lines>=1) //put label in symbols with a num pointing to the previous valid line
						{
							String l = "" + lines; 
							symbols.put(key, l);
						}
						else //first line  is a label
						{
							symbols.put(key,"1");
						}
					}
				}
			}		
			
			//pass2
			String build = "";
			in.close();
			in = new Scanner(fin);
			
			while(in.hasNextLine())
			{
				String line = in.nextLine();
				line = line.replaceAll("\\s+","");
				
				//skip comments and empty lines
				if(line!=null&& line.length()>0&& line.charAt(0)!= '/'&&line.charAt(0)!='(')
				{
					//A instruction
					if(line.charAt(0)=='@')
					{
						//is it a mem address
						if(Character.isDigit(line.charAt(1)))
						{
							int endNum = 1;
							//System.out.println(line);
							while(endNum<line.length()&&Character.isDigit(line.charAt(endNum)))
							{
								endNum++;
							}
							
							//turn into a binary string of length 16
							String num = line.substring(1,endNum);
							int val = Integer.parseInt(num);
							build = Integer.toBinaryString(val);
							for(int i=build.length();i<16;i++)
							{
								build  = "0" + build;
							}
							//for some reason doesn't work with 16384+
							//build = String.format("%016d",Integer.parseInt(build));
							pw.println(build);
						}
						else //it is a label/variable/error
						{
							int endNum = 1;
							
							while(endNum<line.length()&&line.charAt(endNum)!='/' )
							{
								endNum++;
							}
							
							String var = line.substring(1,endNum);
							
							if(symbols.get(var)==null) //symbol doesn't exist exists
							{
								String add = "" + index;
								symbols.put(var,add);
								index++;
							}
							build = "";
							
							int val = Integer.parseInt(symbols.get(var));
							
							build = Integer.toBinaryString(val);
							
							for(int i=build.length();i<16;i++)
							{
								build  = "0" + build;
							}
							//for some reason doesn't work with 16384+
							//build = String.format("%016d",Integer.parseInt(build));
							pw.println(build);
						}
					}
					else //C instruction or syntax error
					{
						build = "111";
						if(line.contains("=")&&line.contains(";"))
						{
							//System.out.println("both");
							
							int endNum = 0;
								while(line.charAt(endNum)!='=')
								{
									endNum++;
								}
						//	System.out.println(line);
							String key = line.substring(0,endNum);
							String des = dest.get(key);
							
						//	System.out.println("dest: " + des);
							endNum++;
							int start = endNum;
							while(endNum<line.length()&&line.charAt(endNum)!=';'&&line.charAt(endNum)!='/')
							{
								endNum++;
							}
							key = line.substring(start,endNum);
							//System.out.println("comp: " + key);
							build = build + comp.get(key) + des;
							start = endNum +1;
							endNum = start;
							while(endNum<line.length()&&line.charAt(endNum)!='/')
							{
								endNum++;
							}
							key = line.substring(start,endNum);
							//System.out.println("jump: "+key);
							build = build + jump.get(key);
							//System.out.println(line);
							//	System.out.println(build+"\n");
							
						}
						else if(line.contains(";"))
						{
							//System.out.println("just ;");
							//System.out.println(line);
							int endNum = 0;
							while(line.charAt(endNum)!=';')
							{
								endNum++;
							}
							String key = line.substring(0,endNum);
						//	System.out.println("comp: " + key);
							build += comp.get(key) + "000";
							key = line.substring(endNum+1,endNum+4);
							//System.out.println("Jump: " + key);
							build += jump.get(key);		
								//System.out.println(line);
							//	System.out.println(build+"\n");
							
						}
						else if(line.contains("="))
						{
							//System.out.println("just =");
							int endNum = 0;
								while(line.charAt(endNum)!='=')
								{
									endNum++;
								}
							//System.out.println(line);
							String key = line.substring(0,endNum);
							String des = dest.get(key);
							//System.out.println("Dest: " + des);
							//System.out.println(build);
							endNum++;
						
							int start = endNum;
							while(endNum<line.length()&&line.charAt(endNum)!='/')
							{
								endNum++;
							}
							key = line.substring(start,endNum);
							//System.out.println("comp: " + key);
							build = build + comp.get(key) + des + "000";
								
								//System.out.println(build+"\n");
							
						}
						else
						{
							throw new IllegalArgumentException("There is a syntax error in the .asm file.");
						}
						pw.println(build);
						
						
						
					}
					
				}
				
			}
			//System.out.println(symbols);
			pw.close();
			in.close();
			//fff.close();
		}
		catch(Exception e)
		{
			System.err.println("Error: ");
			e.printStackTrace();
		}		
	}	
}