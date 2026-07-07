package com.nithiwut.wealthhub.asset.repository;

import com.nithiwut.wealthhub.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
}
