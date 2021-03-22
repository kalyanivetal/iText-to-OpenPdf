package com.fundexpert.pojo;

import java.util.Date;
import java.util.List;

public class Transactions
{
	Date date;
	String transaction;// name of transaction
	double amount, units, price, unitBalance;
	String arn;
	
	public double getUnitBalance()
	{
		return unitBalance;
	}

	public void setUnitBalance(double unitBalance)
	{
		this.unitBalance = unitBalance;
	}

	String type;

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getTransaction()
	{
		return transaction;
	}

	public void setTransaction(String transaction)
	{
		this.transaction = transaction;
	}

	public double getAmount()
	{
		return amount;
	}

	public void setAmount(double amount)
	{
		this.amount = amount;
	}

	public double getUnits()
	{
		return units;
	}

	public void setUnits(double units)
	{
		this.units = units;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	
	
	
}
