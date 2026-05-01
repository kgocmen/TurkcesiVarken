package com.kaan.turkcesivarken.repository;

import com.kaan.turkcesivarken.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}