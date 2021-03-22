package com.fundexpert.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

//import com.fundexpert.connect.mail.team.MissingFundsMail;
//import com.fundexpert.controller.PortfolioImportController;
import com.fundexpert.pojo.Holdings;
import com.fundexpert.pojo.Transactions;
import com.fundexpert.util.CopyOfCamsPortfolioPdfParser;
//import com.fundexpert.util.PdfReaderAndPersist;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class KarvyPdfParserTest {

	public static void main(String[] args) throws IOException, DocumentException {
		
		/*String sourcePath = "F:\\PdfFiles\\575282-1653480030120191028470578564494824.pdf";
		String targetPath = "F:/PdfFiles/karvy/575282-1653480030120191028470578564494824.pdf";
		String pass = "575282";*/
		/*String sourcePath = "F:\\PdfFiles\\Fund@151361-4857882101202114213092384992346202.pdf";
		String targetPath = "F:/PdfFiles/karvy/Fund@151361-4857882101202114213092384992346202.pdf";
		String pass = "Fund@151361";*/
		
		String sourcePath = "/home/puscd/iText-to-openPDF/Fund@174200-16845352302202117132070986790572614.pdf";
		String targetPath = "/home/puscd/iText-to-openPDF/Fund@174200-16845352302202117132070986790572614_OUT.pdf";
		String pass = "Fund@174200";
//		String pass="123456";
		KarvyPdfParserTest k=new KarvyPdfParserTest();
		try {
			/*PortfolioImportController portfolioImportController = new PortfolioImportController(sourcePath);
			portfolioImportController.sFactory(142746, "Fund@142746-17161990203202112293557150050613905",true,pass,true);
			System.out.println("After Pdf importing");
			// writer.print(storePdf.getMissingFunds().toString(4));
			List<Map> missingFunds = portfolioImportController.getMissingFunds();

			System.out.println("After missing funds " + missingFunds.size());*/
			
			System.out.println("output of decrypted File="+k.getDecryptedFile(sourcePath, targetPath, pass));
			
			//PdfReaderAndPersist prap=new PdfReaderAndPersist();
		
			CopyOfCamsPortfolioPdfParser pdfParser = new CopyOfCamsPortfolioPdfParser(targetPath, pass);
			List<Holdings> holdingsList=pdfParser.getKarvyHoldingsList(targetPath, pass);
			Iterator itr = holdingsList.iterator();
			String write = "";
			while (itr.hasNext())
			{
				Holdings h = (Holdings) itr.next();

				write = write + h.getCamsCode() + "-" + h.getMutualFundName() + "\n" + h.getFolioNumberString() + "\n" + h.getArn() + "\n";

				List<Transactions> transList = h.getTransactionsList();
				Iterator itr1 = transList.iterator();
				while (itr1.hasNext())
				{
					Transactions trans = (Transactions) itr1.next();
					write = write + trans.getDate() + "\t" + trans.getAmount() + "\t" + trans.getUnits() + "\t" + trans.getPrice() + "\n";
				}
				write = write + h.getClosingUnits() + "\n\n\n";
			}
			System.out.println(write);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String getDecryptedFile(String sourceFilePath,String targetFilePath,String password) throws IOException
	{
		String command = "";
		try
		{
			File f=new File(targetFilePath);
			if(f.exists())
				System.out.println("Success of deleting existing targetFile = "+f.delete());
			if(((String)System.getProperties().get("os.name")).contains("Windows"))
				command="C:/Program Files/qpdf-8.2.1/bin/qpdf.exe";
			else
				command="qpdf";
			ProcessBuilder pb = new ProcessBuilder(command, "--password="+password, "--decrypt", sourceFilePath, targetFilePath);
			pb. redirectErrorStream(true);
			
			StringBuffer output = new StringBuffer();

			Process p=pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";			
			while ((line = reader.readLine())!= null) 
			{
				output.append(line);
			}
			if(output.toString().toLowerCase().contains("invalid password"))
				throw new BadPasswordException("Not able to decrypt file.");
			return output.toString();
		}
		catch(BadPasswordException be)
		{
			throw be;
		}
		catch(IOException ioe)
		{
			throw ioe;
		}
	}

}
