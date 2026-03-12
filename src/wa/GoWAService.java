//package wa;
//
//import fungsi.PdfProtectorBox;
//import fungsi.koneksiDBWa;
//import fungsi.sekuel;
//
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.Base64;
//import javax.swing.JOptionPane;
//
//public class GoWAService {
//
//    private static final sekuel Sequel = new sekuel();
//    private static final int CONNECT_TIMEOUT = 7000;
//    private static final int READ_TIMEOUT = 20000;
//
//    // =====================================================
//    // ================== CORE ENGINE ======================
//    // =====================================================
//    private static boolean kirimCore(
//            String namaFileReport,
//            String jenisDokumen,
//            String noRM,
//            String noHPForm,
//            boolean usePassword,
//            String pesan
//    ) {
//
//        try {
//
//            if (!isReady(noHPForm)) {
//                return false;
//            }
//
//            File original = getReportFile(namaFileReport);
//            if (original == null) {
//                return false;
//            }
//
//            File fileToSend = original;
//
//            if (usePassword) {
//                String tglLahir = Sequel.cariIsi(
//                        "select tgl_lahir from pasien where no_rkm_medis=?",
//                        noRM
//                );
//                String password = generatePasswordFromBirthDate(tglLahir);
//                fileToSend = protectPdf(original, password);
//            }
//
//            String displayName = buildDisplayFileName(
//                    jenisDokumen,
//                    noRM,
//                    usePassword
//            );
//
//            boolean sukses = sendMultipart(
//                    fileToSend,
//                    noHPForm,
//                    pesan,
//                    displayName
//            );
//
//            if (sukses) {
//                cleanupTmp();
//            }
//
//            return sukses;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // =====================================================
//    // ================= PUBLIC METHODS ====================
//    // =====================================================
//    public static boolean kirimDariNoRM(
//            String namaFileReport,
//            String jenisDokumen,
//            String noRM,
//            String noHPForm,
//            boolean usePassword,
//            String pesan
//    ) {
//        return kirimCore(
//                namaFileReport,
//                jenisDokumen,
//                noRM,
//                noHPForm,
//                usePassword,
//                pesan
//        );
//    }
//
//    public static boolean kirimDariNoRawat(
//            String namaFileReport,
//            String jenisDokumen,
//            String noRawat,
//            String noHPForm,
//            boolean usePassword,
//            String pesan
//    ) {
//
//        String noRM = Sequel.cariIsi(
//                "select no_rkm_medis from reg_periksa where no_rawat=?",
//                noRawat
//        );
//
//        if (noRM == null || noRM.trim().isEmpty()) {
//            System.out.println("No RM tidak ditemukan");
//            return false;
//        }
//
//        return kirimCore(
//                namaFileReport,
//                jenisDokumen,
//                noRM,
//                noHPForm,
//                usePassword,
//                pesan
//        );
//
//    }
//
//    // =====================================================
//    // ================= VALIDATION ========================
//    // =====================================================
//    private static boolean isReady(String noHP) {
//
//        if (koneksiDBWa.GOWA_BASE_URL().isEmpty()
//                || koneksiDBWa.GOWA_USERNAME().isEmpty()
//                || koneksiDBWa.GOWA_PASSWORD().isEmpty()) {
//            System.out.println("CONFIG GOWA BELUM LENGKAP");
//            return false;
//        }
//
//        if (!isSessionReady()) {
//            System.out.println("SESSION GOWA BELUM CONNECTED");
//            return false;
//        }
//
//        if (noHP == null || noHP.trim().isEmpty()) {
//            System.out.println("Nomor HP kosong");
//            return false;
//        }
//
//        return true;
//    }
//
//    private static boolean isSessionReady() {
//
//        try {
//
//            //URL url = new URL(koneksiDBWa.GOWA_BASE_URL() + "/app/status");
//            URL url = new URL(koneksiDBWa.GOWA_BASE_URL() + "/app/status?device_id=" + deviceId());
//            System.out.println("NAMA DEVICE : " + deviceId());
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            conn.setConnectTimeout(CONNECT_TIMEOUT);
//            conn.setReadTimeout(READ_TIMEOUT);
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Authorization", basicAuth());
//
//            if (conn.getResponseCode() != 200) {
//                return false;
//            }
//
//            String response = new String(
//                    conn.getInputStream().readAllBytes(),
//                    StandardCharsets.UTF_8
//            );
//
//            return response.contains("\"is_connected\":true")
//                    && response.contains("\"is_logged_in\":true");
//
//        } catch (Exception e) {
//            return false;
//        }
//
//    }
//
//    // =====================================================
//    // ================= FILE & PDF ========================
//    // =====================================================
//    private static File getReportFile(String namaFile) {
//
//        File file = new File("report/" + namaFile);
//
//        if (!file.exists() || file.length() == 0) {
//            System.out.println("FILE TIDAK DITEMUKAN");
//            return null;
//        }
//
//        return file;
//    }
//
//    private static File protectPdf(File source, String password) {
//
//        if (password == null || password.isEmpty()) {
//            return source;
//        }
//
//        try {
//
//            File folder = new File("tmpPDF");
//            if (!folder.exists()) {
//                folder.mkdirs();
//            }
//
//            File output = new File(folder,
//                    "protected_" + source.getName());
//
//            PdfProtectorBox.encrypt(
//                    source,
//                    output,
//                    password,
//                    password,
//                    128
//            );
//
//            return output;
//
//        } catch (Exception e) {
//            return source;
//        }
//    }
//
//    private static void cleanupTmp() {
//
//        File folder = new File("tmpPDF");
//        if (!folder.exists()) {
//            return;
//        }
//
//        File[] files = folder.listFiles();
//        if (files == null) {
//            return;
//        }
//
//        for (File f : files) {
//            if (f.getName().startsWith("protected_")) {
//                f.delete();
//            }
//        }
//    }
//
//    // =====================================================
//    // ================= HTTP SENDER =======================
//    // =====================================================
//    private static boolean sendMultipart(
//            File file,
//            String noHP,
//            String caption,
//            String displayFileName
//    ) throws Exception {
//
//        String boundary = "----SIMRS-" + System.currentTimeMillis();
//        String phone = normalizePhone(noHP) + "@s.whatsapp.net";
//
//        URL url = new URL(
//                koneksiDBWa.GOWA_BASE_URL()
//                + "/send/file?device_id=" + deviceId()
//        );
//
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        conn.setConnectTimeout(CONNECT_TIMEOUT);
//        conn.setReadTimeout(READ_TIMEOUT);
//        conn.setRequestMethod("POST");
//        conn.setDoOutput(true);
//
//        conn.setRequestProperty("Authorization", basicAuth());
//        conn.setRequestProperty("Content-Type",
//                "multipart/form-data; boundary=" + boundary);
//
//        try (OutputStream output = conn.getOutputStream(); PrintWriter writer = new PrintWriter(
//                new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
//
//            // PHONE
//            writer.append("--").append(boundary).append("\r\n");
//            writer.append("Content-Disposition: form-data; name=\"phone\"\r\n\r\n");
//            writer.append(phone).append("\r\n");
//
//            // CAPTION
//            writer.append("--").append(boundary).append("\r\n");
//            writer.append("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
//            writer.append(caption).append("\r\n");
//
//            // FILE
//            writer.append("--").append(boundary).append("\r\n");
//            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
//                    .append(displayFileName).append("\"\r\n");
//            writer.append("Content-Type: application/pdf\r\n\r\n");
//            writer.flush();
//
//            Files.copy(file.toPath(), output);
//            output.flush();
//
//            writer.append("\r\n--").append(boundary).append("--\r\n");
//            writer.flush();
//        }
//
//        int code = conn.getResponseCode();
//
//        if (code >= 200 && code < 300) {
//
//            JOptionPane.showMessageDialog(
//                    null,
//                    "Pesan terkirim ke nomor : " + normalizePhone(noHP),
//                    "Informasi",
//                    JOptionPane.INFORMATION_MESSAGE
//            );
//
//        } else {
//
//            JOptionPane.showMessageDialog(
//                    null,
//                    "Gagal mengirim pesan ke nomor : " + normalizePhone(noHP),
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE
//            );
//        }
//
//        return code >= 200 && code < 300;
//    }
//
//    // =====================================================
//    // ================= UTIL ==============================
//    // =====================================================
//    private static String deviceId() {
//        try {
//            String id = koneksiDBWa.GOWA_DEVICE_ID();
//            if (id == null || id.trim().isEmpty()) {
//                return "default";
//            }
//            return id.trim();
//        } catch (Exception e) {
//            return "default";
//        }
//    }
//
//    private static String basicAuth() {
//        String auth = koneksiDBWa.GOWA_USERNAME() + ":"
//                + koneksiDBWa.GOWA_PASSWORD();
//
//        return "Basic " + Base64.getEncoder()
//                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
//    }
//
//    private static String normalizePhone(String raw) {
//
//        String d = raw.replaceAll("[^0-9]", "");
//
//        if (d.startsWith("0")) {
//            d = "62" + d.substring(1);
//        }
//        if (!d.startsWith("62")) {
//            d = "62" + d;
//        }
//
//        return d;
//    }
//
//    private static String generatePasswordFromBirthDate(String tgl) {
//
//        if (tgl == null || tgl.length() < 10) {
//            return null;
//        }
//
//        try {
//            LocalDate date = LocalDate.parse(tgl.substring(0, 10));
//            return date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private static String buildDisplayFileName(
//            String jenisDokumen,
//            String noRM,
//            boolean protectedFile
//    ) {
//
//        String tanggal = LocalDate.now()
//                .format(DateTimeFormatter.ofPattern("ddMMyyyy"));
//
//        String safeJenis = jenisDokumen.replaceAll("[^a-zA-Z0-9]", "_");
//
//        return tanggal + "_" + safeJenis + "_" + noRM
//                + (protectedFile ? "_protect" : "")
//                + ".pdf";
//    }
//
////    KIRIM GAMBAR RADIOLOGI
//    public static boolean kirimGambar(String noHP, File file, String caption) {
//
//        try {
//
//            String boundary = "----SIMRS-" + System.currentTimeMillis();
//            String phone = normalizePhone(noHP) + "@s.whatsapp.net";
//
//            URL url = new URL(
//                    koneksiDBWa.GOWA_BASE_URL()
//                    + "/send/file?device_id=" + deviceId()
//            );
//
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            conn.setConnectTimeout(CONNECT_TIMEOUT);
//            conn.setReadTimeout(READ_TIMEOUT);
//            conn.setRequestMethod("POST");
//            conn.setDoOutput(true);
//
//            conn.setRequestProperty("Authorization", basicAuth());
//            conn.setRequestProperty("Content-Type",
//                    "multipart/form-data; boundary=" + boundary);
//
//            try (OutputStream output = conn.getOutputStream(); PrintWriter writer = new PrintWriter(
//                    new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
//
//                writer.append("--").append(boundary).append("\r\n");
//                writer.append("Content-Disposition: form-data; name=\"phone\"\r\n\r\n");
//                writer.append(phone).append("\r\n");
//
//                writer.append("--").append(boundary).append("\r\n");
//                writer.append("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
//                writer.append(caption).append("\r\n");
//
//                writer.append("--").append(boundary).append("\r\n");
//                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
//                        .append(file.getName()).append("\"\r\n");
//                writer.append("Content-Type: application/octet-stream\r\n\r\n");
//                writer.flush();
//
//                Files.copy(file.toPath(), output);
//                output.flush();
//
//                writer.append("\r\n--").append(boundary).append("--\r\n");
//                writer.flush();
//            }
//
//            int code = conn.getResponseCode();
//
//            System.out.println("WA RESPONSE CODE : " + code);
//
//            return code >= 200 && code < 300;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
package wa;

