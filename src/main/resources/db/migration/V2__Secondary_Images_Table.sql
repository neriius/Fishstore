
CREATE TABLE secondary_fish_images (id BIGINT NOT NULL auto_increment, image_file_path VARCHAR(255), fish_id INTEGER, PRIMARY KEY (id)) engine=InnoDB;
ALTER TABLE secondary_fish_images ADD CONSTRAINT fk_secondary_fish_fish  FOREIGN KEY (fish_id) REFERENCES fish (id);