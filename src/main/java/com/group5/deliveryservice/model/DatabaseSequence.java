package com.group5.deliveryservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "database_sequences")
public class DatabaseSequence {

    @Id
    private String id;
    private long seq;

    public DatabaseSequence() {}

    public long getSeq() {
        return seq;
    }
}
