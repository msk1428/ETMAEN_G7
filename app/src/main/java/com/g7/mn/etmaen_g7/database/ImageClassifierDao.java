package com.g7.mn.etmaen_g7.database;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ImageClassifierDao {


    @Query("SELECT * FROM addentry")
    LiveData<List<AddEntry>> loadAllClassifier();

    @Insert
    void insertClassifier(AddEntry addEntry);

    @Insert
    void insertVerifiedImage(VerifiedEntry verifiedEntry);

    @Query("SELECT * FROM verifiedentry")
    LiveData<List<VerifiedEntry>> loadAllVerifiedEntry();

    @Query("SELECT * FROM verifiedentry WHERE id = :id")
    LiveData<VerifiedEntry> loadVerifiedImageById(int id);

    @Query("SELECT * FROM addentry WHERE persistedid = :persistedid")
    AddEntry loadEntryByPersistedFaceId(String persistedid);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateClassifier(AddEntry addEntry);

    @Delete
    void deleteClassifier(AddEntry addEntry);

    @Query("SELECT * FROM addentry WHERE id = :id")
    LiveData<AddEntry> loadClassifierById(int id);
}

