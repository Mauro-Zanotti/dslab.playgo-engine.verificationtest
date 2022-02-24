package it.smartcommunitylab.playandgo.engine.notification;

import java.util.Date;

public class ChallengeAssignedNotification extends NotificationGe {

	private String challengeName;
	private Date startDate;
	private Date endDate;

	public String getChallengeName() {
		return challengeName;
	}

	public void setChallengeName(String challengeName) {
		this.challengeName = challengeName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return String.format("[gameId=%s, playerId=%s, challengeName=%s startDate=%s endDate=%s]", getGameId(),
				getPlayerId(), challengeName, startDate, endDate);
	}

}
