package it.smartcommunitylab.playandgo.engine.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.playandgo.engine.model.PlayerStatsTransport;

@Repository
public interface PlayerStatsTransportRepository extends MongoRepository<PlayerStatsTransport, String> {
	
	public PlayerStatsTransport findByPlayerIdAndCampaignIdAndModeTypeAndGlobal(String playerId, String campaignId, 
			String modeType, Boolean global);
	
	public PlayerStatsTransport findByPlayerIdAndCampaignIdAndModeTypeAndGlobalAndWeeklyDay(String playerId, String campaignId, 
			String modeType, Boolean global, LocalDate weeklyDay);
	
	public List<PlayerStatsTransport> findByPlayerIdAndCampaignIdAndGlobal(String playerId, String campaignId, Boolean global);
	
	public List<PlayerStatsTransport> findByPlayerIdAndCampaignIdAndGlobalAndWeeklyDay(String playerId, String campaignId, 
			Boolean global, LocalDate weeklyDay);
}
