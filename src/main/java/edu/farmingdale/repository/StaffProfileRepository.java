package edu.farmingdale.repository;

import edu.farmingdale.model.StaffProfile;

import java.util.Optional;

public interface StaffProfileRepository extends Repository<StaffProfile, Integer> {
    Optional<StaffProfile> findByUsername(String username);
}
