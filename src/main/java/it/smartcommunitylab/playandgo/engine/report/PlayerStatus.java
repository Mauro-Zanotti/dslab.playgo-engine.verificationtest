package it.smartcommunitylab.playandgo.engine.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import it.smartcommunitylab.playandgo.engine.model.Territory;
import it.smartcommunitylab.playandgo.engine.util.LocalDateDeserializer;

public class PlayerStatus {
	private String playerId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)	
	private LocalDate registrationDate;
	private int activityDays;
	private long travels;
	private List<TransportStats> transportStatsList = new ArrayList<>();
	private Territory territory;
	private double co2;
	
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public LocalDate getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}
	public int getActivityDays() {
		return activityDays;
	}
	public void setActivityDays(int activityDays) {
		this.activityDays = activityDays;
	}
	public long getTravels() {
		return travels;
	}
	public void setTravels(long travels) {
		this.travels = travels;
	}
	public List<TransportStats> getTransportStatsList() {
		return transportStatsList;
	}
	public void setTransportStatsList(List<TransportStats> transportStatsList) {
		this.transportStatsList = transportStatsList;
	}
	public Territory getTerritory() {
		return territory;
	}
	public void setTerritory(Territory territory) {
		this.territory = territory;
	}
	public double getCo2() {
		return co2;
	}
	public void setCo2(double co2) {
		this.co2 = co2;
	}
}
