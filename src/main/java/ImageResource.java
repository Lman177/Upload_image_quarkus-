import jakarta.ws.rs.*;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

@Path("/upload")
public class ImageResource {

    @Inject
    ImageService imageService;

    @POST
    @Path("/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        List<String> fileNames = Collections.singletonList(imageService.uploadFile(input));

        // Save image entities to the database
        imageService.saveImageEntities(fileNames);

        // Retrieve the saved entities
        List<ImageEntity> savedEntities = imageService.getImages();

        return Response.ok(savedEntities).build();
    }
//    @GET
//    @Path("/files")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getImages() {
//        List<ImageEntity> images = imageService.getImages();
//        return Response.ok(images).build();
//    }

}