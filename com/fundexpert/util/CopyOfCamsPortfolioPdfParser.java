package com.fundexpert.util;

import java.text.DecimalFormat;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.fundexpert.exception.FundexpertException;
import com.fundexpert.pojo.Holdings;
import com.fundexpert.pojo.Transactions;

/*import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.Math;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.exceptions.InvalidPdfException;

public class CopyOfCamsPortfolioPdfParser
{
	String path = "";
	String password = "";
	String fh = null;
	int numberSize = 0;// stores the size of number array
	int cnt = 0;// counts total number of transactions
	int po = 0;
	// int fhnIndex = 0;
	// int numberOfMfIndex = 0;// numberOfMfindex used to iterate through numberOfMfUnderOneHouse
	double[][] doub = null;
	String[][] portTable = null;
	// number of transactions under one mf is stored in number array which is created below
	public static int[] number = null;// stores number of transactions under one folio(at one index)
	// A holding (1)list which again has elements as (2)list,where(2)list contains all the transactions under that holding
	// Holdings class contains list for Transactions
	ArrayList<Holdings> holdingsList = new ArrayList<Holdings>();
	ArrayList<Transactions> transactionsList = new ArrayList<Transactions>();
	Holdings holding = null;
	Transactions transactions = null;
	Map<Holdings, ArrayList<Transactions>> holdingsMap = new HashMap<Holdings, ArrayList<Transactions>>();

	// this constructor stores the path to pdf file and its password
	public CopyOfCamsPortfolioPdfParser(String path, String password)
	{
		this.path = path;
		this.password = password;
	}

	public CopyOfCamsPortfolioPdfParser()
	{
		// TODO Auto-generated constructor stub
	}

	public List getHoldingsList() throws BadPasswordException, InvalidPdfException, Exception
	{
		int n = 0;
		String myLine = "", demo[];
		String name = null;
		String tempFolio[] = new String[2];
		PdfReader reader = null;
		Date statementDate=null;
		InputStream is=null;
		String emailId=null;
		try
		{
			File f=new File(path);
			is=new FileInputStream(f);
			reader = new PdfReader(is, password.getBytes());
			//PdfReader.unethicalreading = true;
			n = reader.getNumberOfPages();
			PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);

			// below for loop stores whatever is in the pdf to the string myLine
			//
			/*     for (int index =1; index <= n; index++) {
			         byte[] pageBuf = reader.getPageContent(index);
			         String pageContent = new String(pageBuf);
			         System.out.println("Page - " + index);
			         //System.out.println(pageContent);
				myLine = myLine + pageContent;
			         System.out.println("");
			     }*/
			 for(int i = 1; i <= n; i++) {
			        // Extract content of each page
			        String contentOfPage = pdfTextExtractor.getTextFromPage(i, true);
			        System.out.println("Content OF Page="+contentOfPage);
      			}
			/*for (int i = 1; i <= n; i++)
			{
				//all data is going into myline
				myLine = myLine + PdfTextExtractor.getTextFromPagei(i,true);
			}
			System.out.println("Here.................................................."+myLine);*/
		}
		catch (InvalidPdfException ipe)
		{
			ipe.printStackTrace();
			throw new InvalidPdfException("Not a pdf file");
		}
		catch (BadPasswordException bpe)
		{
			bpe.printStackTrace();
			throw new BadPasswordException("Please upload CAS linked with PAN.");
		}
		finally
		{
			if(reader!=null)
				reader.close();
			if(is!=null)
				is.close();
		}
				Matcher m = Pattern.compile("\r\n|\r|\n").matcher(myLine);
		// demo array stores individual lines from myLine
		demo = myLine.split("\r|\n|\r\n|\n\r");
		// i the index of transaction
		int i = 0;
		// openingFlag is set to true when it sees Opening as word(to know tranasactions are from next line)
		// flag is set to true when we are ready to process transactions after seeing openingFlag and false when we see valuation
		boolean flag = false, openingFlag = false;
		String transaction[] = new String[20];
		int b = 0;
		int add = 0;
		// counts total transactions in the pdf
		int totalTransaction = 0;
		// numberOfTransaction counts number of transaction under one folio and then stores it in number array
		int numberOfTransaction = 0;
		int index1 = 0;
		// the size to number is given on assumption but it will never go out of memory
		number = new int[demo.length / 2];
		// value of indexNumber tells current index of holding in pdf
		int indexOfNumber = 0;
		for (i = 0; i < number.length; i++)
		{
			number[i] = -1;
		}
		i = 0;
		// System.out.println("after pdf=" + myLine);
		/*\\for (int l = 0; l < demo.length; l++)
		{
			System.out.println("demo[" + l + "]=" + demo[l]);
		}*/
		for (int k = 0; k < demo.length; k++)
		{
			if(emailId==null)
			{
				String emailLine=demo[k];
				if(emailLine.contains("Email Id:"))
				{
					//System.out.println("emailLine"+emailLine);
					//Email Id: vikas93_sharma@yahoo.com This Consolidated Account Statement is brought to you as an investor
					int in1=emailLine.indexOf(":")+2;
					int indexOfSpace = emailLine.indexOf(" ", in1);
					String email = emailLine.substring(in1,indexOfSpace);
					if(email.matches("(.)*@[a-zA-Z]+(\\.)[a-zA-Z]{2,4}"))
						emailId=email;
					//System.out.println("Email got from pdf is  ="+emailId);
				}
				
			}
			if(k==0 && demo[k].equals("Consolidated Account Statement"))
			{
				int indexOfStatementDate=k+1;
				String lineAtStatementDate=demo[indexOfStatementDate];
				int indexOfTO=lineAtStatementDate.indexOf("To");
				if(indexOfTO>0)
				{
					String date=lineAtStatementDate.substring(indexOfTO+3);
					//System.out.println("StatementDate ="+date);
					statementDate=new SimpleDateFormat("dd-MMM-yyyy").parse(date);
				}
			}
			if(openingFlag == true && demo[k].indexOf("*** No transactions ") != -1)
			{
				continue;
			}
			if((demo[k].length() > 10 && demo[k].substring(0, 9).equals("Valuation")) || (demo[k].length() == 11 && demo[k].substring(0, 4).equals("Page")))
			{
				b++;
			}
			if(openingFlag && (demo[k].indexOf("Valuation") != -1 || demo[k].indexOf("Page") != -1))
			{
				if(demo[k].indexOf("Valuation") != -1)
				{
					// below code is to fetch closing bal,nav val,valuation amt
					int q = 0;// q describes last 4 columns of pdf i.e amount,price etc
					int fromIndex = 0;
					// when closing,valutaion,nav values are all in one line in demo use below if()
					if(demo[k].indexOf("Closing") != -1)
					{
						while (q != 1)
						{
							fromIndex = demo[k].indexOf(":", fromIndex);
							Matcher mo = Pattern.compile("[0-9]+[,]*[0-9]*[/.][0-9]+").matcher(demo[k].substring(fromIndex, demo[k].length()));
							if(mo.find())
							{
								if(q == 0)
								{
									if(mo.group(0).indexOf(',') != -1)
									{
										StringBuilder sb = new StringBuilder(mo.group(0));
										sb.deleteCharAt(mo.group(0).indexOf(','));
										// the holding object is created below
										holding.setClosingUnits(Double.valueOf(sb.toString()));
									}
									else
									{
										holding.setClosingUnits(Double.valueOf(mo.group(0)));
									}
								}
							}
							q++;
						}
					}
					// this else solves when valuation comes in k line and closing,nav comes in k+1
					else
					{
						String merge = demo[k + 1] + " " + demo[k];
						while (q != 1)
						{
							fromIndex = merge.indexOf(":", fromIndex);
							Matcher mo = Pattern.compile("[0-9]+[,]*[0-9]*[/.][0-9]+").matcher(merge.substring(fromIndex, merge.length()));
							if(mo.find())
							{
								if(q == 0)
								{
									if(mo.group(0).indexOf(',') != -1)
									{
										StringBuilder sb = new StringBuilder(mo.group(0));
										sb.deleteCharAt(mo.group(0).indexOf(','));
										holding.setClosingUnits(Double.valueOf(sb.toString()));
									}
									else
									{
										holding.setClosingUnits(Double.valueOf(mo.group(0)));
									}
								}
							}
							q++;
						}
					}
					// below code is to help with differentiating only transactions with other part in demo array by setting flag=false
					flag = false;
					openingFlag = false;
					number[indexOfNumber] = numberOfTransaction;
					indexOfNumber++;
					// now a add the holding to list
					holdingsList.add(holding);
				}
				else
				{
					flag = false;
				}
				b++;
			}
			if(flag == true)
			{
				numberOfTransaction++;
				// below if is used in case if transaction index goes beyond its length
				if(i > transaction.length - 1)
				{
					add = transaction.length;
					String temporary[] = new String[add];
					temporary = transaction;
					transaction = new String[transaction.length + 20];
					System.arraycopy(temporary, 0, transaction, 0, temporary.length);
				}
				// transaction achieved from demo
				transaction[i] = demo[k];
				i++;
			}
			if(demo[k].length() > 10 && demo[k].substring(1, 8).equals("Opening"))
			{
				// as soon as we get OpeningFlag==true we get new holdings
				holding = new Holdings();
				holding.setEmail(emailId);
				holding.setStatementDate(statementDate);
				openingFlag = true;
				// after seeing flag==true store all transactions in different string say transaction[i]=demo[k]
				flag = true;
				numberOfTransaction = 0;
				// folio number occurs two lines above 'opening' word in demo hence [k-2]
				// below if else find folioNumber considering different use cases as per 'demo'
				/*if(demo[k - 2].matches("(.)+[ ][0-9]+[ ]/[ ][0-9]+(.)+") || demo[k - 2].matches("(.)+[ ][0-9]+(.)+"))
				{
					Matcher match1 = Pattern.compile("[ ][0-9]+[ ][/][ ][0-9]+").matcher(demo[k - 2]);
					if(match1.find())
					{
						tempFolio = match1.group(0).split(" / ");
						if(tempFolio[1].trim().equals("0"))
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim()));
							holding.setFolioNumberString(tempFolio[0].trim());
						}
						else
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim() + tempFolio[1].trim()));
							holding.setFolioNumberString((tempFolio[0].trim() + "/" + tempFolio[1].trim()).replaceAll(" ", ""));
						}
					}
					else if(demo[k - 2].matches("(.)+[0-9]+(.)+"))
					{
						Matcher match2 = Pattern.compile("[ ][A-Za-z]*[0-9]+[ ]").matcher(demo[k - 2]);
						if(match2.find())
						{
							// holding.setFolioNumber(Long.valueOf(match2.group(0).trim()));
							holding.setFolioNumberString(match2.group(0).trim());
						}
					}
				}
				else*/
				{
					int backwards = k;
					while (!demo[backwards].substring(0, 5).equals("Folio"))
					{
						backwards--;
					}
					Matcher match = Pattern.compile("[ ][.0-9]+[ ]/[ ][0-9]+").matcher(demo[backwards]);
					if(match.find())
					{
						// after changes
						tempFolio = match.group(0).split(" / ");
						if(tempFolio[1].trim().equals("0"))
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim()));
							holding.setFolioNumberString(tempFolio[0].trim());
						}
						else
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim() + tempFolio[1].trim()));
							holding.setFolioNumberString((tempFolio[0].trim() + "/" + tempFolio[1].trim()).replaceAll(" ", ""));
						}
						// holding.setFolioNumberString(match.group(0).trim().replaceAll(" ", ""));
					}
					else if(demo[backwards].matches("(.)+[0-9]+(.)+"))
					{
						Matcher match2 = Pattern.compile("[ ][A-Za-z]*[0-9]+[ ]").matcher(demo[backwards]);
						if(match2.find())
						{
							// holding.setFolioNumber(Long.valueOf(match2.group(0).trim()));
							holding.setFolioNumberString(match2.group(0).trim());
						}
						else
							throw new Exception("No Match of FolioNumber");
					}
					//System.out.println("backwards="+backwards+" k="+k);
				}
				//System.out.println("demo[k - 1] is" +demo[k - 1]);
				// when the page change and opening is true and opening unit balance comes after "(INR) (INR)"
				if(demo[k - 1].contains("(INR) (INR)"))
				{
					//System.out.println("!@#$");
					if(demo[k - 7].substring(0, demo[k - 7].indexOf("-") + 1).matches("[a-zA-Z0-9]+[-]"))
					{
						//System.out.println("!@#$1");
						int camsCodeEndIndex = demo[k - 7].indexOf("-");

						//camsCode might contains '-' so we have to check again for '-' for next 2 chars
						//for eg FFEF-G this one is real ,FFEF-GRE-L&T - this one is made up hence dummy
						String nextTwoCharAfterHyphenContainsAnotherHyphen=demo[k-7].substring(camsCodeEndIndex+1,(camsCodeEndIndex+1)+4);
						if(nextTwoCharAfterHyphenContainsAnotherHyphen.contains("-"))
						{
							int otherCamsCodeIndex=nextTwoCharAfterHyphenContainsAnotherHyphen.indexOf("-");
							camsCodeEndIndex+=(otherCamsCodeIndex+1);
						}
						
						String camsCode = demo[k - 7].substring(0, camsCodeEndIndex);
						String mutualFundName = demo[k - 7].substring(camsCodeEndIndex + 1);
						if(mutualFundName.contains("ARN"))
						{
							int indexOfARN=mutualFundName.indexOf("ARN");
							int indexOfClosedBracket=mutualFundName.indexOf(")", indexOfARN);
							if(indexOfClosedBracket != -1)
							{
								String arn=mutualFundName.substring(indexOfARN,indexOfClosedBracket);
								if(!arn.contains("-") && arn.contains("ARN"))
								{
									int indexOfArn = arn.indexOf("ARN");
									String arnValue = arn.substring(indexOfArn+3); 
									arn = "ARN-"+arnValue;
								}
								holding.setArn(arn);
							}
						}
						holding.setCamsCode(camsCode);
						holding.setMutualFundName(mutualFundName);
						
						
						//System.out.println("1........................."+camsCode + "-" + mutualFundName);
					}
					else
					{
						//System.out.println("!@#$2");
						int backwards = k - 7;
						while (!demo[backwards].substring(0, demo[backwards].indexOf("-") + 1).matches("[a-zA-Z0-9]+[-]"))
						{
							backwards--;
						}
						int camsCodeEndIndex = demo[backwards].indexOf("-");
						
						//camsCode might contains '-' so we have to check again for '-' for next 2 chars
						//for eg FFEF-G this one is real ,FFEF-GRE-L&T - this one is made up hence dummy
						String nextTwoCharAfterHyphenContainsAnotherHyphen=demo[backwards].substring(camsCodeEndIndex+1,(camsCodeEndIndex+1)+4);
						if(nextTwoCharAfterHyphenContainsAnotherHyphen.contains("-"))
						{
							int otherCamsCodeIndex=nextTwoCharAfterHyphenContainsAnotherHyphen.indexOf("-");
							camsCodeEndIndex+=(otherCamsCodeIndex+1);
						}
						
						String camsCode = demo[k - 1].substring(0, camsCodeEndIndex);
						String mutualFundName = demo[backwards].substring(camsCodeEndIndex + 1);
						while (backwards != k - 7)
						{
							backwards++;
							mutualFundName = mutualFundName + demo[backwards];
						}
					}
				}
				//in below else if first condition satisfy 'camsCode-Mutualfund name' starting from index 0 where mutualfund name should start with [a-z,A-z]
				//2nd condition states that if demo[k-1] has pattern like '[a-zA-z]-Mutualfund name' starting from index 0 then mutualfund name should not start with name '[0-9]' which is the case when 'ARN-234324)' is at line demo[k-1] and if it starts with [0-9]+ then check if demo[k-1] has 'ARN'
				else if(demo[k - 1].substring(0, demo[k - 1].indexOf("-") + 1).matches("[A-Z0-9]+[-]") &&
                ((demo[k-1].substring(demo[k - 1].indexOf("-"),demo[k - 1].indexOf("-")+2).matches("[/-][0-9]") && !demo[k-1].substring(0,demo[k - 1].indexOf("-")).toLowerCase().contains("arn")) || (!demo[k-1].substring(demo[k - 1].indexOf("-"),demo[k - 1].indexOf("-")+2).matches("[/-][0-9]"))))
				{
					//System.out.println("demo[k - 1] is "+demo[k - 1]);
					//System.out.println("demo[k - 1].substring(0, demo[k - 1].indexOf(\"-\") + 1)"+demo[k - 1].substring(0, demo[k - 1].indexOf("-") + 1));
					//System.out.println(demo[k-1].substring(demo[k - 1].indexOf("-"),demo[k - 1].indexOf("-")+2));
					//System.out.println(demo[k-1].substring(0,demo[k - 1].indexOf("-")).toLowerCase().contains("arn"));
					//System.out.println(demo[k-1].substring(demo[k - 1].indexOf("-"),demo[k - 1].indexOf("-")+2).matches("[/-][0-9]"));
					int camsCodeEndIndex = demo[k - 1].indexOf("-");
					
					//camsCode might contains '-' so we have to check again for '-' for next 2 chars
					//for eg FFEF-G this one is real ,FFEF-GRE-L&T - this one is made up hence dummy
					String nextTwoCharAfterHyphenContainsAnotherHyphen=demo[k-1].substring(camsCodeEndIndex+1,(camsCodeEndIndex+1)+4);
					if(nextTwoCharAfterHyphenContainsAnotherHyphen.contains("-"))
					{
						int otherCamsCodeIndex=nextTwoCharAfterHyphenContainsAnotherHyphen.indexOf("-");
						camsCodeEndIndex+=(otherCamsCodeIndex+1);
					}
					
					String camsCode = demo[k - 1].substring(0, camsCodeEndIndex);
					String mutualFundName = demo[k - 1].substring(camsCodeEndIndex + 1);
					if(mutualFundName.contains("ARN"))
					{
						int indexOfARN=mutualFundName.indexOf("ARN");
						int indexOfClosedBracket=mutualFundName.indexOf(")", indexOfARN);
						if(indexOfClosedBracket!=-1)
						{
							String arn=mutualFundName.substring(indexOfARN,indexOfClosedBracket);
							if(!arn.contains("-") && arn.contains("ARN"))
							{
								int indexOfArn = arn.indexOf("ARN");
								String arnValue = arn.substring(indexOfArn+3); 
								arn = "ARN-"+arnValue;
							}
							holding.setArn(arn);
						}
					}
					holding.setCamsCode(camsCode);
					//System.out.println("camsCode="+camsCode);
					holding.setMutualFundName(mutualFundName);
					//System.out.println("2......................."+camsCode + "-" + mutualFundName);

				}// condition states that if demo[k-1] has pattern like '[a-zA-z]-Mutualfund name' starting from index 0 then mutualfund name should not start with name '[0-9]' which is the case when 'ARN-234324)' is at line demo[k-1] and if it starts with [0-9]+ then check if demo[k-1] has 'ARN'
				else if(demo[k - 2].substring(0, demo[k - 2].indexOf("-") + 1).matches("[a-zA-z0-9]+[-]"))
				{
					//when arn shifts one line below

					//System.out.println("when demo is k-2");
					//System.out.println("!@#$4");
					int camsCodeEndIndex = demo[k - 2].indexOf("-");
					//camsCode might contains '-' so we have to check again for '-' for next 2 chars
					//for eg FFEF-G this one is real ,FFEF-GRE-L&T - this one is made up hence dummy
					String nextTwoCharAfterHyphenContainsAnotherHyphen=demo[k-2].substring(camsCodeEndIndex+1,(camsCodeEndIndex+1)+4);
					if(nextTwoCharAfterHyphenContainsAnotherHyphen.contains("-"))
					{
						int otherCamsCodeIndex=nextTwoCharAfterHyphenContainsAnotherHyphen.indexOf("-");
						camsCodeEndIndex+=(otherCamsCodeIndex+1);
					}
					String camsCode = demo[k - 2].substring(0, camsCodeEndIndex);
					String mutualFundName = demo[k - 2].substring(camsCodeEndIndex + 1) + demo[k - 1];
					if(mutualFundName.contains("ARN"))
					{
						//System.out.println("New String!@# : ");
						int indexOfARN=mutualFundName.indexOf("ARN");
						int indexOfHyphen=mutualFundName.indexOf("-", indexOfARN);
						//now we have index of '-' which comes after 'ARN'. So now we look for '[0-9]+)' pattern.
						if(indexOfHyphen!=-1)
						{
							//System.out.println("Index of hyphen="+indexOfHyphen+" indexOfARN="+indexOfARN);
							//get index of first occurrence of digit in ARN no.
							int indexOfFirstDigit=-1;
							int indexOfClosedBracket=mutualFundName.indexOf(")", indexOfHyphen);
							Pattern p=Pattern.compile("[0-9]");
							String substring=mutualFundName.substring(indexOfHyphen+1,indexOfClosedBracket+1);
							//System.out.println("SUBSTRING="+substring);
							Matcher matcher=p.matcher(substring);
							if(matcher.find())
							{
								indexOfFirstDigit=matcher.start();
								//System.out.println(")(*Index of first digit="+indexOfFirstDigit);
							}
							if(indexOfFirstDigit!=-1)
							{
								String arnContainingOnlyNumbers=substring.substring(indexOfFirstDigit,substring.indexOf(")"));
								//System.out.println("arnContainignOnlyNumbers="+arnContainingOnlyNumbers);
								if(arnContainingOnlyNumbers.matches("[0-9]+"))
								{
									String newArn="ARN-"+arnContainingOnlyNumbers;
									holding.setArn(newArn);
								}
							}
						}
					}
					else if(mutualFundName.contains("Advisor:"))
					{
						//System.out.println("k-2 Index of advisor != -1");
						int indexOfAdvisor = mutualFundName.indexOf("Advisor:");
						if(indexOfAdvisor!=-1)
						{
							int indexOfColon = mutualFundName.indexOf(":",indexOfAdvisor);
							if(indexOfColon!=-1)
							{
								int indexOfCloseBracket = mutualFundName.indexOf(")", indexOfColon);
								String arn = mutualFundName.substring(indexOfColon+1,indexOfCloseBracket).trim(); 
								holding.setArn(arn);
							}
						}
					}
					holding.setMutualFundName(mutualFundName);
					holding.setCamsCode(camsCode);
					//System.out.println("!@#this mutualfundname should not come");
					//System.out.println("3................"+mutualFundName);
				}
				else if(demo[k - 3].substring(0, demo[k - 3].indexOf("-") + 1).matches("[a-zA-Z0-9]+[-]"))
				{
					//System.out.println("!@#$5");
					int camsCodeEndIndex = demo[k - 3].indexOf("-");
					
					//camsCode might contains '-' so we have to check again for '-' for next 2 chars
					//for eg FFEF-G this one is real ,FFEF-GRE-L&T - this one is made up hence dummy
					String nextTwoCharAfterHyphenContainsAnotherHyphen=demo[k-3].substring(camsCodeEndIndex+1,(camsCodeEndIndex+1)+4);
					if(nextTwoCharAfterHyphenContainsAnotherHyphen.contains("-"))
					{
						int otherCamsCodeIndex=nextTwoCharAfterHyphenContainsAnotherHyphen.indexOf("-");
						camsCodeEndIndex+=(otherCamsCodeIndex+1);
					}
					
					String camsCode = demo[k - 3].substring(0, camsCodeEndIndex);
					String mutualFundName = demo[k - 3].substring(camsCodeEndIndex + 1);
					//as soon as we saw 'ARN' look for '-[0-9]+)' in concatenation of all the lines from demo[k-3] to demo[k-1]
					String actualMutualfundNameInPdf=mutualFundName+demo[k-2]+demo[k-1];
					//System.out.println("ActualMutualfundName()*."+actualMutualfundNameInPdf);
					if(actualMutualfundNameInPdf.contains("ARN"))
					{
						//System.out.println("New String!@# : ");
						int indexOfARN=actualMutualfundNameInPdf.indexOf("ARN");
						int indexOfHyphen=actualMutualfundNameInPdf.indexOf("-", indexOfARN);
						//now we have index of '-' which comes after 'ARN'. So now we look for '[0-9]+)' pattern.
						if(indexOfHyphen!=-1)
						{
							//System.out.println("Index of hyphen="+indexOfHyphen+" indexOfARN="+indexOfARN);
							//get index of first occurrence of digit in ARN no.
							int indexOfFirstDigit=-1;
							int indexOfClosedBracket=actualMutualfundNameInPdf.indexOf(")", indexOfHyphen);
							Pattern p=Pattern.compile("[0-9]");
							String substring=actualMutualfundNameInPdf.substring(indexOfHyphen+1,indexOfClosedBracket+1);
							//System.out.println("SUBSTRING="+substring);
							Matcher matcher=p.matcher(substring);
							if(matcher.find())
							{
								indexOfFirstDigit=matcher.start();
								//System.out.println(")(*Index of first digit="+indexOfFirstDigit);
							}
							if(indexOfFirstDigit!=-1)
							{
								String arnContainingOnlyNumbers=substring.substring(indexOfFirstDigit,substring.indexOf(")"));
								if(arnContainingOnlyNumbers.matches("[0-9]+"))
								{
									String newArn="ARN-"+arnContainingOnlyNumbers;
									holding.setArn(newArn);
								}
							}
						}
					}
					holding.setMutualFundName(mutualFundName);
					holding.setCamsCode(camsCode);
					//System.out.println("this mutualfundname should not come but came camsCode = "+camsCode+" mfName="+mutualFundName);
				}
			}
			if(demo[k].indexOf("(INR) (INR)") != -1)
			{
				if(openingFlag == true)
				{
					if(demo[k + 1].length() > 11 && demo[k + 1].substring(0, 11).matches("[0-9]{2}-[a-zA-Z]{3}-[0-9]{4}"))
					{
						//setting flag true after this we 
						flag = true;
					}
					else if(demo[k + 1].length() > 11 && demo[k + 1].substring(0, 11).matches("[0-9]{1,2}/([0-9]{1,2})/[0-9]{4}"))
					{
						flag = true;
					}
					else if(demo[k + 1].substring(0, demo[k + 1].length() - 1).matches("[0-9,]+[/.][0-9]+"))
					{
						flag = true;
					}
					else
					{
						flag = false;
					}
				}
				else
				{
					flag = false;
				}
			}
		}
		int start = 0;
		// below for loop helps to find number of transactions under one holding/Mutual Fund
		// condition:number of transactions under one holding might be different here since different parts(date,amount etc) might not be in one transaction
		int lol = 0;
		for (int j = 0; j < number.length; j++)
		{
			//to seee all transaction uncomment this line.
			// System.out.println("number[" + j + "] = " + number[j]);
		}
		String transaction1[] = new String[transaction.length];
		int index = 0;
		String append = null;
		int j = 0;
		
		int k = 0, q = 0;
		int indexM = 0;
		String cut[];
		StringBuffer sb;
		indexOfNumber = 0;
		numberOfTransaction = number[0];
		int count = 0;// counts total transactions under one mf then set to 0 for next mutualfund
		int indexOfTransaction = 0;
		// Iterator itr = holdingsList.iterator();
		/*while (itr.hasNext())
		{
			Holdings hol = (Holdings) itr.next();
			System.out.println(hol.getMutualFundName());
			System.out.println(hol.getClosingUnits() + "\t\t" + hol.getFolioNumber());
		}*/
		while (j < number.length && number[j] != -1)
		{
			Holdings h = holdingsList.get(j);
			List<String> txList = new ArrayList<String>();
			int l = 0;
			for (l = 0; l < number[j]; l++)
			{
				//System.out.println("!@#$#@!"+transaction[indexOfTransaction + l]);
				txList.add(transaction[indexOfTransaction + l]);
			}
			//System.out.println("h.getMutualFundName()........................................"+h.getMutualFundName());
			// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5");
			//System.out.println("folio ="+h.getFolioNumberString());
			List<Transactions> transactionList=stringToTransactions(txList,h.getMutualFundName());
			if(transactionList!=null && transactionList.size()>0)
			{
				h.setTransactionsList(transactionList);
			}
			// holdingsList.add(j, h);
			indexOfTransaction = indexOfTransaction + l;
			j++;
		}
		//System.out.println("Holding with no transactions");
		for(int l=0;l<holdingsList.size();l++)
		{
			Holdings holding=holdingsList.get(l);
			//we'll count units using units from each transaction for eg (if pdf is from 1-1-2007-5-1-2017 but user has holding of 5000 before these date then it will be shown only as closing units in holding and not as transactions)
			double units=0;
			if(holding.getTransactionsList()==null || holding.getTransactionsList().size()==0)
			{
				holdingsList.remove(l);
				//System.out.println("missing="+holding.getMutualFundName());
				l--;
			}
			else
			{
				/*System.out.println(holding.getFolioNumber());
				System.out.println(holding.getCamsCode()+"  "+holding.getMutualFundName());*/
				Iterator itr=holding.getTransactionsList().iterator();
				
				while(itr.hasNext())
				{
					Transactions tx=(Transactions)itr.next();
					units+=tx.getUnits();
					units=Math.round(units*10000.0)/10000.0;
					//System.out.println(tx.getDate()+"\t"+tx.getAmount()+"\t"+tx.getUnits()+"\t"+tx.getPrice());
				}
				/*System.out.println("total units="+units);*/
			}
			if(units<0.0)
				units=0.0;
			holding.setClosingUnits(units);
		}
		return holdingsList;
	}

	public List<Transactions> stringToTransactions(List<String> tx,String mfName) throws ParseException
	{
		List<Transactions> transactions = new ArrayList<Transactions>();
		// String datePattern = "[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}";
		// String numRegex = "\\([0-9]+(\\.)[0-9]{2,3}\\)|[0-9]+(\\.)[0-9]{2,3}";
		// Regex for standard transaction
		
		//changes in txPattern1
		//a)sometimes price comes with value as 21.100000321 which has upto 9 decimal places hence replacing {2,4} with {2,9} in price regex of txPattern1
		//changes in txPattern2
		//a)sometimes price comes with value as 21.100000321 which has upto 9 decimal places hence replacing {2,4} with {2,9} in price regex of txPattern2
		//changes in txPattern3
		//a)sometimes price comes with value as 21.100000321 which has upto 9 decimal places hence replacing {2,4} with {2,9} in price regex of txPattern3
		
		////uncomment this for only / format date
		/*
		 * String txPattern1 =
		 * "^[0-9]{1,2}/([a-zA-Z]{3}|[0-9]{1,2})/[0-9]{4}(\\s)+(.)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)*$";
		 * // Regex for transaction where unit balance in missing String txPattern2 =
		 * "^[0-9]{1,2}/([a-zA-Z]{3}|[0-9]{1,2})/[0-9]{4}(\\s)+(.)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		 * // Regex for transaction where text and unit balance in missing String
		 * txPattern3 =
		 * "^[0-9]{1,2}/([a-zA-Z]{3}|[0-9]{1,2})/[0-9]{4}(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		 * String txPattern4 = "^[0-9]+(\\.)[0-9]{1,4}$";
		 */
		
		//uncomment this for only - format date
		
		/*
		String txPattern1 = "^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(.)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)*$";
		// Regex for transaction where unit balance in missing
		String txPattern2 = "^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(.)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		// Regex for transaction where text and unit balance in missing
		String txPattern3 = "^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		String txPattern4 = "^[0-9]+(\\.)[0-9]{1,4}$";
		*/
		
		//for both types of dates
		String txPattern1 = "^(([0-9]{1,2}/([0-9]{1,2})/[0-9]{4})|([0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}))(\\s)+"  //for both format date
				+ "(.)+" //everything
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+" //amount with brackets or without brackets
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+" //units
				+ "(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)+" //price
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)*$";//units
		// Regex for transaction where unit balance in missing
		String txPattern2 = "^(([0-9]{1,2}/([0-9]{1,2})/[0-9]{4})|([0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}))(\\s)+"
				+ "(.)+"
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+"
				+ "(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		// Regex for transaction where text and unit balance in missing
		String txPattern3 = "^(([0-9]{1,2}/([0-9]{1,2})/[0-9]{4})|([0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}))(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+"
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+"//units
				+ "(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";//price
		
		String txPattern4 = "^[0-9]+(\\.)[0-9]{1,4}$";
		
		//for seg date-text-units-units
		String txPattern4_1 = "^(([0-9]{1,2}/([0-9]{1,2})/[0-9]{4})|([0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}))(\\s)" //date
				+ "(.)+" //text
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)+"//units
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)*$";//balance units
		//for seg date-text-units - balance units coming in previous line
		String txPattern4_2 = "^(([0-9]{1,2}/([0-9]{1,2})/[0-9]{4})|([0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}))(\\s)" //date
				+ "(.)+" //text
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)*$";//balance units
		String txPattern4_3_1 =  "^(.)+" //text
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)*$";// units
		String txPattern4_3_2 =  "^(([0-9]{1,2}/([0-9]{1,2})/[0-9]{4})|([0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}))(\\s)"  //date
				+ "(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})(\\s)*$";//balance units
		
		try
		{
			String unitBalanceFromPreviousLine=null;
			int transactionIndex=0;
			Iterator<String> txI = tx.iterator();
			while (txI.hasNext())
			{
				String amount = "", price = "", units = "", date = "", content = "", unitBalanceForCurrentTransaction="";
				double amt = 0, p = 0, u = 0,  uB=0;
				Date txDate = null;
				String tr = txI.next();
				
				//System.out.println(tr+"#@!");
				tr = tr.replaceAll(",", "");
				content="";
				//System.out.println("transa ::::::" + tr);
				boolean flag = false;
				boolean div_payout_flag=false;
				boolean seg_units_creation=false;
				boolean seg_redemption=false;
				
				if(tr.matches(txPattern1))
				{
					// Check last 4 value, ignore 4th, take other 3
					//System.out.println(tr + " - Standard pattern");
					unitBalanceForCurrentTransaction = tr.substring(tr.lastIndexOf(" "),tr.length());
					tr = tr.substring(0, tr.lastIndexOf(" "));
					price = tr.substring(tr.lastIndexOf(" "), tr.length());
					tr = tr.substring(0, tr.lastIndexOf(" "));
					units = tr.substring(tr.lastIndexOf(" "), tr.length());
					tr = tr.substring(0, tr.lastIndexOf(" "));
					amount = tr.substring(tr.lastIndexOf(" "), tr.length());
					tr=tr.substring(0,tr.lastIndexOf(" "));
					content = tr.substring(tr.indexOf(" ")+1,tr.length());
					date = tr.substring(0, 11);
					flag = true;
				}
				else if(tr.matches(txPattern2))
				{
					// Check last 3 value
					//System.out.println(tr + "!@#$%^&* - Unit balance missing");
					
					if(tr.substring(12, (12+11)).equals("Bonus Units"))
					{
						//System.out.println("Indside Bonus Points.");
						tr=tr.substring(0, tr.lastIndexOf(" "));
						units = tr.substring(tr.lastIndexOf(" "), tr.length());
						date = tr.substring(0, 11);
						/*System.out.print("Units="+units+" date="+date);*/
						price=".1";
						amount=String.valueOf(Double.valueOf(units)*Double.valueOf(price));
						/*System.out.println("price="+price+" amount="+amount);*/
						flag = true;
					}
					else
					{
						price = tr.substring(tr.lastIndexOf(" "), tr.length());
						tr = tr.substring(0, tr.lastIndexOf(" "));
						units = tr.substring(tr.lastIndexOf(" "), tr.length());
						tr = tr.substring(0, tr.lastIndexOf(" "));
						amount = tr.substring(tr.lastIndexOf(" "), tr.length());
						tr = tr.substring(0, tr.lastIndexOf(" "));
						if(tr.indexOf(" ")==-1)
						{
							//here comes when line containing transaction part goes in previousline
							String previousTransaction = tx.get(transactionIndex-1);
							content = previousTransaction.substring(0,previousTransaction.lastIndexOf(" "));
						}else
						{
							content = tr.substring(tr.indexOf(" "),tr.length());
						}
						date = tr.substring(0, 11);
						flag = true;
					}
					if(unitBalanceFromPreviousLine!=null)
					{
						unitBalanceForCurrentTransaction = unitBalanceFromPreviousLine;
						unitBalanceFromPreviousLine=null;
					}
				}
				else if(tr.matches(txPattern3))
				{
					// Check last 3 value
					//System.out.println(tr + " - Text and Unit balance missing");
					price = tr.substring(tr.lastIndexOf(" "), tr.length());
					tr = tr.substring(0, tr.lastIndexOf(" "));
					units = tr.substring(tr.lastIndexOf(" "), tr.length());
					tr = tr.substring(0, tr.lastIndexOf(" "));
					amount = tr.substring(tr.lastIndexOf(" "), tr.length());
					date = tr.substring(0, 11);
					flag = true;
					String previousTransaction = tx.get(transactionIndex-1).replace(",", ""); 
					
					//System.out.println("Previous Transaction = "+previousTransaction);
					if((previousTransaction.lastIndexOf(" ")!=-1 && previousTransaction.substring(previousTransaction.lastIndexOf(" ")+1,previousTransaction.length()).matches(txPattern4)))
					{
						unitBalanceForCurrentTransaction = previousTransaction.substring(previousTransaction.lastIndexOf(" ")+1,previousTransaction.length());
						content = previousTransaction.substring(0,previousTransaction.lastIndexOf(" "));
						//System.out.println("Unit bal. ="+unitBalanceForCurrentTransaction);
					}
					else if(previousTransaction.matches(txPattern4))
					{
						unitBalanceForCurrentTransaction = previousTransaction;
						content = tr.substring(12,tr.lastIndexOf(" "));
						//System.out.println("Unit bal. ="+unitBalanceForCurrentTransaction);
					}
				}else if(tr.matches(txPattern4_1) && mfName.toLowerCase().contains("segregated")) {
					     // System.out.println("###########################################################################");
						
							// date text units ,balance units
					       //date text amount units- for redemption
							
							seg_units_creation=true;
							//System.out.println("mfName.toLowerCase"+mfName.toLowerCase());
							//System.out.println("tr:"+tr);
							date = tr.substring(0, 11);
							//content="segregated creation";
							
							
							
							tr = tr.substring(0, tr.lastIndexOf(" "));
							
							if(tr.toLowerCase().contains("redemption"))
							{
								seg_units_creation=false;
								seg_redemption=true;
								tr = tr.substring(tr.lastIndexOf(" "), tr.length());
								amount=tr.substring(tr.lastIndexOf(" "), tr.length());
								units="0";
							}else {
								units = tr.substring(tr.lastIndexOf(" "), tr.length());
							}
							tr = tr.substring(0, tr.lastIndexOf(" "));
							
							content = tr.substring(tr.indexOf(" ")+1,tr.length());
							//System.out.println("content is .............................."+content);
							
							
						
					
				}else if(tr.matches(txPattern4_2) && mfName.toLowerCase().contains("segregated")) {
					//System.out.println("************************************************************"+tr);
						    //txPattern4=1234.8978
						
							
							//date text units
					        //date text amount-and balance units in previous line
							seg_units_creation=true;
						//	System.out.println("mfName.toLowerCase"+mfName.toLowerCase());
							//System.out.println("tr:"+tr);
							date = tr.substring(0, 11);
							//content="segregated creation";
							
							
							if(tr.toLowerCase().contains("redemption"))
							{
								seg_units_creation=false;
								seg_redemption=true;
								//tr = tr.substring(tr.lastIndexOf(" "), tr.length());
								amount=tr.substring(tr.lastIndexOf(" "), tr.length());
								units="0";
							}else {
								units = tr.substring(tr.lastIndexOf(" "), tr.length());
							}
							tr = tr.substring(0, tr.lastIndexOf(" "));
							//tr = tr.substring(0, tr.lastIndexOf(" "));
							
							content = tr.substring(tr.indexOf(" ")+1,tr.length());
							
							//System.out.println("content is .............................."+content);
						
					
				}else if((mfName.toLowerCase().contains("segregated")))
				{
					//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4");
					//System.out.println("heloo........................"+mfName.toLowerCase());
					//when previous line have text and units
					//next line have date and balance units
					if(transactionIndex+1<=tx.size()-1) 
					{
						String nextTransaction = tx.get(transactionIndex+1).replace(",", "");
						System.out.println("nextTransaction is"+nextTransaction);
						System.out.println("currentTransaction is"+tr);
						if(tr.toLowerCase().contains("creation of units - segregated portfolio")) 
						{
							
							System.out.println("currentTransaction.matches(txPattern4_3_1)"+tr.matches(txPattern4_3_1));
							System.out.println("nexttransaction.matches(txPattern4_3_2)"+nextTransaction.matches(txPattern4_3_2));
							if(tr.matches(txPattern4_3_1)  && nextTransaction.matches(txPattern4_3_2) )
							{
								System.out.println("lets see");
								seg_units_creation=true;
								date = nextTransaction.substring(0, 11);
								content="creation of units-segregated portfolio";
								
								units = tr.substring(tr.lastIndexOf(" "), tr.length());
								
								System.out.println("units of segregated"+units);
								System.out.println("date"+date);
								txI.next();
								transactionIndex++;
								
							}
						}
					}
					/*if(transactionIndex>0)
					{
						String previousTransaction = tx.get(transactionIndex-1).replace(",", "");
						System.out.println("previousTransaction is"+previousTransaction);
						if(previousTransaction.toLowerCase().contains("creation of units - segregated portfolio")) 
						{
							
							System.out.println("previousTransaction.matches(txPattern4_3_1)"+previousTransaction.matches(txPattern4_3_1));
							System.out.println("tr.matches(txPattern4_3_2)"+tr.matches(txPattern4_3_2));
							if(previousTransaction.matches(txPattern4_3_1)  && tr.matches(txPattern4_3_2) )
							{
								System.out.println("lets see");
								seg_units_creation=true;
								date = tr.substring(0, 11);
								content="creation of units-segregated portfolio";
								
								units = previousTransaction.substring(tr.lastIndexOf(" "), tr.length());
								
								System.out.println("units of segregated"+units);
								System.out.println("date"+date);
								
							}
						}
					}*/
					
					
					
				}
				
				else if((tr.lastIndexOf(" ")!=-1 && tr.substring(tr.lastIndexOf(" ")+1,tr.length()).matches(txPattern4)) || tr.matches(txPattern4))
				{
					//System.out.println("This is ittttttttt!!!!!!!!...........");
					//when unit balance comes in line above transaction line
					//example 1
					//Sys. Investment (1/1) 47.941
					//transaction 09-Nov-2018 500.00 47.941 10.4295  - Pattern2
					//example 2
					//0.000
					//transaction 10-Dec-2018 Systematic Investment Rejection (1/1) (500.00) (47.663) 10.4904  - Pattern2
					if(tr.lastIndexOf(" ")!=-1)
					{
						unitBalanceFromPreviousLine = tr.substring(tr.lastIndexOf(" ")+1,tr.length());
					}
					else
					{
						unitBalanceFromPreviousLine = tr;
					}
					if((tr.toLowerCase().contains("div") || tr.toLowerCase().contains("dividend")) && (tr.toLowerCase().contains("paid") || tr.toLowerCase().contains("payout")))
					{
						div_payout_flag=true;
						content="Dividend Paid";
						amount = tr.substring(tr.lastIndexOf(" "), tr.length());
						date = tr.substring(0, 11);
					}
					
					
					
					
					
				}
				else
				{
					
				}
				// System.out.println(date);
				if(flag == true)
				{
					Transactions transaction = new Transactions();
					transaction.setTransaction(content);
					if(date.contains("/")) 
					{
						txDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
					}else {
						txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
					}
					
					if(amount.contains("("))
					{
						amount = amount.replace("(", "-");
						amount = amount.replace(")", "");
						amt = Double.valueOf(amount);
						units = units.replace("(", "-");
						units = units.replace(")", "");
						u = Double.valueOf(units);
						p = Double.valueOf(price);
						//if(amt<-1 || amt>1)
						{
							transaction.setAmount(amt);
							transaction.setPrice(p);
							transaction.setUnits(u);
							transaction.setDate(txDate);
							transaction.setType("2");
							transactions.add(transaction);
						}
					}
					else
					{
						amt = Double.valueOf(amount);
						u = Double.valueOf(units);
						p = Double.valueOf(price);
						//uB = Double.valueOf(unitBalanceForCurrentTransaction);
						//if(amt>1 || amt<-1)
						{
							transaction.setAmount(amt);
							transaction.setPrice(p);
							transaction.setUnits(u);
							transaction.setDate(txDate);
							transaction.setType("1");
							transactions.add(transaction);
						}
					}
					//Below arn code should stay only on FUNDEXPERT
					Pattern pattern=Pattern.compile("((ARN[\\-])([0-9]+))(([\\/][a-zA-Z0-9]+))|((ARN[\\-])([0-9]+))");
					Matcher m=pattern.matcher(content);
					//System.out.println(m.groupCount());
					while(m.find())
					{
						/*System.out.println("Match found for ARN : "+m.group(3)+"  "+m.group(8));*/
						if(m.group(0).matches("(ARN[\\-][0-9]+)"))
						{
							transaction.setArn(m.group(3));
						}
						else if(m.group(1).matches("(ARN[\\-][0-9]+)"))
						{
							transaction.setArn(m.group(8));
						}
						//System.out.println(m.group(0)+" "+m.group(1)+" "+m.group(2)+" $ "+m.group(3)+" $ !"+m.group(4)+"@ $  "+m.group(5)+" "+m.group(6)+" "+m.group(7)+" "+m.group(8));
					}
					//System.out.println(txDate + "\t\t" + content + "\t\t" + amt + "\t\t\t" + u + "\t\t\t" + p);
				}
				else if(div_payout_flag)
				{
					Transactions transaction = new Transactions();
					transaction.setTransaction(content);
					//txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
					if(date.contains("/")) 
					{
						txDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
					}else {
						txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
					}
					amt = Double.valueOf(amount);
					transaction.setAmount(-amt);
					transaction.setPrice(0.0);
					transaction.setUnits(0.0);
					transaction.setDate(txDate);
					transaction.setType("2");
					transactions.add(transaction);
				}
				else if(seg_units_creation)
				{
					Transactions transaction = new Transactions();
					transaction.setTransaction(content);
					//txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
					if(date.contains("/")) 
					{
						txDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
					}else {
						txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
					}
					 
					transaction.setAmount(0.0);
					transaction.setPrice(0.0);
					//System.out.println("else if"+units);
					transaction.setUnits(Double.valueOf(units));
					transaction.setDate(txDate);
					transaction.setType("1");
					transactions.add(transaction);
				}else if(seg_redemption) {
					Transactions transaction = new Transactions();
					transaction.setTransaction(content);
					if(date.contains("/")) 
					{
						txDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
					}else {
						txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
					}
					amt=0;
					if(amount.contains("("))
					{
						amount = amount.replace("(", "-");
						amount = amount.replace(")", "");
						amt = Double.valueOf(amount);
					}
					
					transaction.setAmount(amt);
					//transaction.setAmount(0.0);
					transaction.setPrice(0.0);
					//System.out.println("else if"+units);
					transaction.setUnits(Double.valueOf(units));
					transaction.setDate(txDate);
					transaction.setType("2");
					transactions.add(transaction);
					
				}
				transactionIndex++;
			}
			return transactions;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	public List getKarvyHoldingsList(String path,String password) throws BadPasswordException, InvalidPdfException, Exception,StringIndexOutOfBoundsException
	{
		int n = 0;
		String myLine = "", demo[];
		String name = null;
		String tempFolio[] = new String[2];
		PdfReader reader = null;
		InputStream is=null;
		Date statementDate=null;
		try
		{
			File f=new File(path);
			is=new FileInputStream(f);
			reader = new PdfReader(is, password.getBytes());
			//reader.unethicalreading = true;
			PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);

			n = reader.getNumberOfPages();
			for(int i = 1; i <= n; i++) {
			        // Extract content of each page
			        String content= pdfTextExtractor.getTextFromPage(i, true);
				myLine = myLine + content;
				//System.out.println("Content of Page"+contentOfPage);
      			}

			System.out.println(myLine);
			reader.close();
		}
		catch (InvalidPdfException ipe)
		{
			ipe.printStackTrace();
			throw new InvalidPdfException("Not a pdf file");
		}
		catch (BadPasswordException bpe)
		{
			bpe.printStackTrace();
			throw new BadPasswordException("Please upload CAS linked with PAN.");
		}
		finally
		{
			if(is!=null)
			{
				is.close();
			}
		}
		Matcher m = Pattern.compile("\r\n|\r|\n").matcher(myLine);
		// demo array stores individual lines from myLine
		demo = myLine.split("\r|\n|\r\n|\n\r");
		//if(!demo[0].contains("Consolidated Account Statement"))
			//throw new FundexpertException("Please upload CAS statements only.");
		
		
		// i the index of transaction
		int i = 0;
		// openingFlag is set to true when it sees Opening as word(to know tranasactions are from next line)
		// flag is set to true when we are ready to process transactions after seeing openingFlag and false when we see valuation
		boolean flag = false, openingFlag = false;
		String transaction[] = new String[20];
		int b = 0;
		int add = 0;
		// counts total transactions in the pdf
		int totalTransaction = 0;
		// numberOfTransaction counts number of transaction under one folio and then stores it in number array
		int numberOfTransaction = 0;
		int index1 = 0;
		// the size to number is given on assumption but it will never go out of memory
		number = new int[demo.length / 2];
		// value of indexNumber tells current index of holding in pdf
		int indexOfNumber = 0;
		for (i = 0; i < number.length; i++)
		{
			number[i] = -1;
		}
		i = 0;
		// System.out.println("after pdf=" + myLine);
		for (int l = 0; l < demo.length; l++)
		{
			//System.out.println("demo[" + l + "]=" + demo[l]);
		}
		for (int k = 0; k < demo.length; k++)
		{
			if(k==0 && demo[k].equals("Consolidated Account Statement"))
			{
				int indexOfStatementDate=k+1;
				String lineAtStatementDate=demo[indexOfStatementDate];
				int indexOfTO=lineAtStatementDate.indexOf("To");
				if(indexOfTO>0)
				{
					String date=lineAtStatementDate.substring(indexOfTO+3);
					System.out.println("StatementDate ="+date);
					statementDate=new SimpleDateFormat("dd-MMM-yyyy").parse(date);
				}
				
			}
			if(openingFlag == true && demo[k].indexOf("*** No transactions ") != -1)
			{
				System.out.println("going here");
				continue;
			}
			if((demo[k].length() > 10 && demo[k].substring(0, 9).equals("Valuation")) || (demo[k].length() == 11 && demo[k].substring(0, 4).equals("Page")))
			{
				b++;
			}
			if(openingFlag && (demo[k].indexOf("Valuation") != -1 || demo[k].indexOf("Page") != -1))
			{
				if(demo[k].indexOf("Valuation") != -1)
				{
					// below code is to fetch closing bal,nav val,valuation amt
					int q = 0;// q describes last 4 columns of pdf i.e amount,price etc
					int fromIndex = 0;
					// when closing,valutaion,nav values are all in one line in demo use below if()
					if(demo[k].indexOf("Closing") != -1)
					{
						while (q != 1)
						{
							fromIndex = demo[k].indexOf(":", fromIndex);
							Matcher mo = Pattern.compile("[0-9]+[,]*[0-9]*[/.][0-9]+").matcher(demo[k].substring(fromIndex, demo[k].length()));
							if(mo.find())
							{
								if(q == 0)
								{
									if(mo.group(0).indexOf(',') != -1)
									{
										StringBuilder sb = new StringBuilder(mo.group(0));
										sb.deleteCharAt(mo.group(0).indexOf(','));
										// the holding object is created below
										holding.setClosingUnits(Double.valueOf(sb.toString()));
									}
									else
									{
										holding.setClosingUnits(Double.valueOf(mo.group(0)));
									}
								}
							}
							q++;
						}
					}
					// this else solves when valuation comes in k line and closing,nav comes in k+1
					else
					{
						String merge = demo[k + 1] + " " + demo[k];
						while (q != 1)
						{
							fromIndex = merge.indexOf(":", fromIndex);
							Matcher mo = Pattern.compile("[0-9]+[,]*[0-9]*[/.][0-9]+").matcher(merge.substring(fromIndex, merge.length()));
							if(mo.find())
							{
								if(q == 0)
								{
									if(mo.group(0).indexOf(',') != -1)
									{
										StringBuilder sb = new StringBuilder(mo.group(0));
										sb.deleteCharAt(mo.group(0).indexOf(','));
										holding.setClosingUnits(Double.valueOf(sb.toString()));
									}
									else
									{
										holding.setClosingUnits(Double.valueOf(mo.group(0)));
									}
								}
							}
							q++;
						}
					}
					// below code is to help with differentiating only transactions with other part in demo array by setting flag=false
					flag = false;
					openingFlag = false;
					number[indexOfNumber] = numberOfTransaction;
					indexOfNumber++;
					// now a add the holding to list
					holdingsList.add(holding);
				}
				else
				{
					flag = false;
				}
				b++;
			}
			if(flag == true)
			{
				numberOfTransaction++;
				// below if is used in case if transaction index goes beyond its length
				if(i > transaction.length - 1)
				{
					add = transaction.length;
					String temporary[] = new String[add];
					temporary = transaction;
					transaction = new String[transaction.length + 20];
					System.arraycopy(temporary, 0, transaction, 0, temporary.length);
				}
				// transaction achieved from demo
				transaction[i] = demo[k];
				i++;
			}
			if(demo[k].length() > 10 && (demo[k].substring(0, 7).equals("Opening")||demo[k].substring(1, 8).equals("Opening")))
			{
				// as soon as we get OpeningFlag==true we get new holdings
				holding = new Holdings();
				holding.setStatementDate(statementDate);
				openingFlag = true;
				// after seeing flag==true store all transactions in different string say transaction[i]=demo[k]
				flag = true;
				numberOfTransaction = 0;
				// folio number occurs two lines above 'opening' word in demo hence [k-2]
				// below if else find folioNumber considering different use cases as per 'demo'
				//below if is modified as per karvy pdf parsing requirement since cams gives Folio No: 17714203573 / 0 
				//but karvy gives 17714203573/ 0
				if(demo[k - 2].matches("(.)+[ ][0-9]+[/][ ][0-9]+(.)+") || demo[k - 2].matches("(.)+[ ][0-9]+(.)+"))
				{
					Matcher match1 = Pattern.compile("[ ][0-9]+[/][ ][0-9]+").matcher(demo[k - 2]);
					if(match1.find())
					{
						tempFolio = match1.group(0).split("/ ");
						if(tempFolio[1].trim().equals("0"))
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim()));
							holding.setFolioNumberString(tempFolio[0].trim());
						}
						else
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim() + tempFolio[1].trim()));
							holding.setFolioNumberString((tempFolio[0].trim() + "/" + tempFolio[1].trim()).replaceAll(" ", ""));
						}
					}
					//chances of going in this else is less as compare to if case
					else if(demo[k - 2].matches("(.)+[0-9]+(.)+"))
					{
						Matcher match2 = Pattern.compile("[ ][A-Za-z]*[0-9]+[ ]").matcher(demo[k - 2]);
						if(match2.find())
						{
							// holding.setFolioNumber(Long.valueOf(match2.group(0).trim()));
							holding.setFolioNumberString(match2.group(0).trim());
						}
					}
				}
				else
				{
					
					int backwards = k;
					while (!demo[backwards].substring(0, 5).equals("Folio"))
					{
						backwards--;
					}
					Matcher match = Pattern.compile("[ ][.0-9]+/[ ][0-9]+").matcher(demo[backwards]);
					if(match.find())
					{
						// after changes
						tempFolio = match.group(0).split("/ ");
						if(tempFolio[1].trim().equals("0"))
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim()));
							holding.setFolioNumberString(tempFolio[0].trim());
						}
						else
						{
							holding.setFolioNumber(Long.valueOf(tempFolio[0].trim() + tempFolio[1].trim()));
							holding.setFolioNumberString((tempFolio[0].trim() + "/" + tempFolio[1].trim()).replaceAll(" ", ""));
						}
						// holding.setFolioNumberString(match.group(0).trim().replaceAll(" ", ""));
					}
					else if(demo[backwards].matches("(.)+[0-9]+(.)+"))
					{
						Matcher match2 = Pattern.compile("[ ][A-Za-z]*[0-9]+[ ]").matcher(demo[backwards]);
						if(match2.find())
						{
							// holding.setFolioNumber(Long.valueOf(match2.group(0).trim()));
							holding.setFolioNumberString(match2.group(0).trim());
						}
						else
							throw new Exception("No Match of FolioNumber");
					}
					//System.out.println("backwards="+backwards+" k="+k);
				}
				// when the page change and opening is true and opening unit balance comes after "(INR) (INR)"
				if(demo[k - 1].contains("(INR) (INR)"))
				{
					//System.out.println("!@#$");
					if(demo[k - 7].substring(0, demo[k - 7].indexOf("-") + 1).matches("[a-zA-Z0-9]+[-]"))
					{
						//System.out.println("!@#$1");
						int camsCodeEndIndex = demo[k - 7].indexOf("-");
						String camsCode = demo[k - 7].substring(0, camsCodeEndIndex);
						String mutualFundName = demo[k - 7].substring(camsCodeEndIndex + 1);
						if(mutualFundName.contains("ARN"))
						{
							int indexOfARN=mutualFundName.indexOf("ARN");
							int indexOfClosedBracket=mutualFundName.indexOf(")", indexOfARN);
							if(indexOfClosedBracket != -1)
							{
								String arn=mutualFundName.substring(indexOfARN,indexOfClosedBracket);
								//System.out.println(mutualFundName+" : ARN = "+arn);
								holding.setArn(arn);
							}
						}
						holding.setCamsCode(camsCode.replaceAll(" ", ""));
						holding.setMutualFundName(mutualFundName);
						
						//System.out.println("1."+camsCode + "-" + mutualFundName);
					}
					else
					{
						//System.out.println("!@#$2");
						int backwards = k - 7;
						while (!demo[backwards].substring(0, demo[backwards].indexOf("-") + 1).matches("[a-zA-Z0-9]+[-]"))
						{
							backwards--;
						}
						int camsCodeEndIndex = demo[backwards].indexOf("-");
						String camsCode = demo[k - 1].substring(0, camsCodeEndIndex);
						String mutualFundName = demo[backwards].substring(camsCodeEndIndex + 1);
						while (backwards != k - 7)
						{
							backwards++;
							mutualFundName = mutualFundName + demo[backwards];
						}
					}
				}
				//in below else if first condition satisfy 'camsCode-Mutualfund name' starting from index 0 where mutualfund name should start with [a-z,A-z]
				//2nd condition states that if demo[k-1] has pattern like '[a-zA-z]-Mutualfund name' starting from index 0 then mutualfund name should not start with name '[0-9]' which is the case when 'ARN-234324)' is at line demo[k-1] and if it starts with [0-9]+ then check if demo[k-1] has 'ARN'
				else if(demo[k - 1].substring(0, demo[k - 1].indexOf("-") + 1).matches("[a-zA-z0-9\\s]+[-]") && ((demo[k-1].substring(demo[k - 1].indexOf("-"),demo[k - 1].indexOf("-")+2).matches("[/-][0-9]") && !demo[k-1].substring(0,demo[k - 1].indexOf("-")).toLowerCase().contains("arn")) || (!demo[k-1].substring(demo[k - 1].indexOf("-"),demo[k - 1].indexOf("-")+2).matches("[/-][0-9]"))))
				{
					int camsCodeEndIndex = demo[k - 1].indexOf("-");
					String camsCode = demo[k - 1].substring(0, camsCodeEndIndex);
					String mutualFundName = demo[k - 1].substring(camsCodeEndIndex + 1);
					if(mutualFundName.contains("ARN"))
					{
						int indexOfARN=mutualFundName.indexOf("ARN");
						int indexOfClosedBracket=mutualFundName.indexOf(")", indexOfARN);
						if(indexOfClosedBracket!=-1)
						{
							String substring=mutualFundName.substring(indexOfARN,indexOfClosedBracket+1);
							//System.out.println("substring="+substring);
							Pattern p=Pattern.compile("[0-9]");
							Matcher matcher=p.matcher(substring);
							int indexOfFirstDigit=0;
							if(matcher.find())
							{
								indexOfFirstDigit=matcher.start();
								//System.out.println(")(*Index of first digit="+indexOfFirstDigit);
							}
							if(indexOfFirstDigit!=-1)
							{
								//System.out.println("substring="+substring+" indexOfFirstDigit="+indexOfFirstDigit+" indexOf )="+substring.indexOf(")"));
								String arnContainingOnlyNumbers=substring.substring(indexOfFirstDigit,substring.indexOf(")"));
								if(arnContainingOnlyNumbers.matches("[0-9]+"))
								{
									String newArn="ARN-"+arnContainingOnlyNumbers;
									holding.setArn(newArn);
								}
							}
						}
					}
					else if(mutualFundName.contains("Direct"))
					{
						if(mutualFundName.contains("Advisor:"))
						{
							int indexOfARN=mutualFundName.indexOf("Advisor:");
							int indexOfClosedBracket=mutualFundName.indexOf(")",indexOfARN);
							if(indexOfClosedBracket!=-1)
							{
								int indexOfActualArn=indexOfARN+8;//8 represent length of 'Advisor:'
								String arn=mutualFundName.substring(indexOfActualArn,indexOfClosedBracket).trim();
								holding.setArn(arn);
							}
						}
					}
					//holding.setCamsCode(camsCode);
					holding.setCamsCode(camsCode.replaceAll(" ", ""));
					//System.out.println("camsCode="+camsCode);
					holding.setMutualFundName(mutualFundName);
					//System.out.println("2."+camsCode + "-" + mutualFundName);

				}
				else if(demo[k - 2].substring(0, demo[k - 2].indexOf("-") + 1).matches("[a-zA-z0-9]+[-]"))
				{
					//System.out.println("!@#$4");
					/*int camsCodeEndIndex = demo[k - 2].indexOf("-");
					String camsCode = demo[k - 2].substring(0, camsCodeEndIndex);
					String mutualFundName = demo[k - 2].substring(camsCodeEndIndex + 1) + demo[k - 1];*/
					//System.out.println("!@#this mutualfundname should not come");
					
					int camsCodeEndIndex = demo[k - 2].indexOf("-");
					String camsCode = demo[k - 2].substring(0, camsCodeEndIndex);
					String mutualFundName = demo[k - 2].substring(camsCodeEndIndex + 1);
					//as soon as we saw 'ARN' look for '-[0-9]+)' in concatenation of all the lines from demo[k-3] to demo[k-1]
					String actualMutualfundNameInPdf=mutualFundName+demo[k-1];
					//System.out.println("ActualMutualfundName()*."+actualMutualfundNameInPdf);
					if(actualMutualfundNameInPdf.contains("ARN"))
					{
						//System.out.println("New String!@# : ");
						int indexOfARN=actualMutualfundNameInPdf.indexOf("ARN");
						int indexOfHyphen=actualMutualfundNameInPdf.indexOf("-", indexOfARN);
						//now we have index of '-' which comes after 'ARN'. So now we look for '[0-9]+)' pattern.
						if(indexOfHyphen!=-1)
						{
							//System.out.println("Index of hyphen="+indexOfHyphen+" indexOfARN="+indexOfARN);
							//get index of first occurrence of digit in ARN no.
							int indexOfFirstDigit=-1;
							int indexOfClosedBracket=actualMutualfundNameInPdf.indexOf(")", indexOfHyphen);
							Pattern p=Pattern.compile("[0-9]");
							String substring=actualMutualfundNameInPdf.substring(indexOfHyphen+1,indexOfClosedBracket+1);
							//System.out.println("SUBSTRING="+substring);
							Matcher matcher=p.matcher(substring);
							if(matcher.find())
							{
								indexOfFirstDigit=matcher.start();
								//System.out.println(")(*Index of first digit="+indexOfFirstDigit);
							}
							if(indexOfFirstDigit!=-1)
							{
								String arnContainingOnlyNumbers=substring.substring(indexOfFirstDigit,substring.indexOf(")"));
								if(arnContainingOnlyNumbers.matches("[0-9]+"))
								{
									String newArn="ARN-"+arnContainingOnlyNumbers;
									holding.setArn(newArn);
								}
							}
						}
					}
					else if(actualMutualfundNameInPdf.contains("Direct"))
					{
						if(actualMutualfundNameInPdf.contains("Advisor:"))
						{
							int indexOfARN=actualMutualfundNameInPdf.indexOf("Advisor:");
							int indexOfClosedBracket=actualMutualfundNameInPdf.indexOf(")",indexOfARN);
							if(indexOfClosedBracket!=-1)
							{
								int indexOfActualArn=indexOfARN+8;//8 represent length of 'Advisor:'
								String arn=actualMutualfundNameInPdf.substring(indexOfActualArn,indexOfClosedBracket).trim();
								holding.setArn(arn);
							}
						}
					}
					holding.setMutualFundName(mutualFundName);
					//holding.setCamsCode(camsCode);
					holding.setCamsCode(camsCode.replaceAll(" ", ""));
				}
				else if(demo[k - 3].substring(0, demo[k - 3].indexOf("-") + 1).matches("[a-zA-Z0-9]+[-]"))
				{
					//System.out.println("!@#$5");
					int camsCodeEndIndex = demo[k - 3].indexOf("-");
					String camsCode = demo[k - 3].substring(0, camsCodeEndIndex);
					String mutualFundName = demo[k - 3].substring(camsCodeEndIndex + 1);
					//as soon as we saw 'ARN' look for '-[0-9]+)' in concatenation of all the lines from demo[k-3] to demo[k-1]
					String actualMutualfundNameInPdf=mutualFundName+demo[k-2]+demo[k-1];
					//System.out.println("ActualMutualfundName()*."+actualMutualfundNameInPdf);
					if(actualMutualfundNameInPdf.contains("ARN"))
					{
						//System.out.println("New String!@# : ");
						int indexOfARN=actualMutualfundNameInPdf.indexOf("ARN");
						int indexOfHyphen=actualMutualfundNameInPdf.indexOf("-", indexOfARN);
						//now we have index of '-' which comes after 'ARN'. So now we look for '[0-9]+)' pattern.
						if(indexOfHyphen!=-1)
						{
							//System.out.println("Index of hyphen="+indexOfHyphen+" indexOfARN="+indexOfARN);
							//get index of first occurrence of digit in ARN no.
							int indexOfFirstDigit=-1;
							int indexOfClosedBracket=actualMutualfundNameInPdf.indexOf(")", indexOfHyphen);
							Pattern p=Pattern.compile("[0-9]");
							String substring=actualMutualfundNameInPdf.substring(indexOfHyphen+1,indexOfClosedBracket+1);
							//System.out.println("SUBSTRING="+substring);
							Matcher matcher=p.matcher(substring);
							if(matcher.find())
							{
								indexOfFirstDigit=matcher.start();
								//System.out.println(")(*Index of first digit="+indexOfFirstDigit);
							}
							if(indexOfFirstDigit!=-1)
							{
								String arnContainingOnlyNumbers=substring.substring(indexOfFirstDigit,substring.indexOf(")"));
								if(arnContainingOnlyNumbers.matches("[0-9]+"))
								{
									String newArn="ARN-"+arnContainingOnlyNumbers;
									holding.setArn(newArn);
								}
							}
						}
					}
					holding.setMutualFundName(mutualFundName);
					//holding.setCamsCode(camsCode);
					holding.setCamsCode(camsCode.replaceAll(" ", ""));
					//System.out.println("this mutualfundname should not come but came camsCode = "+camsCode+" mfName="+mutualFundName);
				}
			}
			if(demo[k].indexOf("(INR) (INR)") != -1)
			{
				if(openingFlag == true)
				{
					if(demo[k + 2].length() >= 11 && demo[k + 2].trim().substring(0, 11).matches("[0-9]{2}-[a-zA-Z]{3}-[0-9]{4}"))
					{
						flag=true;
					}
					else if(demo[k + 1].length() > 11 && demo[k + 1].trim().substring(0, 11).matches("[0-9]{2}-[a-zA-Z]{3}-[0-9]{4}"))
					{
						flag = true;
					}
					else if(demo[k + 1].trim().substring(0, demo[k + 1].length() - 1).matches("[0-9,]+[/.][0-9]+"))
					{
						flag = true;
					}
					else
					{
						flag = false;
					}
				}
				else
				{
					flag = false;
				}
			}
		}
		int start = 0;
		// below for loop helps to find number of transactions under one holding/Mutual Fund
		// condition:number of transactions under one holding might be different here since different parts(date,amount etc) might not be in one transaction
		int lol = 0;
		int sum=0;
		/*for (int j = 0; j < number.length; j++)
		{
			 //System.out.println("number[" + j + "] = " + number[j]);
			 
		}*/
		/*for(int l=0;l<transaction.length;l++){
			System.out.println("Transaction  : "+transaction[l]);
		}*/
		/*for (int j = 0; j < 7; j++)
		{
			 //System.out.println("number[" + j + "] = " + number[j]);
			 
			sum+=number[j];
		}
		for(int ik=sum;ik<sum+number[8];ik++)
			 System.out.println("O T ="+transaction[ik]);*/
		String transaction1[] = new String[transaction.length];
		int index = 0;
		String append = null;
		int j = 0;
		int k = 0, q = 0;
		int indexM = 0;
		String cut[];
		StringBuffer sb;
		indexOfNumber = 0;
		numberOfTransaction = number[0];
		int count = 0;// counts total transactions under one mf then set to 0 for next mutualfund
		int indexOfTransaction = 0;
		// Iterator itr = holdingsList.iterator();
		/*while (itr.hasNext())
		{
			Holdings hol = (Holdings) itr.next();
			System.out.println(hol.getMutualFundName());
			System.out.println(hol.getClosingUnits() + "\t\t" + hol.getFolioNumber());
		}*/
		while (j < number.length && number[j] != -1)
		{
			Holdings h = holdingsList.get(j);
			//System.out.println(h.getFolioNumber()+"----"+h.getFolioNumberString());
			//System.out.println(h.getCamsCode()+"-"+h.getMutualFundName());
			List<String> txList = new ArrayList<String>();
			int l = 0;
			for (l = 0; l < number[j]; l++)
			{
				//System.out.println("!@#$#@!"+transaction[indexOfTransaction + l]);
				String tempTran=transaction[indexOfTransaction + l];
				//System.out.println(tempTran);
				txList.add(transaction[indexOfTransaction + l]);
			}
			//System.out.println(h.getClosingUnits());
			//System.out.println("\n");
			// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5");
			List<Transactions> transactionList=stringToTransactionsForKarvy(txList);
			if(transactionList!=null && transactionList.size()>0)
			{
				h.setTransactionsList(transactionList);
			}
			// holdingsList.add(j, h);
			indexOfTransaction = indexOfTransaction + l;
			j++;
		}
		//System.out.println("Holding with no transactions");
		for(int l=0;l<holdingsList.size();l++)
		{
			Holdings holding=holdingsList.get(l);
			//we'll count units using units from each transaction for eg (if pdf is from 1-1-2007-5-1-2017 but user has holding of 5000 before these date then it will be shown only as closing units in holding and not as transactions)
			double units=0;
			if(holding.getTransactionsList()==null || holding.getTransactionsList().size()==0)
			{
				holdingsList.remove(l);
				//System.out.println("missing="+holding.getMutualFundName());
				l--;
			}
			else
			{
				//System.out.println("Holding id="+holding.getMutualFundName()+" !@# "+holding.getFolioNumber());
				//System.out.println(holding.getCamsCode()+"  "+holding.getMutualFundName());
				Iterator itr=holding.getTransactionsList().iterator();
				
				while(itr.hasNext())
				{
					Transactions tx=(Transactions)itr.next();
					units+=tx.getUnits();
					//System.out.println(tx.getDate()+"\t"+tx.getAmount()+"\t"+tx.getUnits()+"\t"+tx.getPrice()+" totalUnitsBalance="+units);
				}
				DecimalFormat df=new DecimalFormat(".000000");
				units=Double.valueOf(df.format(units));
				if(units>-.00009 && units<.00009)
					units=0;
				//System.out.println("total units="+units);
			}
			String mfName=holding.getMutualFundName();
			if(mfName!=null)
			{
				int indexOfOpeningBracket=mfName.contains("(Advi")?mfName.indexOf("(Advi"):-1;
				if(indexOfOpeningBracket!=-1)
				{
					holding.setMutualFundName(mfName.substring(0,indexOfOpeningBracket).trim());
				}
				else
				{
					//if '(Advi' is not in mfName string in pdf then we have to explicitly remove 'Registrar' from mfName
					int indexOfRegistrar=mfName.contains("Registrar")?mfName.indexOf("Registrar"):-1;
					if(indexOfRegistrar!=-1)
					{
						holding.setMutualFundName(mfName.substring(0,indexOfRegistrar).trim());
					}
				}
			}
			holding.setClosingUnits(units);
		}
		return holdingsList;
	}
	
	public List<Transactions> stringToTransactionsForKarvy(List<String> tx) throws ParseException
	{
		List<Transactions> transactions = new ArrayList<Transactions>();
		// String datePattern = "[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}";
		// String numRegex = "\\([0-9]+(\\.)[0-9]{2,3}\\)|[0-9]+(\\.)[0-9]{2,3}";
		// Regex for standard transaction
		
		//changes in txPattern1
		//a)sometimes price comes with value as 21.100000321 which has upto 9 decimal places hence replacing {2,4} with {2,9} in price regex of txPattern1
		//changes in txPattern2
		//a)sometimes price comes with value as 21.100000321 which has upto 9 decimal places hence replacing {2,4} with {2,9} in price regex of txPattern2
		//changes in txPattern3
		//a)sometimes price comes with value as 21.100000321 which has upto 9 decimal places hence replacing {2,4} with {2,9} in price regex of txPattern3
		
		String txPattern1 = "^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(.)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)*$";
		// Regex for transaction where unit balance in missing
		String txPattern2 = "^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(.)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		// Regex for transaction where text and unit balance in missing
		String txPattern3 = "^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		
		/*String pp=^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(.)+[0-9]+((\\.)[0-9]{2,4})*(\\s)*$
				^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+[0-9]+((\\.)[0-9]{2,4})*(\\s)*$
				 divi 
				 ^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}
				 ^(.)+[0-9]+((\\.)[0-9]{2,4})**/ 
		//for karvy new type of transaction arrives for eg transaction in two lines as follows
		//05-Mar-2018 80.018 37.4916  - this represent txpattern4 - as date units price
		//Purchase- Systematic 3,000.00 80.018 - this represent txPattern5 as - text amount unitbalance
		//since these txpattern4 and 5 does not satisfy any of the txPatterns1,2,3 we can safely insert it after them
		//TODO used * in txpatern4 for amount
		String txPattern4="^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})";
		String txPattern5="^(.)+(\\s)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+((\\.)[0-9]{2,4})*)$";
		
		//this txPattern is copy of txPattern3 except extra regex added at the end for following transaction
		// 20-Feb-2017 -49000.00 -262.4950 186.67 0.000
		String txPattern6 = "^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)([0-9]+(\\.)[0-9]{2,9})$";
		
		//similar to txPattern4 and txPattern5 exception extra date where second date is the actual date
		//20-Apr-2017 25-Apr-2017 (4,009.204) 34.92 - where 25 april is the actual date
		//*Redemption - ELECTRONIC PAYMENT (140,000.00) 10,215.988 - there is no change compare to txPattern5
		String txPattern7="^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4})";
		String txPattern8="^(.)+(\\s)+(\\([0-9]+((\\.)[0-9]{2,4})*\\)|[0-9]+((\\.)[0-9]{2,4})*|\\-[0-9]+((\\.)[0-9]{2,4})*)(\\s)+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+((\\.)[0-9]{2,4})*)$";
		
		//pattern 9 eg-03-Feb-2021 03-Feb-2021 *** Stamp Duty *** 22.74 (1.929) 2,384.1056 
		String txPattern9="^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)"
				+ "+[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)"
				+"+(.)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		
		String txPattern9_1="^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)"
				+ "+[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,9}\\)|[0-9]+(\\.)[0-9]{2,9})(\\s)*$";
		
		//Sys. Investment (8/240) *** Stamp Duty *** 999.95 98.732 txPattern10example
		// first line txPattern9.1(12-Oct-2020 10-Nov-2020 0.05 10.250 97.5599)and second  txPattern10
		String txPattern10="(.)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)"
				+ "+(\\([0-9]+(\\.)[0-9]{2,4}\\)|[0-9]+(\\.)[0-9]{2,4}|\\-[0-9]+(\\.)[0-9]{2,4})(\\s)*$";
		Iterator<String> txI = tx.iterator();
		int transactionIndex=0;
		while (txI.hasNext())
		{
			String amount = "", price = "", units = "", date = "", content = "";
			double amt = 0, p = 0, u = 0;
			Date txDate = null;
			String tr = txI.next();
			//System.out.println(tr+"#@!");
			tr = tr.replaceAll(",", "");
			tr = tr.trim();
			System.out.println("transaction real=" + tr);
			
			boolean flag = false;
			boolean div_payout_flag=false;
			String nextTransaction="";
			if((transactionIndex+2)<tx.size()) {
				
				nextTransaction=tx.get(transactionIndex+2);
				System.out.println("tx.get(transactionIndex+1)" + tx.get(transactionIndex+1));
				System.out.println("tx.get(transactionIndex+2)" + tx.get(transactionIndex+2));
			}
			
			//System.out.println("nextTransaction is"+nextTransaction);
			/*if(nextTransaction.matches(txPattern10)) {
				System.out.println("matching 10");
			}*/
			if(tr.matches(txPattern9) && tr.contains("*** Stamp Duty ***") && nextTransaction.contains("S T P Out"))
			{
				System.out.println("TxPattern 9 matched");
				String tempTran=tr;
				if(txI.hasNext())
				{
					tr = txI.next();
					tr = tr.replaceAll(",", "");
					//System.out.println("tr for 8="+tr);
					if(tempTran.matches(txPattern9) && tr.contains("S T P Out"))
					{
						//eg-S T P Out (4,596.00) 143.559 for tr(next transaction)
						//eg-03-Feb-2021 03-Feb-2021 *** Stamp Duty *** 22.74 (1.929) 2,384.1056 for tempTran or txPattern9
						//System.out.println("TxPattern 8 matched = "+tr);
						date = (tempTran.substring(0, tempTran.indexOf(" "))).trim();
						price = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tempTran = tempTran.substring(0, tempTran.lastIndexOf(" "));
						units = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tempTran = tempTran.substring(0, tempTran.lastIndexOf(" "));
						
						tr = tr.substring(0,tr.lastIndexOf(" "));
						amount = tr.substring(tr.lastIndexOf(" "), tr.length());
						flag=true;
					}
				}
			}
			else if(tr.matches(txPattern9_1)  )
			{
				System.out.println("TxPattern 9 matched with 10");
				String tempTran=tr;
				if(txI.hasNext())
				{
					tr = txI.next();
					tr = tr.replaceAll(",", "");
					//System.out.println("tr for 8="+tr);
					if(tempTran.matches(txPattern9_1) && tr.contains("Sys. Investment") && tr.contains("*** Stamp Duty ***")
							&& tr.matches(txPattern10))
					{
						//eg-S T P Out (4,596.00) 143.559 for tr(next transaction)
						//eg-03-Feb-2021 03-Feb-2021 *** Stamp Duty *** 22.74 (1.929) 2,384.1056 for tempTran or txPattern9
						//System.out.println("TxPattern 8 matched = "+tr);
						date = (tempTran.substring(0, tempTran.indexOf(" "))).trim();
						price = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tempTran = tempTran.substring(0, tempTran.lastIndexOf(" "));
						units = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tempTran = tempTran.substring(0, tempTran.lastIndexOf(" "));
						
						tr = tr.substring(0,tr.lastIndexOf(" "));
						amount = tr.substring(tr.lastIndexOf(" "), tr.length());
						flag=true;
					}
				}
			}
			else if(tr.matches(txPattern6))
			{
				System.out.println("tran="+tr+" txPattern6");
				//Check last 3 value
				//System.out.println(tr + " - Text missing");
				tr=tr.substring(0, tr.lastIndexOf(" "));
				//System.out.println(tr);
				price = tr.substring(tr.lastIndexOf(" "), tr.length());
				tr = tr.substring(0, tr.lastIndexOf(" "));
				units = tr.substring(tr.lastIndexOf(" "), tr.length());
				tr = tr.substring(0, tr.lastIndexOf(" "));
				amount = tr.substring(tr.lastIndexOf(" "), tr.length());
				date = tr.substring(0, 11);
				flag = true;
			}			
			else if(tr.matches(txPattern1))
			{
				System.out.println("txPattern1");
				// Check last 4 value, ignore 4th, take other 3
				//System.out.println(tr + " - Standard pattern");
				tr = tr.substring(0, tr.lastIndexOf(" "));
				price = tr.substring(tr.lastIndexOf(" "), tr.length());
				tr = tr.substring(0, tr.lastIndexOf(" "));
				units = tr.substring(tr.lastIndexOf(" "), tr.length());
				tr = tr.substring(0, tr.lastIndexOf(" "));
				amount = tr.substring(tr.lastIndexOf(" "), tr.length());
				tr=tr.substring(0,tr.lastIndexOf(" "));
				content = tr.substring(tr.indexOf(" ")+1,tr.length());
				date = tr.substring(0, 11);
				flag = true;
			}
			else if(tr.matches(txPattern2))
			{
				System.out.println("txPattern2");
				// Check last 3 value
				//System.out.println(tr + "!@#$%^&* - Unit balance missing");
				
				if(tr.substring(12, (12+11)).equals("Bonus Units"))
				{
					//System.out.println("Indside Bonus Points.");
					tr=tr.substring(0, tr.lastIndexOf(" "));
					units = tr.substring(tr.lastIndexOf(" "), tr.length());
					date = tr.substring(0, 11);
					/*System.out.print("Units="+units+" date="+date);*/
					price=".1";
					amount=String.valueOf(Double.valueOf(units)*Double.valueOf(price));
					/*System.out.println("price="+price+" amount="+amount);*/
					flag = true;
				}
				else
				{
					price = tr.substring(tr.lastIndexOf(" "), tr.length());
					tr = tr.substring(0, tr.lastIndexOf(" "));
					units = tr.substring(tr.lastIndexOf(" "), tr.length());
					tr = tr.substring(0, tr.lastIndexOf(" "));
					amount = tr.substring(tr.lastIndexOf(" "), tr.length());
					date = tr.substring(0, 11);
					flag = true;
				}
			}
			else if(tr.matches(txPattern3))
			{
				System.out.println("txPattern3");
				// Check last 3 value
				//System.out.println(tr + " - Text and Unit balance missing");
				price = tr.substring(tr.lastIndexOf(" "), tr.length());
				tr = tr.substring(0, tr.lastIndexOf(" "));
				units = tr.substring(tr.lastIndexOf(" "), tr.length());
				tr = tr.substring(0, tr.lastIndexOf(" "));
				amount = tr.substring(tr.lastIndexOf(" "), tr.length());
				date = tr.substring(0, 11);
				flag = true;
			}
			else if(tr.matches(txPattern4))
			{
				System.out.println("TxPattern 4 matched");
				String tempTran=tr;
				if(txI.hasNext())
				{
					tr = txI.next();
					tr = tr.replaceAll(",", "");
					//System.out.println("tr for 5="+tr);
					if(tr.matches(txPattern5))
					{
						//System.out.println("TxPattern 5 matched = "+tr);
						price = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tempTran = tempTran.substring(0, tempTran.lastIndexOf(" "));
						units = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						date = tempTran.substring(0, 11);
						tr = tr.substring(0,tr.lastIndexOf(" "));
						amount = tr.substring(tr.lastIndexOf(" "), tr.length());
						flag=true;
					}
				}
			}
			else if(tr.matches(txPattern7))
			{
				System.out.println("TxPattern 7 matched");
				String tempTran=tr;
				if(txI.hasNext())
				{
					tr = txI.next();
					tr = tr.replaceAll(",", "");
					//System.out.println("tr for 8="+tr);
					if(tr.matches(txPattern8))
					{
						//System.out.println("TxPattern 8 matched = "+tr);
						price = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tempTran = tempTran.substring(0, tempTran.lastIndexOf(" "));
						units = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tempTran = tempTran.substring(0, tempTran.lastIndexOf(" "));
						date = tempTran.substring(tempTran.lastIndexOf(" "), tempTran.length());
						tr = tr.substring(0,tr.lastIndexOf(" "));
						amount = tr.substring(tr.lastIndexOf(" "), tr.length());
						flag=true;
					}
				}
			}
			else if(tr.toLowerCase().contains("dividend") && (tr.toLowerCase().contains("paid") || tr.toLowerCase().contains("payout")))
			{
				//System.out.println("tr is"+tr);
				String previousTransaction=(tx.get(transactionIndex-1).replace(",", "")).trim();
				//System.out.println("previousTransaction is"+previousTransaction+"|");
				/*(previousTransaction.matches("^(\\s*)+[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+[0-9]+((\\.)[0-9]{2,4})*"))
				{
					         System.out.println("matching");
					
				}*/
				
				/*System.out.println("tr-1 is"+tx.get(transactionIndex-1));
				if((tx.get(transactionIndex-1).replace(",", "")).matches("^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+[0-9]+((\\.)[0-9]{2,4})*")) {
					System.out.println("matching");
				}*/
				//date text amount and tr contains dividend payout or paid
				if(tr.matches("^[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+(.)+[0-9]+((\\.)[0-9]{2,4})*(\\s)*$") && (tr.toLowerCase().contains("dividend") && (tr.toLowerCase().contains("paid") || tr.toLowerCase().contains("payout"))) ) 
				{
					//System.out.println("tr is in first"+tr);
					div_payout_flag=true;
					content="Dividend Paid";
					amount = tr.substring(tr.lastIndexOf(" "), tr.length());
					date = tr.substring(0, 11);
				}
				//date amount in previous line
				//divident text in recent line
				else if(previousTransaction.matches("^(\\s*)+[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}(\\s)+[0-9]+((\\.)[0-9]{2,4})*")
						&& (tr.toLowerCase().contains("dividend") && (tr.toLowerCase().contains("paid") || tr.toLowerCase().contains("payout")))
						) 
				{
					//System.out.println("tr is in second"+tr);
					div_payout_flag=true;
					content="Dividend Paid";
					date = previousTransaction.substring(0, 11);
					amount = previousTransaction.substring(previousTransaction.lastIndexOf(" "), previousTransaction.length());
				}else if(previousTransaction.matches("^(\\s*)+[0-9]{2}\\-[A-Za-z]{3}\\-[0-9]{4}")
						&& (tr.toLowerCase().contains("dividend") && (tr.toLowerCase().contains("paid") || tr.toLowerCase().contains("payout"))
								&& (tr.matches("^(.)+[0-9]+((\\.)[0-9]{2,4})*(\\s)*$")))
						) 
				{
					//System.out.println("tr is in third"+tr);
					div_payout_flag=true;
					content="Dividend Paid";
					date = previousTransaction.substring(0, 11);
					amount = tr.substring(tr.lastIndexOf(" "), tr.length());
				}
				
				
			}
			
			else
			{
				// System.out.println(tr + " - Does not match");
			}
			// System.out.println(date);
			if(flag == true)
			{
				Transactions transaction = new Transactions();
				txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
				if(amount.contains("("))
				{
					amount = amount.replace("(", "-");
					amount = amount.replace(")", "");
					amt = Double.valueOf(amount);
					units = units.replace("(", "-");
					units = units.replace(")", "");
					u = Double.valueOf(units);
					p = Double.valueOf(price);
					
					if(amt>1 || amt<-1)
					{
						transaction.setAmount(amt);
						transaction.setPrice(p);
						transaction.setUnits(u);
						transaction.setDate(txDate);
						transaction.setType("2");
						transactions.add(transaction);
					}
					//System.out.println("units is"+units);
				}
				else
				{
					amt = Double.valueOf(amount);
					//System.out.println("units is"+units);
					u = Double.valueOf(units);
					p = Double.valueOf(price);
					if(amt>1 || amt<-1)
					{
						transaction.setAmount(amt);
						transaction.setPrice(p);
						transaction.setUnits(u);
						transaction.setDate(txDate);
						transaction.setType("1");
						transactions.add(transaction);
					}
				}
				//System.out.println(date+"\t"+amt+"\t"+u+"\t"+p);
				
				//Below arn code should stay only on FUNDEXPERT
				Pattern pattern=Pattern.compile("((ARN[\\-])([0-9]+))(([\\/][a-zA-Z0-9]+))|((ARN[\\-])([0-9]+))");
				Matcher m=pattern.matcher(content);
				//System.out.println(m.groupCount());
				while(m.find())
				{
					/*System.out.println("Match found for ARN : "+m.group(3)+"  "+m.group(8));*/
					if(m.group(0).matches("(ARN[\\-][0-9]+)"))
					{
						transaction.setArn(m.group(3));
					}
					else if(m.group(1).matches("(ARN[\\-][0-9]+)"))
					{
						transaction.setArn(m.group(8));
					}
					
					//System.out.println(m.group(0)+" "+m.group(1)+" "+m.group(2)+" $ "+m.group(3)+" $ !"+m.group(4)+"@ $  "+m.group(5)+" "+m.group(6)+" "+m.group(7)+" "+m.group(8));
				}
				//System.out.println(txDate + "\t\t" + "content is"+content + "\t\t" +"|amount is"+ amt + "\t\t\t" + u + "\t\t\t" + p);
			}
			else if(div_payout_flag)
			{
				Transactions transaction = new Transactions();
				transaction.setTransaction(content);
				//txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
				if(date.contains("/")) 
				{
					txDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
				}else {
					txDate = new SimpleDateFormat("dd-MMM-yyyy").parse(date);
				}
				amt = Double.valueOf(amount);
				transaction.setAmount(-amt);
				transaction.setPrice(0.0);
				transaction.setUnits(0.0);
				transaction.setDate(txDate);
				transaction.setType("2");
				transactions.add(transaction);
			}
			transactionIndex++;
		}
		return transactions;
	}
}
