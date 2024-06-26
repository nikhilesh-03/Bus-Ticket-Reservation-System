package com.masai.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.masai.entities.Bus;
import com.masai.entities.Passenger;
import com.masai.entities.Transaction;
import com.masai.exceptions.DuplicateDataException;
import com.masai.exceptions.InvalidDetailsException;
import com.masai.exceptions.ProductException;
import com.masai.utility.DBConnection;

public class PassengerServiceImpl implements PassengerService {

	@Override
	public boolean login(String email, String password, Map<String, Passenger> passengers) throws InvalidDetailsException {
		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT passwd FROM Passenger WHERE emailId = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String dbPassword = rs.getString("passwd");
				if (dbPassword.equals(password)) {
					// Sync with the local map if necessary
					if (!passengers.containsKey(email)) {
						// Load passenger details from the database
						String detailsQuery = "SELECT * FROM Passenger WHERE emailId = ?";
						PreparedStatement detailsPs = conn.prepareStatement(detailsQuery);
						detailsPs.setString(1, email);
						ResultSet detailsRs = detailsPs.executeQuery();

						if (detailsRs.next()) {
							String username = detailsRs.getString("username");
							String mobileNo = detailsRs.getString("mobileNo");
							double walletBalance = detailsRs.getDouble("walletBalance");
							Passenger passenger = new Passenger(username, password, email, mobileNo, walletBalance);
							passengers.put(email, passenger);
						}
					}
					return true;
				}
				else {
					throw new InvalidDetailsException("Invalid Credentials");
				}
			}
			else {
				throw new InvalidDetailsException("You have not signed up yet, please sign up");
			}
		}
		catch (SQLException e) {
			throw new InvalidDetailsException("Database error occurred during login: " + e.getMessage());
		}
	}

	@Override
	public void signUp(Passenger pas, Map<String, Passenger> customers) throws DuplicateDataException {
		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT * FROM Passenger WHERE emailId = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, pas.getEmailId());
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				throw new DuplicateDataException("Passenger already exists, please login.");
			} else {
				String insertQuery = "INSERT INTO Passenger (username, passwd, mobileNo, emailId, walletBalance) VALUES (?, ?, ?, ?, ?)";
				PreparedStatement insertPs = conn.prepareStatement(insertQuery);
				insertPs.setString(1, pas.getUsername());
				insertPs.setString(2, pas.getPassword());
				insertPs.setString(3, pas.getMobileNo());
				insertPs.setString(4, pas.getEmailId());
				insertPs.setDouble(5, pas.getWalletBalance());

				int rowsInserted = insertPs.executeUpdate();
				if (rowsInserted > 0) {
					customers.put(pas.getEmailId(), pas);
				} else {
					throw new DuplicateDataException("Passenger could not be registered.");
				}
			}
		} catch (SQLException e) {
			throw new DuplicateDataException("Database error occurred during sign up: " + e.getMessage());
		}
	}

	
	@Override
	public boolean addMoneyToWallet(double amount, String email, Map<String, Passenger> passengers) {

		try (Connection conn = DBConnection.getConnection()) {
			String query = "UPDATE Passenger SET walletBalance = walletBalance + ? WHERE emailId = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setDouble(1, amount);
			ps.setString(2, email);

			int rowsUpdated = ps.executeUpdate();
			if (rowsUpdated > 0) {
				Passenger psng = passengers.get(email);
				psng.setWalletBalance(psng.getWalletBalance() + amount);
				passengers.put(email, psng);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public double viewWalletBalance(String email, Map<String, Passenger> passengers) {
		double walletBalance = 0.0;

		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT walletBalance FROM Passenger WHERE emailId = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				walletBalance = rs.getDouble("walletBalance");

				// Sync with the local map if necessary
				if (passengers.containsKey(email)) {
					Passenger psng = passengers.get(email);
					psng.setWalletBalance(walletBalance);
					passengers.put(email, psng);
				}
			} else {
				System.out.println("Passenger with email " + email + " does not exist.");
			}
		} catch (SQLException e) {
			System.out.println("Database error occurred while fetching wallet balance: " + e.getMessage());
		}

		return walletBalance;
	}

	@Override
	public Passenger viewPassengerDetails(String email, Map<String, Passenger> passengers) {

		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT * FROM Passenger WHERE emailId = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String username = rs.getString("username");
				String password = rs.getString("passwd");
				String mobileNo = rs.getString("mobileNo");
				double walletBalance = rs.getDouble("walletBalance");

				Passenger psng = new Passenger(username, password, email, mobileNo, walletBalance);
				passengers.put(email, psng);
				return psng;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<Passenger> viewAllPassengers(Map<String, Passenger> passengers) throws ProductException {

		List<Passenger> list = new ArrayList<>();
		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT * FROM Passenger";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				String username = rs.getString("userName");
				String password = rs.getString("passwd");
				String mobileNo = rs.getString("mobileNo");
				String emailId = rs.getString("emailId");
				double walletBalance = rs.getDouble("walletBalance");

				Passenger psng = new Passenger(username, password, mobileNo, emailId, walletBalance);
				list.add(psng);
			}

			if (list.isEmpty()) {
				throw new ProductException("No passengers are there.");
			}
		} catch (SQLException e) {
			throw new ProductException("Unable to retrieve passenger details.");
		}
		return list;
	}

	@Override
	public boolean bookTicket(int busId, int noOfSeat, String email, Map<Integer, Bus> bus,
			Map<String, Passenger> passengers, List<Transaction> transactions) throws InvalidDetailsException, ProductException {
		try (Connection conn = DBConnection.getConnection()) {
			String busQuery = "SELECT * FROM Bus WHERE busId = ?";
			PreparedStatement busPs = conn.prepareStatement(busQuery);
			busPs.setInt(1, busId);
			ResultSet busRs = busPs.executeQuery();

			if (busRs.next()) {
				int totalSeats = busRs.getInt("totalSeats");
				double pricePerSeat = busRs.getDouble("price");

				if (totalSeats >= noOfSeat) {
					Passenger psng = passengers.get(email);

					double totalPrice = noOfSeat * pricePerSeat;

					if (psng.getWalletBalance() >= totalPrice) {
						conn.setAutoCommit(false);

						// Deduct wallet balance
						String updateWalletQuery = "UPDATE Passenger SET walletBalance = walletBalance - ? WHERE emailId = ?";
						PreparedStatement updateWalletPs = conn.prepareStatement(updateWalletQuery);
						updateWalletPs.setDouble(1, totalPrice);
						updateWalletPs.setString(2, email);
						updateWalletPs.executeUpdate();

						// Update bus seats
						String updateBusQuery = "UPDATE Bus SET totalSeats = totalSeats - ? WHERE busId = ?";
						PreparedStatement updateBusPs = conn.prepareStatement(updateBusQuery);
						updateBusPs.setInt(1, noOfSeat);
						updateBusPs.setInt(2, busId);
						updateBusPs.executeUpdate();

						// Create transaction
						String insertTransactionQuery = "INSERT INTO Transaction (username, emailId, busId, busName, noOfBookedSeats, pricePerSeat, totalPrice, bookingDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
						PreparedStatement insertTransactionPs = conn.prepareStatement(insertTransactionQuery);
						insertTransactionPs.setString(1, psng.getUsername());
						insertTransactionPs.setString(2, email);
						insertTransactionPs.setInt(3, busId);
						insertTransactionPs.setString(4, busRs.getString("busName"));
						insertTransactionPs.setInt(5, noOfSeat);
						insertTransactionPs.setDouble(6, pricePerSeat);
						insertTransactionPs.setDouble(7, totalPrice);
						insertTransactionPs.setDate(8, java.sql.Date.valueOf(LocalDate.now()));
						insertTransactionPs.executeUpdate();

						conn.commit();
						conn.setAutoCommit(true);

						psng.setWalletBalance(psng.getWalletBalance() - totalPrice);
						bus.get(busId).setTotalSeat(totalSeats - noOfSeat);
						Transaction tr = new Transaction(psng.getUsername(), email, busId, busRs.getString("busName"), noOfSeat, pricePerSeat, totalPrice, LocalDate.now());
						transactions.add(tr);

						return true;
					}
					else {
						throw new InvalidDetailsException("Wallet balance is not sufficient");
					}
				}
				else {
					throw new InvalidDetailsException("Number of seats are not sufficient");
				}
			}
			else {
				throw new InvalidDetailsException("Bus not available with id: " + busId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ProductException("Error occurred while booking ticket.");
		}

//		return false;
	}

}
