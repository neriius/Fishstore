package technikal.task.fishmarket.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "secondary_fish_images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecondaryFishImages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imageFilePath;

    @ManyToOne
    @JoinColumn(name = "fish_id")
    private Fish fish;
}
