package it.smartcommunitylab.playandgo.engine.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import it.smartcommunitylab.playandgo.engine.dto.PlayerInfoConsole;
import it.smartcommunitylab.playandgo.engine.dto.TrackedInstanceConsole;
import it.smartcommunitylab.playandgo.engine.dto.TrackedInstancePoly;
import it.smartcommunitylab.playandgo.engine.exception.BadRequestException;
import it.smartcommunitylab.playandgo.engine.geolocation.model.ValidationResult.TravelValidity;
import it.smartcommunitylab.playandgo.engine.manager.CampaignManager;
import it.smartcommunitylab.playandgo.engine.manager.PlayerManager;
import it.smartcommunitylab.playandgo.engine.manager.TrackedInstanceManager;
import it.smartcommunitylab.playandgo.engine.model.Campaign;
import it.smartcommunitylab.playandgo.engine.model.Player;
import it.smartcommunitylab.playandgo.engine.model.PlayerRole;
import it.smartcommunitylab.playandgo.engine.model.PlayerRole.Role;
import it.smartcommunitylab.playandgo.engine.repository.PlayerRoleRepository;
import it.smartcommunitylab.playandgo.engine.util.ErrorCode;
import it.smartcommunitylab.playandgo.engine.util.Utils;

@RestController
public class ConsoleController extends PlayAndGoController {
	
	@Autowired
	PlayerRoleRepository playerRoleRepository;
	
	@Autowired
	PlayerManager playerManager;
	
	@Autowired
	CampaignManager campaignManager;
	
	@Autowired
	TrackedInstanceManager trackedInstanceManager;
	
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
		invalidateUserRoleCache(userName);
	}
	
	@DeleteMapping("/api/console/role/territory")
	public void removeTerritoryManager(
			@RequestParam String userName,
			@RequestParam String territoryId,
			HttpServletRequest request) throws Exception {
		checkAdminRole(request);
		PlayerRole r = playerRoleRepository.findByPreferredUsernameAndRoleAndEntityId(userName, 
				Role.territory, territoryId);
		if(r != null) {
			playerRoleRepository.delete(r);
			invalidateUserRoleCache(userName);
		}
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
		invalidateUserRoleCache(userName);
	}
	
	@DeleteMapping("/api/console/role/campaign")
	public void removeCampaignManager(
			@RequestParam String userName,
			@RequestParam String campaignId,
			HttpServletRequest request) throws Exception {
		Campaign campaign = campaignManager.getCampaign(campaignId);
		if(campaign == null) {
			throw new BadRequestException("campaign not found", ErrorCode.CAMPAIGN_NOT_FOUND);
		}
		checkRole(request, Role.territory, campaign.getTerritoryId());
		PlayerRole r = playerRoleRepository.findByPreferredUsernameAndRoleAndEntityId(userName, 
				Role.campaign, campaignId);
		if(r != null) {
			playerRoleRepository.delete(r);
			invalidateUserRoleCache(userName);
		}
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
	
	@GetMapping("/api/console/role/my")
	public List<PlayerRole> getMyRoles(HttpServletRequest request) throws Exception {
		String playerId = getCurrentPreferredUsername(request);
		return playerRoleRepository.findByPreferredUsername(playerId);
	}
	
	@GetMapping("/api/console/player/search")
	public Page<PlayerInfoConsole> searchPlayersByTerritory(
			@RequestParam String territoryId,
			@RequestParam(required = false) String text,
			@ParameterObject Pageable pageRequest,
			HttpServletRequest request) throws Exception {
		checkRole(request, Role.territory, territoryId);
		Page<Player> page = playerManager.searchPlayers(territoryId, text, pageRequest);
		List<PlayerInfoConsole> result = new ArrayList<>(); 
		for(Player p : page.getContent()) {
			PlayerInfoConsole info = new PlayerInfoConsole();
			info.setPlayer(p);
			info.setCampaigns(campaignManager.getPlayerCampaigns(p.getPlayerId(), territoryId));
			result.add(info);
		}
		return new PageImpl<PlayerInfoConsole>(result, pageRequest, page.getTotalElements());
	}
	
	@GetMapping("/api/console/track/search")
	public Page<TrackedInstanceConsole> searchTrackedInstance(
			@RequestParam String territoryId,
			@RequestParam(required = false) String trackId,
			@RequestParam(required = false) String playerId,
			@RequestParam(required = false) String modeType,
			@RequestParam(required = false) 
			@Parameter(example = "UTC millis") Long dateFrom,
			@RequestParam(required = false) 
			@Parameter(example = "UTC millis") Long dateTo,
			@RequestParam(required = false) String campaignId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String scoreStatus,
			@RequestParam(required = false) Boolean toCheck,
			@ParameterObject Pageable pageRequest,
			HttpServletRequest request) throws Exception {
		checkRole(request, Role.territory, territoryId);
		Date dDateFrom = null;
		Date dDateTo = null;
		if((dateFrom != null) && (dateTo != null)) {
			dDateFrom = Utils.getUTCDate(dateFrom);
			dDateTo = Utils.getUTCDate(dateTo);
		}
		return trackedInstanceManager.searchTrackedInstance(territoryId, trackId, playerId, modeType, campaignId, status, scoreStatus,
				toCheck, dDateFrom, dDateTo, pageRequest);
	}
	
	@GetMapping("/api/console/track/detail")
	public TrackedInstancePoly getTrackedInstanceDetail(
			@RequestParam String territoryId,
			@RequestParam String trackId,
			HttpServletRequest request) throws Exception {
		checkRole(request, Role.territory, territoryId);
		return trackedInstanceManager.getTrackPolylines(territoryId, trackId);
	}
	
	@GetMapping("/api/console/track/update")
	public void updateValidationResult(
			@RequestParam String trackId,
			@RequestParam String validity,
			@RequestParam(required = false) String modeType,
			@RequestParam(required = false) Double distance,
			@RequestParam(required = false) Long duration,
			@RequestParam(required = false) String errorType,
			@RequestParam(required = false) String note,
			HttpServletRequest request) throws Exception {
		checkAdminRole(request);
		TravelValidity changedValidity = TravelValidity.valueOf(validity);
		trackedInstanceManager.updateValidationResult(trackId, changedValidity, modeType, distance, duration, errorType, note);
	}
	
	@GetMapping("/api/console/track/revalidate")
	public void revalidateTrack(
			@RequestParam String territoryId,
			@RequestParam String campaignId,
			@RequestParam(required = false) String trackedInstanceId,
			@RequestParam(required = false) 
			@Parameter(example = "UTC millis") Long dateFrom,
			@RequestParam(required = false) 
			@Parameter(example = "UTC millis") Long dateTo,			
			HttpServletRequest request) throws Exception {
		checkRole(request, Role.territory, territoryId);
		if(Utils.isNotEmpty(trackedInstanceId)) {
			trackedInstanceManager.revalidateTrack(territoryId, campaignId, trackedInstanceId);
		} else {
			Date dDateFrom = null;
			Date dDateTo = null;
			if((dateFrom != null) && (dateTo != null)) {
				dDateFrom = Utils.getUTCDate(dateFrom);
				dDateTo = Utils.getUTCDate(dateTo);
				trackedInstanceManager.revalidateTracks(territoryId, campaignId, dDateFrom, dDateTo);
			}
		}
	}
	
	@PutMapping("/api/console/track/check")
	public void modifyToCheck(
	        @RequestParam String trackId,
	        @RequestParam boolean toCheck,
	        HttpServletRequest request) throws Exception {
	    checkAdminRole(request);
	    trackedInstanceManager.modifyToCheck(trackId, toCheck);
	}

}
