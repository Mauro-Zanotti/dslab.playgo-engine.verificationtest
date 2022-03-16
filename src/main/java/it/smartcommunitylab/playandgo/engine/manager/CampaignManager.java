package it.smartcommunitylab.playandgo.engine.manager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.playandgo.engine.campaign.BasicPersonalCampaignGameNotification;
import it.smartcommunitylab.playandgo.engine.campaign.PersonalCampaignTripValidator;
import it.smartcommunitylab.playandgo.engine.dto.PlayerCampaignDTO;
import it.smartcommunitylab.playandgo.engine.exception.BadRequestException;
import it.smartcommunitylab.playandgo.engine.model.Campaign;
import it.smartcommunitylab.playandgo.engine.model.Campaign.Type;
import it.smartcommunitylab.playandgo.engine.model.CampaignSubscription;
import it.smartcommunitylab.playandgo.engine.model.Player;
import it.smartcommunitylab.playandgo.engine.repository.CampaignRepository;
import it.smartcommunitylab.playandgo.engine.repository.CampaignSubscriptionRepository;
import it.smartcommunitylab.playandgo.engine.repository.PlayerRepository;

@Component
public class CampaignManager {
	private static transient final Logger logger = LoggerFactory.getLogger(CampaignManager.class);
	
	private static final String CAMPAIGNSUB = "campaignSubscriptions";
	
	@Autowired
	MongoTemplate template;
	
	@Autowired
	CampaignRepository campaignRepository;
	
	@Autowired
	CampaignSubscriptionRepository campaignSubscriptionRepository;
	
	@Autowired
	PlayerRepository playerRepository;
	
	@Autowired
	PersonalCampaignTripValidator basicPersonalCampaignTripValidator;
	
	@Autowired
	BasicPersonalCampaignGameNotification basicPersonalCampaignGameNotification;
	
	public void saveCampaign(Campaign campaign) {
		campaignRepository.save(campaign);
	}
	
	public Campaign getCampaign(String campaignId) {
		return campaignRepository.findById(campaignId).orElse(null);
	}
	
	public List<Campaign> getCampaigns() {
		return campaignRepository.findAll(Sort.by(Sort.Direction.DESC, "dateFrom"));
	}
	
	public List<Campaign> getCampaignsByTerritory(String territoryId) {
		return campaignRepository.findByTerritoryId(territoryId, Sort.by(Sort.Direction.DESC, "dateFrom"));
	}
	
	public Campaign deleteCampaign(String campaignId) {
		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if(campaign != null) {
			campaignRepository.deleteById(campaignId);
		}
		return campaign;
	}
	
	public Campaign getDefaultCampaignByTerritory(String territoryId) {
		return campaignRepository.findByTerritoryIdAndType(territoryId, Type.personal);
	}
	
	public CampaignSubscription subscribePlayer(Player player, String campaignId, Map<String, Object> campaignData) throws Exception {
		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if(campaign == null) {
			throw new BadRequestException("campaign doesn't exist");
		}
		if(!campaign.getTerritoryId().equals(player.getTerritoryId())) {
			throw new BadRequestException("territory doesn't match");
		}
		if(Type.company.equals(campaign.getType())) {
			//TODO check campaign subscription endpoint
		}
		CampaignSubscription sub = new CampaignSubscription();
		sub.setPlayerId(player.getPlayerId());
		sub.setCampaignId(campaignId);
		sub.setTerritoryId(player.getTerritoryId());
		sub.setMail(player.getMail());
		sub.setSendMail(player.getSendMail());
		sub.setRegistrationDate(LocalDate.now());
		if(campaignData != null) {
			sub.setCampaignData(campaignData);
		}
		return campaignSubscriptionRepository.save(sub);
	}
	
	public CampaignSubscription unsubscribePlayer(Player player, String campaignId) throws Exception {
		CampaignSubscription subscription = campaignSubscriptionRepository.findByPlayerId(campaignId, player.getPlayerId());
		if(subscription != null) {
			campaignSubscriptionRepository.deleteById(subscription.getId());
		}
		return subscription;
	}
	
	public void updateDefaultCampaignSuscription(Player player) {
		Campaign campaign = getDefaultCampaignByTerritory(player.getTerritoryId());
		if(campaign != null) {
			CampaignSubscription sub = campaignSubscriptionRepository.findByPlayerId(campaign.getCampaignId(), player.getPlayerId());
			if(sub != null) {
				Query query = new Query(new Criteria("id").is(sub.getId()));
				Update update = new Update();
				update.set("sendMail", player.getSendMail());
				update.set("mail", player.getMail());
				template.updateFirst(query, update, CAMPAIGNSUB);
			}
		}
	}
	
	public List<PlayerCampaignDTO> getPlayerCampaigns(String playerId) {
		List<PlayerCampaignDTO> result = new ArrayList<>();
		List<CampaignSubscription> campaigns = campaignSubscriptionRepository.findByPlayerId(playerId);
		for(CampaignSubscription sub : campaigns) {
			Campaign campaign = campaignRepository.findById(sub.getCampaignId()).orElse(null);
			if(campaign != null) {
				PlayerCampaignDTO dto = new PlayerCampaignDTO(campaign, sub);
				result.add(dto);
			}
		}
		return result;
	}

	public CampaignSubscription subscribePlayerByTerritory(String nickname, Campaign campaign,
			Map<String, Object> campaignData) throws Exception {
		Player player = playerRepository.findByNicknameIgnoreCase(nickname);
		if(player == null) {
			throw new BadRequestException("nickname doesn't exist");
		}
		if(!campaign.getTerritoryId().equals(player.getTerritoryId())) {
			throw new BadRequestException("territory doesn't match");
		}
		CampaignSubscription sub = new CampaignSubscription();
		sub.setPlayerId(player.getPlayerId());
		sub.setCampaignId(campaign.getCampaignId());
		sub.setTerritoryId(player.getTerritoryId());
		sub.setMail(player.getMail());
		sub.setSendMail(player.getSendMail());
		sub.setRegistrationDate(LocalDate.now());
		if(campaignData != null) {
			sub.setCampaignData(campaignData);
		}
		return campaignSubscriptionRepository.save(sub);
	}
}
