package com.learningstuff.zipcreateandextract.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.learningstuff.zipcreateandextract.models.User;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Md. Shamim
 * Date: ১৮/১/২২
 * Time: ১১:৩২ AM
 * Email: mdshamim723@gmail.com
 */

@RestController
@AllArgsConstructor
@RequestMapping(value = "")
public class UserController {

    @GetMapping(value = "/zip-1")
    public void zipFiles(HttpServletResponse response) throws IOException {

        //setting headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"test.zip\"");

        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

        // create a list to add files to be zipped
        ArrayList<File> files = new ArrayList<>(2);
        files.add(new File("README.md"));

        // package files
        for (File file : files) {
            //new zip entry and copying inputStream with file to zipOutputStream, after all closing streams
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);

            IOUtils.copy(fileInputStream, zipOutputStream);

            fileInputStream.close();
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
    }

    @GetMapping(value = "/zip-2")
    public ResponseEntity<StreamingResponseBody> zipJson() {

        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"test.zip\"").body(out -> {

            ZipOutputStream zipOutputStream = new ZipOutputStream(out);

            // create a list to add files to be zipped
            ArrayList<File> files = new ArrayList<>(2);

            files.add(new File("README.md"));
            files.add(new File("HELP.md"));

            // temp file
            Path tempFile = Files.createTempFile(null, ".json");

            // write to temp file
            Files.write(tempFile, "{\"name\": \"Shamim\"}\n".getBytes(StandardCharsets.UTF_8));
            List<String> content = Arrays.asList("{\"name\": \"Roni\"}", "{\"name\": \"Joniyed\"}", "{\"name\": \"Nahid\"}");
            Files.write(tempFile, content, StandardOpenOption.APPEND);

            files.add(tempFile.toFile());

            // package files
            for (File file : files) {
                //new zip entry and copying inputStream with file to zipOutputStream, after all closing streams
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream fileInputStream = new FileInputStream(file);

                IOUtils.copy(fileInputStream, zipOutputStream);

                fileInputStream.close();
                zipOutputStream.closeEntry();
            }

            zipOutputStream.close();
            Files.deleteIfExists(tempFile);
        });
    }

    @GetMapping(value = "/zip-3")
    public ResponseEntity<StreamingResponseBody> zipJson1() {

        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"test.zip\"").body(out -> {

            var zipOutputStream = new ZipOutputStream(out);

            // create a list to add files to be zipped
            ArrayList<File> files = new ArrayList<>(2);

            // temp file
            Path tempFile = Files.createTempFile(null, ".json");

            // write class
            List<User> users = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                users.add(new User("User --- " + i));
            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(tempFile.toFile(), users);

            files.add(tempFile.toFile());

            // package files
            for (File file : files) {
                //new zip entry and copying inputStream with file to zipOutputStream, after all closing streams
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream fileInputStream = new FileInputStream(file);

                IOUtils.copy(fileInputStream, zipOutputStream);

                fileInputStream.close();
                zipOutputStream.closeEntry();
            }

            zipOutputStream.close();
            Files.deleteIfExists(tempFile);
        });
    }

    @PostMapping(value = "/read-json")
    public ResponseEntity<?> readJson(@RequestPart(name = "files") List<MultipartFile> files) {

        ObjectMapper mapper = new ObjectMapper();
        List<User> users = new ArrayList<>();

        for (MultipartFile file : files) {
            try {

                InputStream inputStream = file.getInputStream();
                TypeReference<List<User>> typeReference = new TypeReference<>() {
                };
                List<User> _users = mapper.readValue(inputStream, typeReference);
                users.addAll(_users);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok().body(users);
    }

    @PostMapping(value = "/un-zip")
    public ResponseEntity<?> upZip(@RequestPart(name = "files") List<MultipartFile> files) {

        Path path = Paths.get("static");

        for (MultipartFile file : files) {

            try {
                ZipInputStream inputStream = new ZipInputStream(file.getInputStream());

                for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null; ) {
                    Path resolvedPath = path.resolve(entry.getName());
                    if (!entry.isDirectory()) {
                        Files.createDirectories(resolvedPath.getParent());
                        Files.copy(inputStream, resolvedPath);
                    } else {
                        Files.createDirectories(resolvedPath);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return ResponseEntity.ok().body("Successfully unzip.");
    }


    @PostMapping(value = "/read-zip")
    public ResponseEntity<?> readZip(@RequestPart(name = "files") List<MultipartFile> files) {

        ObjectMapper mapper = new ObjectMapper();
        List<User> users = new ArrayList<>();

        try {

            for (MultipartFile file : files) {

                // temp file
                Path tempFile = Files.createTempFile(null, null);
                file.transferTo(tempFile.toFile());

                ZipFile zipFile = new ZipFile(tempFile.toFile());

                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                while (enumeration.hasMoreElements()) {

                    ZipEntry zipEntry = enumeration.nextElement();

                    InputStream inputStream = zipFile.getInputStream(zipEntry);

                    TypeReference<List<User>> typeReference = new TypeReference<>() {
                    };
                    List<User> _users = mapper.readValue(inputStream, typeReference);
                    users.addAll(_users);

                }

                Files.deleteIfExists(tempFile);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().body(users);
    }

}
