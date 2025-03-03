package technikal.task.fishmarket.controllers;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import technikal.task.fishmarket.models.Fish;
import technikal.task.fishmarket.models.FishDto;
import technikal.task.fishmarket.models.SecondaryFishImages;
import technikal.task.fishmarket.services.FishRepository;
import technikal.task.fishmarket.services.SecondaryFishImagesRepository;

@Controller
@RequestMapping("/fish")
public class FishController {

	@Autowired
	private FishRepository repo;
	@Autowired
	private SecondaryFishImagesRepository secondaryFishImagesRepository;
	private static final String UPLOAD_IMAGES_DIR = "public/images/";

	@GetMapping( {"", "/"})
	public String showFishList(Model model) {
		List<Fish> fishlist = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("fishlist", fishlist);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			System.out.println("Roles: " + authentication.getAuthorities());
		}
		return "index";
	}

	@GetMapping("/create")
	public String showCreatePage(Model model) {
		FishDto fishDto = new FishDto();
		model.addAttribute("fishDto", fishDto);
		return "createFish";
	}

	@GetMapping("/delete")
	public String deleteFish(@RequestParam int id) {

		try {

			Fish fish = repo.findById(id).get();

			Path imagePath = Paths.get(UPLOAD_IMAGES_DIR+fish.getPrimaryImageFileName());
			Files.delete(imagePath);
			repo.delete(fish);
			deleteSecondaryImages(fish);


		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception: " + ex.getMessage());
		}

		return "redirect:/fish";
	}

	private void deleteSecondaryImages(Fish fish) throws IOException {
		Path imagePath;
		for (SecondaryFishImages secondaryFishImage : fish.getSecondaryImages()){
			imagePath = Paths.get(UPLOAD_IMAGES_DIR+secondaryFishImage.getImageFilePath());
			Files.delete(imagePath);
			secondaryFishImagesRepository.delete(secondaryFishImage);
		}
	}

	@PostMapping("/create")
	public String addFish(@Valid @ModelAttribute FishDto fishDto, BindingResult result) {

		if(fishDto.getPrimaryImageFile().isEmpty()) {
			result.addError(new FieldError("fishDto", "imageFile", "Потрібне фото рибки"));
		}
		if(result.hasErrors()) {
			return "createFish";
		}
		try {
			createImagesDirectoryIfNotExists();
		}catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		MultipartFile primaryImageFile = fishDto.getPrimaryImageFile();
		Date catchDate = new Date();
		String  storageFileName = catchDate.getTime() + "_" + primaryImageFile.getOriginalFilename();
        try {
            saveImageToDirectory(primaryImageFile, UPLOAD_IMAGES_DIR, storageFileName);
        } catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
        }

		List<SecondaryFishImages> secondaryFishImagesList = saveSecondaryImagesToDirectory(fishDto, catchDate);
		Fish fish = new Fish();
		saveFishToDb(fishDto, secondaryFishImagesList, fish, catchDate, storageFileName);

		return "redirect:/fish";
	}

	/**
	 * Saves fish information to the database.
	 *
	 * @param fishDto                 DTO containing fish details, including name and price.
	 * @param secondaryFishImagesList  List of secondary fish images.
	 * @param fishToSave               The {@link Fish} entity to be saved.
	 * @param catchDate                The date when the fish was caught.
	 * @param storageFileName          The filename of the primary image.
	 */
	private void saveFishToDb(FishDto fishDto, List<SecondaryFishImages> secondaryFishImagesList, Fish fishToSave, Date catchDate, String storageFileName) {
		secondaryFishImagesList.forEach(secondaryFishImages -> secondaryFishImages.setFish(fishToSave));
		fishToSave.setCatchDate(catchDate);
		fishToSave.setPrimaryImageFileName(storageFileName);
		fishToSave.setName(fishDto.getName());
		fishToSave.setPrice(fishDto.getPrice());
		fishToSave.setSecondaryImages(secondaryFishImagesList);
		repo.save(fishToSave);
	}

	/**
	 * Saves secondary fish images to the specified directory and returns a list of {@link SecondaryFishImages} entities.
	 *
	 * @param fishDto   The DTO containing secondary image files.
	 * @param catchDate The date when the fish was caught, used to generate unique filenames.
	 * @return A list of {@link SecondaryFishImages} objects with the saved image file paths.
	 */
	private static List<SecondaryFishImages> saveSecondaryImagesToDirectory(FishDto fishDto, Date catchDate) {
		List<MultipartFile> secondaryImagesFiles = fishDto.getSecondaryImagesFiles();
		List<SecondaryFishImages> secondaryFishImagesList = new ArrayList<>();
		for (MultipartFile secondaryImagesFile : secondaryImagesFiles) {
			String secondaryImageStorageFileName = catchDate.getTime() + "_" + secondaryImagesFile.getOriginalFilename();
			try {
				saveImageToDirectory(secondaryImagesFile, UPLOAD_IMAGES_DIR, secondaryImageStorageFileName);
			} catch (IOException e) {
				System.out.println("Exception: " + e.getMessage());
			}
			SecondaryFishImages secondaryFishImages = new SecondaryFishImages();
			secondaryFishImages.setImageFilePath(secondaryImageStorageFileName);
			secondaryFishImagesList.add(secondaryFishImages);
		}
		return secondaryFishImagesList;
	}

	private static void saveImageToDirectory(MultipartFile primaryImageFile, String uploadDir, String storageFileName) throws IOException {
		try(InputStream inputStream = primaryImageFile.getInputStream()){
			Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static String createImagesDirectoryIfNotExists() throws IOException {
		String uploadDir = UPLOAD_IMAGES_DIR;
		Path uploadPath = Paths.get(uploadDir);

		if(!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		return uploadDir;
	}

}
