package it.smartcommunitylab.playandgo.engine.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.playandgo.engine.dto.PlayerCampaign;
import it.smartcommunitylab.playandgo.engine.exception.BadRequestException;
import it.smartcommunitylab.playandgo.engine.manager.CampaignManager;
import it.smartcommunitylab.playandgo.engine.manager.TerritoryManager;
import it.smartcommunitylab.playandgo.engine.model.Campaign;
import it.smartcommunitylab.playandgo.engine.model.CampaignSubscription;
import it.smartcommunitylab.playandgo.engine.model.Player;
import it.smartcommunitylab.playandgo.engine.model.PlayerRole.Role;
import it.smartcommunitylab.playandgo.engine.util.Utils;

@RestController
public class CampaignController extends PlayAndGoController {
	private static transient final Logger logger = LoggerFactory.getLogger(CampaignController.class);
	
	@Autowired
	CampaignManager campaignManager;
	@Autowired
	TerritoryManager territoryManager;
	
	@PostMapping("/api/campaign")
	public void addCampaign(
			@RequestBody Campaign campaign,
			HttpServletRequest request) throws Exception {
		checkAdminRole(request);
		campaignManager.addCampaign(campaign);
	}
	
	@GetMapping("/api/campaign/{campaignId}")
	public Campaign getCampaign(
			@PathVariable String campaignId,
			HttpServletRequest request) throws Exception {
		return campaignManager.getCampaign(campaignId);
	}
	
	@GetMapping("/api/campaign")
	public List<Campaign> getCampaigns(
			@RequestParam(required=false) String territoryId,
			HttpServletRequest request) throws Exception {
		if(Utils.isNotEmpty(territoryId)) {
			return campaignManager.getCampaignsByTerritory(territoryId);
		}
		return campaignManager.getCampaigns();
	}
	
	@DeleteMapping("/api/campaign/{campaignId}")
	public Campaign deleteCampaign(
			@PathVariable String campaignId,
			HttpServletRequest request) throws Exception {
		checkAdminRole(request);
		return campaignManager.deleteCampaign(campaignId);
	}
	
	@GetMapping("/api/campaign/my")
	public List<PlayerCampaign> getMyCampaigns(
			HttpServletRequest request) throws Exception {
		Player player = getCurrentPlayer(request);
		return campaignManager.getPlayerCampaigns(player.getPlayerId());
	}
	
	@PostMapping("/api/campaign/{campaignId}/subscribe")
	public CampaignSubscription subscribeCampaign(
			@PathVariable String campaignId,
			@RequestBody(required = false) Map<String, Object> campaignData,
			HttpServletRequest request) throws Exception {
		Player player = getCurrentPlayer(request);
		return campaignManager.subscribePlayer(player, campaignId, campaignData);
	}
	
	@PutMapping("/api/campaign/{campaignId}/unsubscribe")
	public CampaignSubscription unsubscribeCampaign(
			@PathVariable String campaignId,
			HttpServletRequest request) throws Exception {
		Player player = getCurrentPlayer(request);
		return campaignManager.unsubscribePlayer(player, campaignId);
	}
	
	@PostMapping("/api/campaign/{campaignId}/subscribe/territory")
	public CampaignSubscription subscribeCampaignByTerritory(
			@PathVariable String campaignId,
			@RequestParam String nickname,
			@RequestBody Map<String, Object> campaignData,
			HttpServletRequest request) throws Exception {
		Campaign campaign = campaignManager.getCampaign(campaignId);
		if(campaign == null) {
			throw new BadRequestException("campaign doesn't exist");
		}
		checkRole(request, Role.territory, campaign.getTerritoryId());
		return campaignManager.subscribePlayerByTerritory(nickname, campaign, campaignData);
	}
	

}
