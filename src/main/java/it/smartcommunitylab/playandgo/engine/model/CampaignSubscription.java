package it.smartcommunitylab.playandgo.engine.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import it.smartcommunitylab.playandgo.engine.util.LocalDateDeserializer;

@Document(collection="campaignSubscriptions")
public class CampaignSubscription {
	@Id
	private String id;
	@Indexed
	private String playerId;
	@Indexed
	private String campaignId;
	private String territoryId;
	private String mail;
	private Boolean sendMail = Boolean.FALSE;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate registrationDate;
	private Map<String, Object> campaignData = new HashMap<>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public Map<String, Object> getCampaignData() {
		return campaignData;
	}
	public void setCampaignData(Map<String, Object> campaignData) {
		this.campaignData = campaignData;
	}
	public Boolean getSendMail() {
		return sendMail;
	}
	public void setSendMail(Boolean sendMail) {
		this.sendMail = sendMail;
	}
	public String getTerritoryId() {
		return territoryId;
	}
	public void setTerritoryId(String territoryId) {
		this.territoryId = territoryId;
	}
	public LocalDate getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}
}
