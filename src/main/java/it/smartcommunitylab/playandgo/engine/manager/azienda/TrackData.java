package it.smartcommunitylab.playandgo.engine.manager.azienda;

import java.util.ArrayList;
import java.util.List;

public class TrackData {
	private Long startTime;
	private String multimodalId;
	private List<LegData> legs = new ArrayList<>();
	
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public List<LegData> getLegs() {
		return legs;
	}
	public void setLegs(List<LegData> legs) {
		this.legs = legs;
	}
    public String getMultimodalId() {
        return multimodalId;
    }
    public void setMultimodalId(String multimodalId) {
        this.multimodalId = multimodalId;
    }
}
