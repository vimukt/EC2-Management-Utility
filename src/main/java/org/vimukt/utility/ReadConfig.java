package org.vimukt.utility;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ReadConfig {

	/**
     * Return a value from conf/crendentials given a property
     * @param property an existing property from config/credentials
     * @return the value of a property
     */
    public static String getValue(String property) {
        Properties properties;
        String value = null;
        try {
            properties = new Properties();
            properties.load(new FileReader(new File("conf/credentials")));

            value =  properties.getProperty(property);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

}
