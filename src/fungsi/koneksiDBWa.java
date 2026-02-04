/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fungsi;

import java.io.FileInputStream;
import java.util.Properties;

public class koneksiDBWa {

    private static Properties prop = new Properties();
    private static boolean loaded = false;

    private static void load() {
        if (loaded) {
            return;
        }
        try {
            FileInputStream fis = new FileInputStream("setting/database.xml");
            prop.loadFromXML(fis);
            fis.close();
            loaded = true;
        } catch (Exception e) {
            System.err.println("[WAHA] Gagal load database.xml");
            e.printStackTrace();
        }
    }

    public static String WAHA_BASE_URL() {
        load();
        return prop.getProperty("WAHA_BASE_URL", "").trim();
    }

    public static String SESSION() {
        load();
        return prop.getProperty("WAHA_SESSION", "").trim();
    }

    public static String getAPIKey() {
        load();
        return prop.getProperty("WAHA_API_KEY", "").trim();
    }
    public static String TOKEN() {
        load();
        return prop.getProperty("TOKEN", "").trim();
    }

    public static String FILE_BASE_URL() {
        load();
        return prop.getProperty("WAHA_FILE_BASE_URL", "").trim();
    }

    public static String WAHA_API_KEY() {
        load();
        return prop.getProperty("WAHA_API_KEY", "").trim();
    }
    
}
