package com.enigmacamp.pawtner.repository;

import java.util.Optional;
import java.util.UUID;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    List<Business> findAllByOwner_Id(UUID id);
    Optional<Business> findByOwner(User owner);

    @Query(value = "SELECT ST_Distance(b.location, ST_SetSRID(:userLocation, 4326)) FROM businesses b WHERE b.id = :businessId", nativeQuery = true)
    Double calculateDistanceToBusiness(@Param("businessId") UUID businessId, @Param("userLocation") Point userLocation);

    @Query(value = """
        SELECT * FROM businesses b
        WHERE ST_DWithin(b.location, ST_SetSRID(:userLocation, 4326), :distanceInMeters)
        AND (:hasEmergencyServices IS NULL OR b.has_emergency_services = :hasEmergencyServices)
        AND (:statusRealtime IS NULL OR b.status_realtime = :statusRealtime)
    """, nativeQuery = true)
    List<Business> findNearbyBusinessesWithFilters(
        @Param("userLocation") Point userLocation,
        @Param("distanceInMeters") double distanceInMeters,
        @Param("hasEmergencyServices") Boolean hasEmergencyServices,
        @Param("statusRealtime") String statusRealtime
    );
}
