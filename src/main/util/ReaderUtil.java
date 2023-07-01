package main.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class ReaderUtil {
    private ReaderUtil() {}

    /**
     * Reads a Properties File by parsing Keys and Values and composing a HashMap
     * @param filepath path to the .config file
     * @return Returns the corresponding Key - Value pairs
     * @throws IOException if Properties-File isn't found
     */
    public static HashMap<String,String> readProperties(String filepath) throws IOException {
        try(InputStream stream = ReaderUtil.class.getClassLoader().getResourceAsStream(filepath)) {

            Properties props = new Properties();
            props.load(stream);

            Enumeration<Object> keys = props.keys();

            HashMap<String, String> keyMap = new HashMap<>();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = props.getProperty(key);
                keyMap.put(key, value);
            }

            return keyMap;
        } catch (IOException e) {
            throw new IOException("Unable to read the provided properties file: '" + filepath + "'");
        }
    }
}
