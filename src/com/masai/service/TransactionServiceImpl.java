package com.masai.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.masai.entities.Transaction;
import com.masai.exceptions.TransactionException;
import com.masai.utility.DBConnection;

public class TransactionServiceImpl implements TransactionService {

	@Override
	public List<Transaction> viewPassengerTransactions(String email, List<Transaction> transactions) throws TransactionException {

		List<Transaction> myTransactions = new ArrayList<>();

		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT * FROM Transaction WHERE emailId = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Transaction tr = new Transaction(
						rs.getString("username"),
						rs.getString("emailId"),
						rs.getInt("busId"),
						rs.getString("busName"),
						rs.getInt("noOfBookedSeats"),
						rs.getDouble("pricePerSeat"),
						rs.getDouble("totalPrice"),
						rs.getDate("bookingDate").toLocalDate()
				);
				myTransactions.add(tr);
			}

			if (myTransactions.isEmpty()) {
				throw new TransactionException("There are no transactions for this passenger.");
			}
		} catch (SQLException e) {
			throw new TransactionException("Database error occurred while fetching transactions: " + e.getMessage());
		}

		return myTransactions;

	}

	@Override
	public List<Transaction> viewAllTransactions(List<Transaction> transactions) throws TransactionException {

		List<Transaction> allTransactions = new ArrayList<>();

		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT * FROM Transaction";
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Transaction tr = new Transaction(
						rs.getString("username"),
						rs.getString("emailId"),
						rs.getInt("busId"),
						rs.getString("busName"),
						rs.getInt("noOfBookedSeats"),
						rs.getDouble("pricePerSeat"),
						rs.getDouble("totalPrice"),
						rs.getDate("bookingDate").toLocalDate()
				);
				allTransactions.add(tr);
			}

			if (allTransactions.isEmpty()) {
				throw new TransactionException("There are no transactions available.");
			}
		} catch (SQLException e) {
			throw new TransactionException("Database error occurred while fetching all transactions: " + e.getMessage());
		}

		return allTransactions;
	}

}
