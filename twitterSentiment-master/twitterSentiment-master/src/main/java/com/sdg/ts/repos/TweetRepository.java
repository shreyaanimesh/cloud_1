package com.sdg.ts.repos;


import com.sdg.ts.model.Tweet;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TweetRepository extends CrudRepository<Tweet, Long> {

    @Query("DELETE FROM Tweet WHERE statusId = :statusId")
    @Transactional
    @Modifying
    public void deleteByStatusId(@Param("statusId") Long statusId);


    public Tweet findByStatusId(Long statusId);


}
