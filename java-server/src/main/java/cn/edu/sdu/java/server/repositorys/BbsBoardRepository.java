package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BbsBoardRepository extends JpaRepository<BbsBoard, Long> {

    Optional<BbsBoard> findByName(String name);

    Boolean existsByName(String name);
}
