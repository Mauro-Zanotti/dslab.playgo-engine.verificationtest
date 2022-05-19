package it.smartcommunitylab.playandgo.engine.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;
import it.smartcommunitylab.playandgo.engine.dto.TrackedInstanceInfo;
import it.smartcommunitylab.playandgo.engine.geolocation.model.GeolocationsEvent;
import it.smartcommunitylab.playandgo.engine.manager.TrackedInstanceManager;
import it.smartcommunitylab.playandgo.engine.model.Player;
import it.smartcommunitylab.playandgo.engine.util.Utils;

@RestController
public class TrackController extends PlayAndGoController {
	private static transient final Logger logger = LoggerFactory.getLogger(TrackController.class);
	
	@Autowired
	TrackedInstanceManager trackedInstanceManager;
	
	@PostMapping("/api/track/player/geolocations")
	public void storeGeolocationEvent(
			@RequestBody(required = false) GeolocationsEvent geolocationsEvent,
			HttpServletRequest request) throws Exception {
		Player player = getCurrentPlayer(request);
		trackedInstanceManager.storeGeolocationEvents(geolocationsEvent, player.getPlayerId(), player.getTerritoryId());
	}
	
	@GetMapping("/api/track/player")
	public Page<TrackedInstanceInfo> getTrackedInstanceInfoList(
			@RequestParam(required = false) 
			@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
			@ApiParam(value = "yyyy-MM-dd HH:mm:ss") Date dateFrom,
			@RequestParam(required = false) 
			@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
			@ApiParam(value = "yyyy-MM-dd HH:mm:ss") Date dateTo,
			@RequestParam(required = false) String campaignId,
			Pageable pageRequest,
			HttpServletRequest request) throws Exception {
		Player player = getCurrentPlayer(request);
		if(Utils.isNotEmpty(campaignId)) {
			return trackedInstanceManager.getTrackedInstanceInfoList(player.getPlayerId(), campaignId, dateFrom, dateTo, pageRequest);
		} else {
			return trackedInstanceManager.getTrackedInstanceInfoList(player.getPlayerId(), dateFrom, dateTo, pageRequest);
		}
	}
	
	@GetMapping("/api/track/player/{trackedInstanceId}")
	public TrackedInstanceInfo getTrackedInstanceInfo(
			@PathVariable String trackedInstanceId,	
			@RequestParam(required = false) String campaignId,
			HttpServletRequest request) throws Exception {
		Player player = getCurrentPlayer(request);
		if(Utils.isNotEmpty(campaignId)) {
			return trackedInstanceManager.getTrackedInstanceInfo(player.getPlayerId(), trackedInstanceId, campaignId);
		} else {
			return trackedInstanceManager.getTrackedInstanceInfo(player.getPlayerId(), trackedInstanceId);
		}
	}
	
	
	
	
}
