package com.fundexpert.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Holdings
{
	long folioNumber;
	String camsCode, mutualFundName, folioNumberString;
	Double closingUnits, avgNav;
	String arn;
	List<Transactions> transactionsList = new ArrayList<Transactions>();
	Date statementDate;
	String emailId;

	public String getFolioNumberString()
	{
		return folioNumberString;
	}

	public void setFolioNumberString(String folioNumberString)
	{
		this.folioNumberString = folioNumberString;
	}

	public long getFolioNumber()
	{
		return folioNumber;
	}

	public void setFolioNumber(long folioNumber)
	{
		this.folioNumber = folioNumber;
	}

	public Double getClosingUnits()
	{
		return closingUnits;
	}

	public String getCamsCode()
	{
		return camsCode;
	}

	public void setCamsCode(String camsCode)
	{
		this.camsCode = camsCode;
	}

	public String getMutualFundName()
	{
		return mutualFundName;
	}

	public void setMutualFundName(String mutualFundName)
	{
		this.mutualFundName = mutualFundName;
	}

	public List<Transactions> getTransactionsList()
	{
		return transactionsList;
	}

	public void setTransactionsList(List<Transactions> transactionsList)
	{
		this.transactionsList = transactionsList;
	}

	public Double getAvgNav()
	{
		return avgNav;
	}

	public void setAvgNav(Double avgNav)
	{
		this.avgNav = avgNav;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public void setClosingUnits(Double closingUnits) {
		this.closingUnits = closingUnits;
	}

	public Date getStatementDate() {
		return statementDate;
	}

	public void setStatementDate(Date statementDate) {
		this.statementDate = statementDate;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmail(String emailId) {
		this.emailId=emailId;
	}
}
