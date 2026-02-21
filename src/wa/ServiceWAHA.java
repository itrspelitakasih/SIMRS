package wa;

import fungsi.PdfProtectorBox;
import fungsi.koneksiDBWa;
import fungsi.sekuel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServiceWAHA {

    private sekuel Sequel = new sekuel();
    private static final int CONNECT_TIMEOUT = 7000;
    private static final int READ_TIMEOUT = 15000;

    /* =====================================================
       ================= TEXT ONLY =========================
       ===================================================== */
    public boolean kirimTextOnly(String noHP, String pesan) {

        if (!isSessionReady()) {
            System.err.println("WAHA Session belum WORKING.");
            return false;
        }

        SendResult r = kirimTextWithStatus(noHP, pesan);

        if (r.ok) {
            System.out.println("[WAHA] TEXT SUCCESS HTTP " + r.httpCode);
            return true;
        } else {
            System.err.println("[WAHA] TEXT FAILED");
            System.err.println(r.error);
            System.err.println(r.responseBody);
            return false;
        }
    }

    /* =====================================================
       ================= TEXT + FILE =======================
       ===================================================== */
    public boolean kirimTextWithFile(
            String namaFileReport,
            String jenisDokumen,
            String pesan,
            String noHP,
            String idDokumen,
            String passwordPdf
    ) {

        if (!isSessionReady()) {
            System.err.println("WAHA Session belum WORKING.");
            return false;
        }

        try {

            File srcPdf = new File(
                    System.getProperty("user.dir")
                    + File.separator + "report"
                    + File.separator + namaFileReport
            );

            if (!srcPdf.exists()) {
                System.err.println("File report tidak ditemukan: " + namaFileReport);
                return false;
            }

            File folder = new File("tmpPDF");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());

            String safeId = (idDokumen == null || idDokumen.trim().isEmpty())
                    ? "DOC"
                    : idDokumen.replaceAll("[^0-9A-Za-z]", "_");

            String namaFile = jenisDokumen.replaceAll("\\s+", "_")
                    + "_" + safeId
                    + "_" + timestamp + "_secure.pdf";

            File tempCopy = new File(folder, "temp_" + timestamp + ".pdf");
            File securePdf = new File(folder, namaFile);

            // COPY
            Files.copy(srcPdf.toPath(),
                    tempCopy.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // PROTECT (optional)
            if (passwordPdf != null && !passwordPdf.trim().isEmpty()) {
                PdfProtectorBox.encrypt(
                        tempCopy,
                        securePdf,
                        passwordPdf.trim(),
                        passwordPdf.trim(),
                        128
                );
                tempCopy.delete();
            } else {
                securePdf = tempCopy; // tanpa protect
            }

            // UPLOAD
            String fileUrl = uploadPDFToServer(securePdf);

            if (fileUrl == null) {
                return false;
            }

            String finalMessage = pesan + "\n\n🔗 Download:\n" + fileUrl;

            return kirimTextOnly(noHP, finalMessage);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* =====================================================
       ================= CORE SEND =========================
       ===================================================== */
    private SendResult kirimTextWithStatus(String nomorWa, String pesan) {

        String baseUrl = koneksiDBWa.WAHA_BASE_URL();
        String apiKey = koneksiDBWa.WAHA_API_KEY();
        String session = koneksiDBWa.SESSION();

        String phone = normalizePhone(nomorWa);
        String pesanAman = escapeJson(pesan);

        String payloadA = "{\"receiver\":\"" + phone + "\","
                + "\"message\":\"" + pesanAman + "\"}";

        SendResult r1 = postJson(baseUrl + "/api/sendText", payloadA, apiKey);
        if (r1.ok) {
            return r1;
        }

        String payloadB = "{\"chatId\":\"" + phone + "@c.us\","
                + "\"text\":\"" + pesanAman + "\","
                + "\"session\":\"" + session + "\"}";

        return postJson(baseUrl + "/api/sendText", payloadB, apiKey);
    }

    /* =====================================================
       ================= HTTP CORE =========================
       ===================================================== */
    private SendResult postJson(String urlStr, String payload, String apiKey) {

        HttpURLConnection conn = null;

        try {

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-API-Key", apiKey);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            String body = readBody(conn);

            boolean ok = (code >= 200 && code < 300);
            String err = ok ? null : ("HTTP " + code + " | " + body);

            return new SendResult(ok, code, body, err);

        } catch (SocketTimeoutException e) {
            return new SendResult(false, 0, null, "Timeout");
        } catch (Exception e) {
            return new SendResult(false, 0, null, e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readBody(HttpURLConnection conn) {
        try (InputStream is
                = (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                ? conn.getInputStream()
                : conn.getErrorStream()) {

            if (is == null) {
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "";
        }
    }

    /* =====================================================
       ================= FILE UPLOAD =======================
       ===================================================== */
    private String uploadPDFToServer(File file) {

        try {

            String baseUrl = koneksiDBWa.FILE_BASE_URL();
            String uploadUrl = baseUrl + "/generatePDF/upload_lab.php";
            String token = koneksiDBWa.TOKEN();

            String boundary = "----SIMRS-" + System.currentTimeMillis();
            String CRLF = "\r\n";

            HttpURLConnection conn
                    = (HttpURLConnection) new URL(uploadUrl).openConnection();

            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = conn.getOutputStream(); PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(file.getName()).append("\"").append(CRLF);
                writer.append("Content-Type: application/pdf").append(CRLF);
                writer.append(CRLF).flush();

                Files.copy(file.toPath(), output);
                output.flush();

                writer.append(CRLF).flush();
                writer.append("--").append(boundary).append("--").append(CRLF).flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is == null) {
                return null;
            }

            String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            if (responseCode == 200 && response.contains("\"url\"")) {
                int idx = response.indexOf("\"url\"");
                int start = response.indexOf("\"", idx + 6) + 1;
                int end = response.indexOf("\"", start);
                return response.substring(start, end);
            }

            System.err.println("[UPLOAD] HTTP " + responseCode + " | " + response);
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* =====================================================
       ================= SESSION CHECK =====================
       ===================================================== */
    private boolean isSessionReady() {

        try {

            String baseUrl = koneksiDBWa.WAHA_BASE_URL();
            String apiKey = koneksiDBWa.WAHA_API_KEY();
            String session = koneksiDBWa.SESSION();

            URL url = new URL(baseUrl + "/api/sessions/" + session);
            HttpURLConnection conn
                    = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-API-Key", apiKey);

            int code = conn.getResponseCode();
            if (code != 200) {
                return false;
            }

            String response = new String(
                    conn.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return response.contains("\"status\":\"WORKING\"");

        } catch (Exception e) {
            return false;
        }
    }

    /* =====================================================
       ================= UTIL ===============================
       ===================================================== */
    private String normalizePhone(String raw) {
        if (raw == null) {
            return "";
        }
        String d = raw.replaceAll("[^0-9]", "");
        if (d.startsWith("0")) {
            d = "62" + d.substring(1);
        }
        if (!d.startsWith("62")) {
            d = "62" + d;
        }
        return d;
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    /* =====================================================
       ================= RESULT =============================
       ===================================================== */
    public static class SendResult {

        public boolean ok;
        public int httpCode;
        public String responseBody;
        public String error;

        public SendResult(boolean ok, int httpCode,
                String responseBody, String error) {
            this.ok = ok;
            this.httpCode = httpCode;
            this.responseBody = responseBody;
            this.error = error;
        }
    }

    public boolean kirimFileYangSudahAda(
            File existingFile,
            String jenisDokumen,
            String pesan,
            String noHP,
            String idDokumen,
            String passwordPdf
    ) {

        if (!isSessionReady()) {
            System.err.println("WAHA Session belum WORKING.");
            return false;
        }

        try {

            if (!existingFile.exists() || existingFile.length() == 0) {
                System.err.println("File tidak ditemukan atau kosong.");
                return false;
            }

            File folder = new File("tmpPDF");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new java.util.Date());

            String safeId = (idDokumen == null || idDokumen.trim().isEmpty())
                    ? "DOC"
                    : idDokumen.replaceAll("[^0-9A-Za-z]", "_");

            String namaFile = jenisDokumen.replaceAll("\\s+", "_")
                    + "_" + safeId
                    + "_" + timestamp + "_secure.pdf";

            File securePdf = new File(folder, namaFile);

            // PROTECT
            PdfProtectorBox.encrypt(
                    existingFile,
                    securePdf,
                    passwordPdf.trim(),
                    passwordPdf.trim(),
                    128
            );

            // UPLOAD
            String fileUrl = uploadPDFToServer(securePdf);

            if (fileUrl == null) {
                System.err.println("Upload gagal.");
                return false;
            }

            // Gabungkan pesan + link
            String finalMessage = pesan + "\n\n🔗 Download:\n" + fileUrl;

            return kirimTextOnly(noHP, finalMessage);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean kirimDokumenDariNoRawat(
            String namaFileReport,
            String jenisDokumen,
            String noRawat,
            String idDokumen,
            String pesanDariForm
    ) {

        if (!isSessionReady()) {
            System.err.println("WAHA Session belum WORKING.");
            return false;
        }

        try {

            File existingFile = new File("report/" + namaFileReport);

            if (!existingFile.exists() || existingFile.length() == 0) {
                System.err.println("File report tidak ditemukan atau kosong.");
                return false;
            }

            // ===== Ambil Data =====
            String noRM = Sequel.cariIsi(
                    "select no_rkm_medis from reg_periksa where no_rawat=?",
                    noRawat
            );

            if (noRM == null || noRM.trim().isEmpty()) {
                return false;
            }

            String noHP = Sequel.cariIsi(
                    "select no_tlp from pasien where no_rkm_medis=?",
                    noRM
            );

            if (noHP == null || noHP.trim().isEmpty()) {
                return false;
            }

            String tglLahirStr = Sequel.cariIsi(
                    "select tgl_lahir from pasien where no_rkm_medis=?",
                    noRM
            );

            String passwordPdf = generatePasswordFromBirthDate(tglLahirStr);

            // ===== Protect =====
            File folder = new File("tmpPDF");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new java.util.Date());

            String safeId = idDokumen.replaceAll("[^0-9A-Za-z]", "_");

            File securePdf = new File(folder,
                    jenisDokumen.replaceAll("\\s+", "_")
                    + "_" + safeId
                    + "_" + timestamp + "_secure.pdf");

            PdfProtectorBox.encrypt(
                    existingFile,
                    securePdf,
                    passwordPdf,
                    passwordPdf,
                    128
            );

            // ===== Upload =====
            String fileUrl = uploadPDFToServer(securePdf);
            if (fileUrl == null) {
                return false;
            }

            // ===== Kirim persis pesan dari form + link =====
            String finalMessage = pesanDariForm + "\n\n" + fileUrl;

            return kirimTextOnly(noHP, finalMessage);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generatePasswordFromBirthDate(String tglLahir) {

        if (tglLahir == null || tglLahir.trim().isEmpty()) {
            return "01011990";
        }

        try {
            java.util.Date date
                    = new java.text.SimpleDateFormat("yyyy-MM-dd")
                            .parse(tglLahir.trim());

            return new java.text.SimpleDateFormat("ddMMyyyy")
                    .format(date);

        } catch (Exception e) {
            return "01011990";
        }
    }

    public boolean kirimDokumenDariNoRM(
            String namaFileReport,
            String jenisDokumen,
            String noRM,
            String idDokumen,
            String pesanDariForm
    ) {

        if (!isSessionReady()) {
            System.err.println("WAHA Session belum WORKING.");
            return false;
        }

        try {

            File existingFile = new File("report/" + namaFileReport);

            if (!existingFile.exists() || existingFile.length() == 0) {
                System.err.println("File report tidak ditemukan atau kosong.");
                return false;
            }

            /* ================= AMBIL DATA PASIEN ================= */
            String namaPasien = Sequel.cariIsi(
                    "select nm_pasien from pasien where no_rkm_medis=?",
                    noRM
            );

            if (namaPasien == null || namaPasien.trim().isEmpty()) {
                System.err.println("Nama pasien tidak ditemukan.");
                return false;
            }

            String noHP = Sequel.cariIsi(
                    "select no_tlp from pasien where no_rkm_medis=?",
                    noRM
            );

            if (noHP == null || noHP.trim().isEmpty()) {
                System.err.println("Nomor HP pasien tidak tersedia.");
                return false;
            }

            /* ================= PASSWORD ================= */
            String tglLahirStr = Sequel.cariIsi(
                    "select tgl_lahir from pasien where no_rkm_medis=?",
                    noRM
            );

            String passwordPdf = generatePasswordFromBirthDate(tglLahirStr);

            /* ================= PROTECT ================= */
            File folder = new File("tmpPDF");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new java.util.Date());

            String safeId = idDokumen.replaceAll("[^0-9A-Za-z]", "_");

            File securePdf = new File(folder,
                    jenisDokumen.replaceAll("\\s+", "_")
                    + "_" + safeId
                    + "_" + timestamp + "_secure.pdf");

            PdfProtectorBox.encrypt(
                    existingFile,
                    securePdf,
                    passwordPdf,
                    passwordPdf,
                    128
            );

            /* ================= UPLOAD ================= */
            String fileUrl = uploadPDFToServer(securePdf);
            if (fileUrl == null) {
                return false;
            }

            /* ================= KIRIM ================= */
            String finalMessage = pesanDariForm + "\n\n" + fileUrl;

            return kirimTextOnly(noHP, finalMessage);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
