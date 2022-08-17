package it.smartcommunitylab.playandgo.engine.campaign.school;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.playandgo.engine.ge.GamificationEngineManager;
import it.smartcommunitylab.playandgo.engine.manager.survey.SurveyManager;
import it.smartcommunitylab.playandgo.engine.manager.survey.SurveyRequest;
import it.smartcommunitylab.playandgo.engine.model.Campaign;
import it.smartcommunitylab.playandgo.engine.model.CampaignSubscription;
import it.smartcommunitylab.playandgo.engine.model.Player;
import it.smartcommunitylab.playandgo.engine.util.Utils;

@Component
public class SchoolCampaignSubscription {
	private static Logger logger = LoggerFactory.getLogger(SchoolCampaignSubscription.class);
	
	@Autowired
	SurveyManager surveyManager;
	
	@Autowired
	GamificationEngineManager gamificationEngineManager;

	public CampaignSubscription subscribeCampaign(Player player, Campaign campaign, 
			Map<String, Object> campaignData) throws Exception {
		CampaignSubscription sub = new CampaignSubscription();
		sub.setPlayerId(player.getPlayerId());
		sub.setCampaignId(campaign.getCampaignId());
		sub.setTerritoryId(player.getTerritoryId());
		sub.setMail(player.getMail());
		sub.setSendMail(player.getSendMail());
		sub.setRegistrationDate(new Date());
		if(campaignData != null) {
			sub.setCampaignData(campaignData);
		}
		//check default survey
		if(campaign.hasDefaultSurvey()) {
			SurveyRequest sr = campaign.getDefaultSurvey();
			surveyManager.assignSurveyChallenges(campaign.getCampaignId(), Arrays.asList(player.getPlayerId()), sr);
		}
		//create player on GE
		if(Utils.isNotEmpty(campaign.getGameId())) {
			gamificationEngineManager.createPlayer(player.getPlayerId(), campaign.getGameId());
		}		
		return sub;
	}
}
