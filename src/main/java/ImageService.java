    import jakarta.inject.Inject;
    import jakarta.inject.Singleton;
    import jakarta.ws.rs.core.MultivaluedMap;
    import org.apache.commons.io.IOUtils;
    import org.eclipse.microprofile.config.inject.ConfigProperty;
    import org.jboss.resteasy.plugins.providers.multipart.InputPart;
    import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

    import java.io.File;
    import java.io.IOException;
    import java.io.InputStream;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.nio.file.StandardOpenOption;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;

    @Singleton
    public class ImageService {

        @Inject
        ImageRepository imageRepository;

        @ConfigProperty(name = "upload.directory")
        String UPLOAD_DIR;

        public String uploadFile(MultipartFormDataInput input) {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            List<String> fileNames = new ArrayList<>();
            List<InputPart> inputParts = uploadForm.get("file");
            String fileName = null;
            for (InputPart inputPart : inputParts) {
                try {
                    MultivaluedMap<String, String> header = inputPart.getHeaders();
                    fileName = getFileName(header);
                    fileNames.add(fileName);
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    writeFile(inputStream, fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Save image entities to the database
            saveImageEntities(fileNames);
            return "Files Successfully Uploaded";
        }

        public List<String> getAllFiles() {
            File customDir = new File(UPLOAD_DIR);
            File[] files = customDir.listFiles();
            List<String> fileNames = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    fileNames.add(file.getName());
                }
            }
            return fileNames;
        }

        private void writeFile(InputStream inputStream, String fileName) throws IOException {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            File customDir = new File(UPLOAD_DIR);
            fileName = customDir.getAbsolutePath() + File.separator + fileName;
            Files.write(Paths.get(fileName), bytes, StandardOpenOption.CREATE_NEW);
        }

        private String getFileName(MultivaluedMap<String, String> header) {
            String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
            for (String filename : contentDisposition) {
                if ((filename.trim().startsWith("filename"))) {
                    String[] name = filename.split("=");
                    String finalFileName = name[1].trim().replaceAll("\"", "");
                    return finalFileName;
                }
            }
            return "";
        }

        void saveImageEntities(List<String> fileNames) {
            for (String fileName : fileNames) {
                ImageEntity imageEntity = new ImageEntity();
                imageEntity.setImageUrl(fileName);
                imageRepository.persist(imageEntity);
            }
        }
        public List<ImageEntity> getImages() {
            return imageRepository.listAll();
        }


    }
