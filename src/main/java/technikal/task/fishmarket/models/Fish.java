package technikal.task.fishmarket.models;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fish")
@Data
public class Fish {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private String name;
	private double price;
	private Date catchDate;
	private String primaryImageFileName;
	@OneToMany(mappedBy = "fish", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SecondaryFishImages> secondaryImages;

}
