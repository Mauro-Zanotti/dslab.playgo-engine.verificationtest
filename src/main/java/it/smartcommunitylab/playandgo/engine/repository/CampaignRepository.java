package it.smartcommunitylab.playandgo.engine.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.playandgo.engine.model.Campaign;
import it.smartcommunitylab.playandgo.engine.model.Campaign.Type;

@Repository
public interface CampaignRepository extends MongoRepository<Campaign, String> {

	public List<Campaign> findByTerritoryId(String territoryId, Sort sort); 
	
	public List<Campaign> findByType(Type type, Sort sort);
	
	@Query ("{'defaultForTerritory': true, 'territoryId' : ?0}")
	public Campaign findDefaultByTerritoryId(String territoryId);
}
