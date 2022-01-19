package com.learningstuff.zipcreateandextract.Utils;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.learningstuff.zipcreateandextract.models.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Md. Shamim
 * Date: ১৯/১/২২
 * Time: ২:২১ PM
 * Email: mdshamim723@gmail.com
 */

public class Util {

    public static Path getTempJsonFileWithUsers(int count) throws IOException {

        // create temp file
        Path tempFile = Files.createTempFile(null, ".json");

        // write class as json
        List<User> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            users.add(new User("User --- " + i));
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(tempFile.toFile(), users);

        return tempFile;
    }

}
