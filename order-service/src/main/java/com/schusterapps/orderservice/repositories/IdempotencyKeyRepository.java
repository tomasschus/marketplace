package com.schusterapps.orderservice.repositories;

import com.schusterapps.orderservice.models.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}
