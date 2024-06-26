package com.masai.entities;

import java.io.Serializable;

public class Passenger extends User implements Serializable {
	
	private double walletBalance;

	public Passenger(String username, String password, String mobileNo, String emailId, double walletBalance) {
		super(username, password, mobileNo, emailId);
		
		this.walletBalance = walletBalance;
		
	}
	
//	public Passenger(String email, String name, String pass, String address, double balance) {
//		// TODO Auto-generated constructor stub
//	}

	public double getWalletBalance() {
		return walletBalance;
	}
	
	public void setWalletBalance(double walletBalance) {
		this.walletBalance = walletBalance;
	}

	@Override
	public String toString() {
		return "Passenger [Username = " + getUsername() + ", MobileNo = "
				+ getMobileNo() + ", EmailId = " + getEmailId() + ", WalletBalance = " + walletBalance + "]";
	}

}
