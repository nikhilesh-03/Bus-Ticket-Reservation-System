package com.masai.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.masai.exceptions.DuplicateDataException;
import com.masai.exceptions.ProductException;
import com.masai.utility.DBConnection;

import java.sql.*;
import com.masai.entities.Bus;
import com.masai.exceptions.ProductException;

public class BusServiceImpl implements BusService{


	@Override
	public String addBus(Bus bus, Map<Integer, Bus> buses) throws DuplicateDataException {
		String message = "Bus not added!";
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		try (Connection conn = DBConnection.getConnection()) {
			String query = "INSERT INTO Bus (busId, busName, busType, source, destination, departureDate, arrivalDate, totalSeats, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, bus.getId());
			ps.setString(2, bus.getBusName());
			ps.setString(3, bus.getBusType());
			ps.setString(4, bus.getSource());
			ps.setString(5, bus.getDestination());
			ps.setString(6, bus.getDepartureTime().format(formatter));
			ps.setString(7, bus.getArrivalTime().format(formatter));
			ps.setInt(8, bus.getTotalSeat());
			ps.setDouble(9, bus.getPrice());

			int rowsInserted = ps.executeUpdate();
			if (rowsInserted > 0) {
				message = "Bus added successfully!";
				buses.put(bus.getId(), bus);
			}
		} catch (SQLException e) {
			throw new DuplicateDataException(e.getMessage());
		}
		return message;
	}
	
	
	@Override
	public void viewAllBuses(Map<Integer, Bus> buses) throws ProductException {
		try (Connection conn = DBConnection.getConnection()) {
			String query = "SELECT * FROM Bus";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int busId = rs.getInt("busId");
				String busName = rs.getString("busName");
				String busType = rs.getString("busType");
				String source = rs.getString("source");
				String destination = rs.getString("destination");
				int totalSeats = rs.getInt("TotalSeats");
				Double price = rs.getDouble("Price");

				Bus bus = new Bus(busId, busName, totalSeats, busType, price, source, destination);
				buses.put(busId, bus);
			}

			if (buses != null && !buses.isEmpty()) {
				for (Map.Entry<Integer, Bus> entry : buses.entrySet()) {
					System.out.println(entry.getValue());
				}
			}
			else {
				throw new ProductException("No Buses are there...");
			}

		}
		catch (SQLException e) {
			throw new ProductException("Unable to retrieve bus details.");
		}
	}

	@Override
	public void deleteBus(int busId, Map<Integer, Bus> buses) throws ProductException {
		if (buses != null && !buses.isEmpty()) {
			if (buses.containsKey(busId)) {
				try (Connection conn = DBConnection.getConnection()) {
					String query = "DELETE FROM Bus WHERE busId = ?";
					PreparedStatement ps = conn.prepareStatement(query);
					ps.setInt(1, busId);

					int rowsDeleted = ps.executeUpdate();
					if (rowsDeleted > 0) {
						buses.remove(busId);
						System.out.println("Bus deleted successfully");
					}
					else {
						throw new ProductException("Bus not found in the database.");
					}
				}
				catch (SQLException e) {
					throw new ProductException("Unable to delete bus.");
				}
			}
			else {
				throw new ProductException("Bus details not found in the map.");
			}
		}
		else {
			throw new ProductException("There are no buses in the list.");
		}
	}

	@Override
	public String updateBusDetails(int busId, Bus bus, Map<Integer, Bus> buses) throws ProductException {

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		if (buses != null && !buses.isEmpty()) {
			if (buses.containsKey(busId)) {
				try (Connection conn = DBConnection.getConnection()) {
					String query = "UPDATE Bus SET busName = ?, busType = ?, source = ?, destination = ?, departureDate = ?, arrivalDate = ?, totalSeats = ?, price = ? WHERE busId = ?";
					PreparedStatement ps = conn.prepareStatement(query);
					ps.setString(1, bus.getBusName());
					ps.setString(2, bus.getBusType());
					ps.setString(3, bus.getSource());
					ps.setString(4, bus.getDestination());
					ps.setString(5, bus.getDepartureTime().format(formatter));
					ps.setString(6, bus.getArrivalTime().format(formatter));
					ps.setInt(7, bus.getTotalSeat());
					ps.setDouble(8, bus.getPrice());
					ps.setInt(9, busId);

					int rowsUpdated = ps.executeUpdate();
					if (rowsUpdated > 0) {
						buses.put(busId, bus);
						return "Bus details have been successfully updated";
					}
					else {
						throw new ProductException("Bus not found in the database.");
					}
				}
				catch (SQLException e) {
					throw new ProductException("Unable to update bus details due to " + e.getMessage());
				}
			}
			else {
				throw new ProductException("Bus details are not found in the map.");
			}
		}
		else {
			throw new ProductException("There are no buses in the list.");
		}
	}

}
