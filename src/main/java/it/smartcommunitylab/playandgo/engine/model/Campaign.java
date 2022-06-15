package it.smartcommunitylab.playandgo.engine.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="campaigns")
public class Campaign {
	public static enum Type {
		company, city, school, personal
	};

	@Id
	private String campaignId;
	private Type type;
	private String territoryId;
	private String name;
	private String description;
	private Date dateFrom;
	private Date dateTo;
	private Boolean active = Boolean.FALSE;
	private Boolean communications = Boolean.FALSE;
	private int startDayOfWeek = 1; //Monday is 1 and Sunday is 7
	@Indexed
	private String gameId;
	private List<CampaignDetail> details = new ArrayList<>(); 
	private Image logo;
	private Image banner;
	
	private Map<String, Object> validationData = new HashMap<>();
	
	/**
	 * <name, link>
	 */
	private Map<String, String> surveys = new HashMap<>();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public Map<String, Object> getValidationData() {
		return validationData;
	}

	public void setValidationData(Map<String, Object> validationData) {
		this.validationData = validationData;
	}

	public String getTerritoryId() {
		return territoryId;
	}

	public void setTerritoryId(String territoryId) {
		this.territoryId = territoryId;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getCommunications() {
		return communications;
	}

	public void setCommunications(Boolean communications) {
		this.communications = communications;
	}

	public int getStartDayOfWeek() {
		return startDayOfWeek;
	}

	public void setStartDayOfWeek(int startDayOfWeek) {
		this.startDayOfWeek = startDayOfWeek;
	}

	public Image getLogo() {
		return logo;
	}

	public void setLogo(Image logo) {
		this.logo = logo;
	}

	public boolean currentlyActive() {
		Date now = new Date();
		return !Boolean.FALSE.equals(getActive()) && 
				(getDateFrom() == null || !getDateFrom().after(now)) &&
				(getDateTo() == null || !getDateTo().before(now));
	}

	public Image getBanner() {
		return banner;
	}

	public void setBanner(Image banner) {
		this.banner = banner;
	}

	public List<CampaignDetail> getDetails() {
		return details;
	}

	public void setDetails(List<CampaignDetail> details) {
		this.details = details;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public Map<String, String> getSurveys() {
		return surveys;
	}

	public void setSurveys(Map<String, String> surveys) {
		this.surveys = surveys;
	}

}
