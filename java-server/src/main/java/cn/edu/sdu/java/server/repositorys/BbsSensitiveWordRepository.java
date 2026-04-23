package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsSensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsSensitiveWordRepository extends JpaRepository<BbsSensitiveWord, Long> {

    List<BbsSensitiveWord> findByLevel(Integer level);
}