import fungsi.koneksiDBWa;
import fungsi.sekuel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class GoWAService {

    private static final sekuel Sequel = new sekuel();

    private static final int CONNECT_TIMEOUT = 7000;
    private static final int READ_TIMEOUT = 20000;

    // =====================================================
    // ENGINE KIRIM FILE (PDF / JPG / PNG / DLL)
    // =====================================================
    private static boolean kirimFile(File file, String noHP, String caption) {

        try {

            if (!isReady(noHP)) {
                return false;
            }

            String boundary = "----SIMRS-" + System.currentTimeMillis();
            String phone = normalizePhone(noHP) + "@s.whatsapp.net";

            URL url = new URL(
                    koneksiDBWa.GOWA_BASE_URL()
                    + "/send/file?device_id=" + deviceId()
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", basicAuth());
            conn.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=" + boundary
            );

            try (
                    OutputStream output = conn.getOutputStream(); PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(output, StandardCharsets.UTF_8),
                    true
            )) {

                // PHONE
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"phone\"\r\n\r\n");
                writer.append(phone).append("\r\n");

                // CAPTION
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
                writer.append(caption).append("\r\n");

                // FILE
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: application/octet-stream\r\n\r\n");
                writer.flush();

                Files.copy(file.toPath(), output);
                output.flush();

                writer.append("\r\n--").append(boundary).append("--\r\n");
                writer.flush();
            }

            int code = conn.getResponseCode();

            System.out.println("WA RESPONSE CODE : " + code);

            return code >= 200 && code < 300;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

    // =====================================================
    // PUBLIC METHODS BARU
    // =====================================================
    public static boolean kirimGambar(String noHP, File file, String caption) {
        return kirimFile(file, noHP, caption);
    }

    public static boolean kirimPDF(String noHP, File file, String caption) {
        return kirimFile(file, noHP, caption);
    }

    // =====================================================
    // KOMPATIBILITAS DENGAN KODE LAMA
    // =====================================================
    public static boolean kirimDariNoRM(
            String namaFileReport,
            String jenisDokumen,
            String noRM,
            String noHPForm,
            boolean usePassword,
            String pesan
    ) {

        File file = new File("report/" + namaFileReport);

        if (!file.exists()) {
            System.out.println("FILE REPORT TIDAK DITEMUKAN");
            return false;
        }

        return kirimFile(file, noHPForm, pesan);
    }

    public static boolean kirimDariNoRawat(
            String namaFileReport,
            String jenisDokumen,
            String noRawat,
            String noHPForm,
            boolean usePassword,
            String pesan
    ) {

        String noRM = Sequel.cariIsi(
                "select no_rkm_medis from reg_periksa where no_rawat=?",
                noRawat
        );

        if (noRM == null || noRM.trim().isEmpty()) {

            System.out.println("No RM tidak ditemukan");
            return false;

        }

        File file = new File("report/" + namaFileReport);

        if (!file.exists()) {

            System.out.println("FILE REPORT TIDAK DITEMUKAN");
            return false;

        }

        return kirimFile(file, noHPForm, pesan);
    }

    // =====================================================
    // VALIDATION
    // =====================================================
    private static boolean isReady(String noHP) {

        if (koneksiDBWa.GOWA_BASE_URL().isEmpty()
                || koneksiDBWa.GOWA_USERNAME().isEmpty()
                || koneksiDBWa.GOWA_PASSWORD().isEmpty()) {

            System.out.println("CONFIG GOWA BELUM LENGKAP");
            return false;
        }

        if (!isSessionReady()) {

            System.out.println("SESSION GOWA BELUM CONNECTED");
            return false;
        }

        if (noHP == null || noHP.trim().isEmpty()) {

            System.out.println("Nomor HP kosong");
            return false;
        }

        return true;
    }

    private static boolean isSessionReady() {

        try {

            URL url = new URL(
                    koneksiDBWa.GOWA_BASE_URL()
                    + "/app/status?device_id=" + deviceId()
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", basicAuth());

            if (conn.getResponseCode() != 200) {
                return false;
            }

            String response = new String(
                    conn.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return response.contains("\"is_connected\":true")
                    && response.contains("\"is_logged_in\":true");

        } catch (Exception e) {

            return false;

        }
    }

    // =====================================================
    // UTIL
    // =====================================================
    private static String deviceId() {

        try {

            String id = koneksiDBWa.GOWA_DEVICE_ID();

            if (id == null || id.trim().isEmpty()) {
                return "default";
            }

            return id.trim();

        } catch (Exception e) {

            return "default";

        }
    }

    private static String basicAuth() {

        String auth
                = koneksiDBWa.GOWA_USERNAME()
                + ":"
                + koneksiDBWa.GOWA_PASSWORD();

        return "Basic "
                + Base64.getEncoder()
                        .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private static String normalizePhone(String raw) {

        String d = raw.replaceAll("[^0-9]", "");

        if (d.startsWith("0")) {
            d = "62" + d.substring(1);
        }

        if (!d.startsWith("62")) {
            d = "62" + d;
        }

        return d;
    }
}
