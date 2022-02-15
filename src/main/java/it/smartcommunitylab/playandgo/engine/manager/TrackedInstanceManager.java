package it.smartcommunitylab.playandgo.engine.manager;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.playandgo.engine.geolocation.model.GeolocationsEvent;
import it.smartcommunitylab.playandgo.engine.geolocation.model.ValidationResult;
import it.smartcommunitylab.playandgo.engine.model.CampaignPlayerTrack;
import it.smartcommunitylab.playandgo.engine.model.CampaignSubscription;
import it.smartcommunitylab.playandgo.engine.model.TrackedInstance;
import it.smartcommunitylab.playandgo.engine.mq.ManageValidateTripRequest;
import it.smartcommunitylab.playandgo.engine.mq.MessageQueueManager;
import it.smartcommunitylab.playandgo.engine.mq.ValidateCampaignTripRequest;
import it.smartcommunitylab.playandgo.engine.mq.ValidateTripRequest;
import it.smartcommunitylab.playandgo.engine.repository.CampaignPlayerTrackRepository;
import it.smartcommunitylab.playandgo.engine.repository.CampaignSubscriptionRepository;
import it.smartcommunitylab.playandgo.engine.repository.TrackedInstanceRepository;
import it.smartcommunitylab.playandgo.engine.validation.GeolocationsProcessor;
import it.smartcommunitylab.playandgo.engine.validation.ValidationService;

@Component
public class TrackedInstanceManager implements ManageValidateTripRequest {
	private static Log logger = LogFactory.getLog(TrackedInstanceManager.class);
	
	private static final String TRACKEDINSTANCE = "trackedInstances";
	
	@Autowired
	private MessageQueueManager queueManager;

	@Autowired
	private TrackedInstanceRepository trackedInstanceRepository;
	
	@Autowired
	private CampaignSubscriptionRepository campaignSubscriptionRepository;
	
	@Autowired
	private CampaignPlayerTrackRepository campaignPlayerTrackRepository;

	@Autowired
	private GeolocationsProcessor geolocationsProcessor;
	
	@Autowired
	private ValidationService validationService;
	
	@PostConstruct
	public void init() {
		queueManager.setManageValidateTripRequest(this);
	}
	
	public void storeGeolocationEvents(GeolocationsEvent geolocationsEvent, String playerId, String territoryId) throws Exception {
		List<TrackedInstance> list = geolocationsProcessor.storeGeolocationEvents(geolocationsEvent, playerId, territoryId);
		for (TrackedInstance ti : list) {
			ValidateTripRequest request = new ValidateTripRequest(playerId, territoryId, ti.getId());
			queueManager.sendValidateTripRequest(request);
		}
	}
	
	public List<TrackedInstance> getPlayerTrakedInstaces(String playerId) {
		return trackedInstanceRepository.findByUserId(playerId, Sort.by(Sort.Direction.DESC, "day"));
	}
	
	public TrackedInstance getTrackedInstance(String trackId) {
		return trackedInstanceRepository.findById(trackId).orElse(null);
	}
	
	public void updateValidationResult(TrackedInstance track, ValidationResult result) {
		track.setValidationResult(result);
		trackedInstanceRepository.save(track);
	}

	@Override
	public void validateTripRequest(ValidateTripRequest msg) {
		TrackedInstance track = getTrackedInstance(msg.getTrackedInstanceId());
		if(track != null) {
			try {
				ValidationResult validationResult = validationService.validateFreeTracking(track.getGeolocationEvents(), 
						track.getFreeTrackingTransport(), msg.getTerritoryId());
				updateValidationResult(track, validationResult);
				if(validationResult.isValid()) {
					List<CampaignSubscription> list = campaignSubscriptionRepository.findByPlayerIdAndTerritoryId(msg.getPlayerId(), msg.getTerritoryId());
					for(CampaignSubscription sub : list) {
						CampaignPlayerTrack pTrack = new CampaignPlayerTrack();
						pTrack.setPlayerId(msg.getPlayerId());
						pTrack.setCampaignId(sub.getCampaignId());
						pTrack.setCampaignSubscriptionId(sub.getId());
						pTrack.setTrackedInstanceId(track.getId());
						pTrack.setTerritoryId(msg.getTerritoryId());
						campaignPlayerTrackRepository.save(pTrack);
						ValidateCampaignTripRequest request = new ValidateCampaignTripRequest(msg.getPlayerId(), 
								msg.getTerritoryId(), track.getId(), sub.getCampaignId(), sub.getId(), pTrack.getId());
						queueManager.sendValidateCampaignTripRequest(request);
					}
				}
			} catch (Exception e) {
				logger.warn("validateTripRequest error:" + e.getMessage());
			}			
		}
	}

}
