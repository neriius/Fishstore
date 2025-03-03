package technikal.task.fishmarket.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import technikal.task.fishmarket.models.SecondaryFishImages;

@Repository
public interface SecondaryFishImagesRepository extends JpaRepository<SecondaryFishImages, Long> {
}
