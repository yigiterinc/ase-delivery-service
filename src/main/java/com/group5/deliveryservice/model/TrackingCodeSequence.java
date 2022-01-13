package com.group5.deliveryservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tracking_code_sequence")
public class TrackingCodeSequence {

    @Id
    private String id;
    private String seq;

    public TrackingCodeSequence(String id, String seq) {
        this.id = id;
        this.seq = seq;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) { this.seq = seq; }
}
