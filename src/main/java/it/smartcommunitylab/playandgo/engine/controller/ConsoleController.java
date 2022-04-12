package it.smartcommunitylab.playandgo.engine.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.playandgo.engine.exception.BadRequestException;
import it.smartcommunitylab.playandgo.engine.manager.CampaignManager;
import it.smartcommunitylab.playandgo.engine.model.Campaign;
import it.smartcommunitylab.playandgo.engine.model.PlayerRole;
import it.smartcommunitylab.playandgo.engine.model.PlayerRole.Role;
import it.smartcommunitylab.playandgo.engine.repository.CampaignRepository;
import it.smartcommunitylab.playandgo.engine.repository.PlayerRoleRepository;
import it.smartcommunitylab.playandgo.engine.util.ErrorCode;

@RestController
public class ConsoleController extends PlayAndGoController {
	
	@Autowired
	PlayerRoleRepository playerRoleRepository;
	
	@Autowired
	CampaignManager campaignManager;
	
	@PostMapping("/api/console/role/territory")
	public void addTerritoryManager(
			@RequestParam String userName,
			@RequestParam String territoryId,
			HttpServletRequest request) throws Exception {
		checkAdminRole(request);
		PlayerRole r = new PlayerRole();
		r.setPreferredUsername(userName);
		r.setEntityId(territoryId);
		r.setRole(Role.territory);
		playerRoleRepository.save(r);
	}
	
	@PostMapping("/api/console/role/campaign")
	public void addCampaignManager(
			@RequestParam String userName,
			@RequestParam String campaignId,
			HttpServletRequest request) throws Exception {
		Campaign campaign = campaignManager.getCampaign(campaignId);
		if(campaign == null) {
			throw new BadRequestException("campaign not found", ErrorCode.CAMPAIGN_NOT_FOUND);
		}
		checkRole(request, Role.territory, campaign.getTerritoryId());
		PlayerRole r = new PlayerRole();
		r.setPreferredUsername(userName);
		r.setEntityId(campaignId);
		r.setRole(Role.campaign);
		playerRoleRepository.save(r);
	}
	
	@GetMapping("/api/console/role/territory")
	public List<PlayerRole> getTerritoryManager(
			@RequestParam String territoryId,
			HttpServletRequest request) throws Exception {
		checkAdminRole(request);
		return playerRoleRepository.findByRoleAndEntityId(Role.territory, territoryId);
	}
	
	@GetMapping("/api/console/role/campaign")
	public List<PlayerRole> getCampaignManager(
			@RequestParam String campaignId,
			HttpServletRequest request) throws Exception {
		Campaign campaign = campaignManager.getCampaign(campaignId);
		if(campaign == null) {
			throw new BadRequestException("campaign not found", ErrorCode.CAMPAIGN_NOT_FOUND);
		}
		checkRole(request, Role.territory, campaign.getTerritoryId());
		return playerRoleRepository.findByRoleAndEntityId(Role.campaign, campaignId);
	}


}
