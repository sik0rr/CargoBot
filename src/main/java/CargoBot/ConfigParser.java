package CargoBot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigParser {
    private String filePath;
    public ConfigParser(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, String> getConfig() {
        ObjectMapper mapper = new ObjectMapper();
        Path jsonFile = Paths.get(filePath);
        Map<String, String> config = new HashMap<>();
        try {
            config.putAll(mapper.readValue(jsonFile.toFile(), new TypeReference<>(){}));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(config);
        return config;
    }
}
