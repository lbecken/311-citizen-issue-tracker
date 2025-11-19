package gov.lby.cityissuetracker.repository;

import gov.lby.cityissuetracker.entity.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    Optional<Department> findByName(String name);

    Optional<Department> findByNameIgnoreCase(String name);
}
