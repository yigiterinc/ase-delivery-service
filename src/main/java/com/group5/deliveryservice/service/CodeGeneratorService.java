package com.group5.deliveryservice.service;

import com.group5.deliveryservice.model.TrackingCodeSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class CodeGeneratorService {

    final static Pattern NUMBER = Pattern.compile("\\d+");

    private MongoOperations mongoOperations;

    private static void increment(TrackingCodeSequence trackingCodeSequence) {
         trackingCodeSequence.setSeq(NUMBER.matcher(trackingCodeSequence.getSeq())
                .replaceFirst(s -> String.format(
                        "%0" + s.group().length() + "d",
                        Integer.parseInt(s.group()) + 1)));
    }

    @Autowired
    public CodeGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String generateSequence(String seqName) {
        TrackingCodeSequence counter = mongoOperations.findOne(query(where("_id").is(seqName)), TrackingCodeSequence.class);
        if (!Objects.isNull(counter)) {
            increment(counter);
            mongoOperations.save(counter);
            return counter.getSeq();
        } else {
            String initialValue = "00000001";
            mongoOperations.save(new TrackingCodeSequence(seqName, initialValue), seqName);
            return initialValue;
        }
    }
}
