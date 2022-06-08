package it.smartcommunitylab.playandgo.engine.campaign;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.playandgo.engine.exception.ServiceException;
import it.smartcommunitylab.playandgo.engine.manager.PlayerCampaignPlacingManager;
import it.smartcommunitylab.playandgo.engine.manager.azienda.LegData;
import it.smartcommunitylab.playandgo.engine.manager.azienda.LegResult;
import it.smartcommunitylab.playandgo.engine.manager.azienda.PgAziendaleManager;
import it.smartcommunitylab.playandgo.engine.manager.azienda.TrackData;
import it.smartcommunitylab.playandgo.engine.manager.azienda.TrackResult;
import it.smartcommunitylab.playandgo.engine.model.CampaignPlayerTrack;
import it.smartcommunitylab.playandgo.engine.model.TrackedInstance;
import it.smartcommunitylab.playandgo.engine.model.Campaign.Type;
import it.smartcommunitylab.playandgo.engine.model.CampaignPlayerTrack.ScoreStatus;
import it.smartcommunitylab.playandgo.engine.mq.ManageValidateCampaignTripRequest;
import it.smartcommunitylab.playandgo.engine.mq.MessageQueueManager;
import it.smartcommunitylab.playandgo.engine.mq.UpdateCampaignTripRequest;
import it.smartcommunitylab.playandgo.engine.mq.ValidateCampaignTripRequest;
import it.smartcommunitylab.playandgo.engine.repository.CampaignPlayerTrackRepository;
import it.smartcommunitylab.playandgo.engine.repository.TrackedInstanceRepository;
import it.smartcommunitylab.playandgo.engine.util.Utils;

@Component
public class CompanyCampaignTripValidator implements ManageValidateCampaignTripRequest {
	private static Logger logger = LoggerFactory.getLogger(CompanyCampaignTripValidator.class);
	
	@Autowired
	MessageQueueManager queueManager;
	
	@Autowired
	PgAziendaleManager pgAziendaleManager;
	
	@Autowired
	CampaignPlayerTrackRepository campaignPlayerTrackRepository;
	
	@Autowired
	TrackedInstanceRepository trackedInstanceRepository;
	
	@Autowired
	PlayerCampaignPlacingManager playerReportManager;

	@PostConstruct
	public void init() {
		queueManager.setManageValidateCampaignTripRequest(this, Type.company);
	}

	@Override
	public void validateTripRequest(ValidateCampaignTripRequest msg) {
		CampaignPlayerTrack playerTrack = campaignPlayerTrackRepository.findById(msg.getCampaignPlayerTrackId()).orElse(null);
		if(playerTrack != null) {
			TrackData trackData = getTrackData(msg.getTrackedInstanceId());
			if(trackData != null) {
				try {
					TrackResult trackResult = pgAziendaleManager.validateTrack(msg.getCampaignId(), msg.getPlayerId(), trackData);
					if(!trackResult.getValid()) {
						errorPlayerTrack(playerTrack, trackResult.getErrorCode());
					} else {
						LegResult legResult = trackResult.getLegs().get(0);
						populatePlayerTrack(playerTrack, legResult.getMean(), legResult.getValidDistance());
						playerReportManager.updatePlayerCampaignPlacings(playerTrack);
					}
				} catch (ServiceException e) {
					logger.warn("validateTripRequest error:" + e.getMessage());
					//TODO gestione errore
//					playerTrack.setValid(false);
//					playerTrack.setErrorCode(e.getCode());
//					campaignPlayerTrackRepository.save(playerTrack);
				}
			}			
		}	
	}
	
	private void populatePlayerTrack(CampaignPlayerTrack playerTrack, String modeType, double distance) {
		playerTrack.setScoreStatus(ScoreStatus.COMPUTED);
		playerTrack.setValid(true);
		playerTrack.setModeType(modeType);
		playerTrack.setDistance(distance);
		playerTrack.setCo2(Utils.getSavedCo2(modeType, distance));
		campaignPlayerTrackRepository.save(playerTrack);
	}
	
	private void errorPlayerTrack(CampaignPlayerTrack playerTrack, String errorCode) {
		playerTrack.setScoreStatus(ScoreStatus.COMPUTED);
		playerTrack.setValid(false);
		playerTrack.setErrorCode(errorCode);	
		campaignPlayerTrackRepository.save(playerTrack);
	}
	
	private TrackData getTrackData(String trackedInstanceId) {
		TrackedInstance track = trackedInstanceRepository.findById(trackedInstanceId).orElse(null);
		if(track != null) {
			TrackData trackData = new TrackData();
			trackData.setStartTime(track.getStartTime().getTime());
			LegData legData = new LegData();
			legData.setId(track.getId());
			legData.setMean(track.getValidationResult().getValidationStatus().getModeType().toString());
			legData.setDistance(track.getValidationResult().getValidationStatus().getDistance());
			legData.getPoints().addAll(track.getGeolocationEvents());
			trackData.getLegs().add(legData);
			return trackData;
		}
		return null;
	}
	
	@Override
	public void invalidateTripRequest(ValidateCampaignTripRequest msg) {
		CampaignPlayerTrack playerTrack = campaignPlayerTrackRepository.findById(msg.getCampaignPlayerTrackId()).orElse(null);
		if(playerTrack != null) {
			try {
				pgAziendaleManager.invalidateTrack(playerTrack.getCampaignId(), 
						playerTrack.getPlayerId(), playerTrack.getTrackedInstanceId());
			} catch (ServiceException e) {
				logger.warn("invalidateTripRequest error:" + e.getMessage());
				//TODO gestione errore
			}
			TrackedInstance track = trackedInstanceRepository.findById(msg.getTrackedInstanceId()).orElse(null);
			errorPlayerTrack(playerTrack, track.getValidationResult().getValidationStatus().getError().toString());
			playerReportManager.removePlayerCampaignPlacings(playerTrack);							
		}
	}

	@Override
	public void updateTripRequest(UpdateCampaignTripRequest msg) {
		// TODO Auto-generated method stub
		CampaignPlayerTrack playerTrack = campaignPlayerTrackRepository.findById(msg.getCampaignPlayerTrackId()).orElse(null);
		TrackedInstance track = trackedInstanceRepository.findById(playerTrack.getTrackedInstanceId()).orElse(null);
		if((playerTrack != null) && (track != null)) {
			double delta = track.getValidationResult().getValidationStatus().getDistance() - playerTrack.getDistance();
			if(delta > 0) {
				try {
					TrackResult trackResult = pgAziendaleManager.updateTrack(playerTrack.getCampaignId(), 
							playerTrack.getPlayerId(), track.getId(), delta);
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

}
