/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fungsi;

import AESsecurity.EnkripsiAES;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author khanzasoft
 */
public class koneksiDBWa {

    private static Connection connection = null;
    private static final Properties prop = new Properties();
    private static final MysqlDataSource dataSource = new MysqlDataSource();
    private static String var = "";

    public koneksiDBWa() {
    }

    public static Connection condb() {
        if (connection == null) {
            try {
                prop.loadFromXML(new FileInputStream("setting/database-extra.xml"));
                dataSource.setURL("jdbc:mysql://" + EnkripsiAES.decrypt(prop.getProperty("HOSTWA")) + ":" + EnkripsiAES.decrypt(prop.getProperty("PORTWA")) + "/" + EnkripsiAES.decrypt(prop.getProperty("DATABASEWA")) + "?zeroDateTimeBehavior=convertToNull&amp;autoReconnect=true");
                dataSource.setUser(EnkripsiAES.decrypt(prop.getProperty("USERWA")));
                dataSource.setPassword(EnkripsiAES.decrypt(prop.getProperty("PASSWA")));
                connection = dataSource.getConnection();
                System.out.println("  Koneksi Berhasil. Menyambungkan ke database bridging Gateway WA...!!!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Koneksi ke server bridging Gateway WA terputus : " + e);
            }
        }
        return connection;
    }
    
    public static String IDGRUPWA() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database-extra.xml"));
            var = prop.getProperty("IDGRUPWA");
        } catch (Exception e) {
            var = "";
        }
        return var;
    }
    public static String NOTIFWA() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database-extra.xml"));
            var = prop.getProperty("NOTIFWA");
        } catch (Exception e) {
            var = "";
        }
        return var;
    }
    public static String NOTIFWABOOKING() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database-extra.xml"));
            var = prop.getProperty("NOTIFWAREGISTRASI");
        } catch (Exception e) {
            var = "";
        }
        return var;
    }
    public static String NOTIFWAREGISTRASI() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database-extra.xml"));
            var = prop.getProperty("NOTIFWAREGISTRASI");
        } catch (Exception e) {
            var = "";
        }
        return var;
    }
}
