package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.imageio.ImageIO;

@Service
public class S3Service {

   private static final String IMAGE_BASE_URL = "https://images.nattypro.life"; //single source of truth for the new domain
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.secret.key}")
    private String secretKey;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

 private S3Client getClient() {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .build();
    }

    // Enforces a size cap, then attempts a real image decode (not just a declared
    // Content-Type header) to confirm the bytes are actually a valid image before
    // anything reaches S3. Returns the bytes so callers don't have to read twice.
    private byte[] validateAndReadImage(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the maximum allowed size of 10MB.");
        }
        byte[] bytes = file.getBytes();
        if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
            throw new IllegalArgumentException("File is not a valid image.");
        }
        return bytes;
    }

public String uploadFile(MultipartFile file) throws IOException {
        byte[] imageBytes = validateAndReadImage(file);

        String originalFilename = file.getOriginalFilename();
        String safeFilename = originalFilename != null
            ? originalFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_")
            : "file";
        String key = "posts/" + UUID.randomUUID() + "_" + safeFilename;

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.getContentType())
            .build();

        getClient().putObject(request, RequestBody.fromBytes(imageBytes));

     return IMAGE_BASE_URL + "/" + key;  //swaps old hardcoded s3 hostname for the new constant and cleaned up stray closing brace
    }
// instead of searching for a literal sting this pareses the URL properly and pull out just its path, the strips the leasing slash to get to s3 key
public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        String key;
        try {
            key = new URI(fileUrl).getPath();
        } catch (URISyntaxException e) {
            return;
        }
        if (key == null || key.isEmpty()) return;
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        getClient().deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build());
    }
    
 public String uploadHeroImage(MultipartFile file) throws IOException {
        byte[] imageBytes = validateAndReadImage(file);

      String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).replaceAll("[^a-zA-Z0-9.\\-]", "_");
        }
        String key = "hero/" + UUID.randomUUID() + ext;
       getClient().putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build(),
            RequestBody.fromBytes(imageBytes)
        );
        return IMAGE_BASE_URL + "/" + key;
    }
}