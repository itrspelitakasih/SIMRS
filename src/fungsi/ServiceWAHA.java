package fungsi;

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

    private static final int CONNECT_TIMEOUT = 7000;
    private static final int READ_TIMEOUT = 15000;

    /* ================= MASTER FUNCTION ================= */
    public boolean kirimLab(
            String namaPasien,
            String noHP,
            String noRawat,
            String passwordPdf,
            String namaRS
    ) {
        /* ================= CEK SESSION ================= */
        if (!isSessionReady()) {
            System.err.println("WAHA Session belum WORKING.");
            return false;
        }
        try {

            File srcPdf = new File("report", "rptPeriksaLab2.pdf");
            if (!srcPdf.exists()) {
                System.err.println("PDF tidak ditemukan");
                return false;
            }

            File folder = new File("tmpPDF/lab");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());

            String namaFile = "Lab_"
                    + noRawat.replaceAll("[^0-9A-Za-z]", "_")
                    + "_" + timestamp + "_secure.pdf";

            File securePdf = new File(folder, namaFile);
            File tempCopy = new File(folder, "temp_" + timestamp + ".pdf");

            /* COPY */
            Files.copy(srcPdf.toPath(),
                    tempCopy.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            /* PROTECT */
            PdfProtectorBox.encrypt(
                    tempCopy,
                    securePdf,
                    passwordPdf,
                    "SIMRS_INTERNAL",
                    128
            );

            tempCopy.delete();

            /* UPLOAD */
            String fileUrl = uploadPDFToServer(securePdf);
            if (fileUrl == null) {
                return false;
            }

            String pesan = buatPesanLab(namaPasien, namaRS, fileUrl);

            return kirimText(noHP, pesan);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ================= PESAN ================= */
    private String buatPesanLab(
            String namaPasien,
            String namaRS,
            String fileUrl
    ) {

        return "Halo *" + namaPasien + "* 👋\n\n"
                + "Berikut hasil Pemeriksaan Laboratorium Anda dari *"
                + namaRS + "*.\n\n"
                + "🔗 Link Download : " + fileUrl + "\n\n"
                + "🔐 Password PDF : tanggal lahir (ddMMyyyy)\n\n"
                + "Terima kasih.";
    }

    /* ================= KIRIM TEXT (ENGINE STABIL) ================= */
    public boolean kirimText(String nomorWa, String pesan) {

        SendResult r = kirimTextWithStatus(nomorWa, pesan);

        if (r.ok) {
            System.out.println("[WAHA] HTTP " + r.httpCode);
            return true;
        } else {
            System.err.println("[WAHA] Gagal kirim");
            System.err.println(r.error);
            System.err.println(r.responseBody);
            return false;
        }
    }

    private SendResult kirimTextWithStatus(String nomorWa, String pesan) {

        String baseUrl = koneksiDBWa.WAHA_BASE_URL();
        String apiKey = koneksiDBWa.WAHA_API_KEY();
        String session = koneksiDBWa.SESSION();

        String phone = normalizePhone(nomorWa);
        String pesanAman = escapeJson(pesan);

        String payloadA
                = "{\"receiver\":\"" + phone + "\","
                + "\"message\":\"" + pesanAman + "\"}";

        SendResult r1 = postJson(baseUrl + "/api/sendText", payloadA, apiKey);
        if (r1.ok) {
            return r1;
        }

        String payloadB
                = "{\"chatId\":\"" + phone + "@c.us\","
                + "\"text\":\"" + pesanAman + "\","
                + "\"session\":\"" + session + "\"}";

        return postJson(baseUrl + "/api/sendText", payloadB, apiKey);
    }

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

            if (is == null) return "";

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "";
        }
    }

    /* ================= UPLOAD (ASLI DARI SCRIPTMU) ================= */
    private String uploadPDFToServer(File file) {

        try {

            String baseUrl = koneksiDBWa.FILE_BASE_URL();
            String uploadUrl = baseUrl + "/HasilLab/upload_lab.php";
            String token = koneksiDBWa.TOKEN();

            String boundary = "----SIMRS-" + System.currentTimeMillis();
            String CRLF = "\r\n";

            HttpURLConnection conn =
                    (HttpURLConnection) new URL(uploadUrl).openConnection();

            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(
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

            if (is == null) return null;

            String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            if (responseCode == 200) {
                int idx = response.indexOf("\"url\"");
                if (idx > -1) {
                    int start = response.indexOf("\"", idx + 6) + 1;
                    int end = response.indexOf("\"", start);
                    return response.substring(start, end);
                }
            }

            System.err.println("[UPLOAD] HTTP " + responseCode + " | " + response);
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* ================= UTIL ================= */
    private String normalizePhone(String raw) {
        String d = raw.replaceAll("[^0-9]", "");
        if (d.startsWith("0")) d = "62" + d.substring(1);
        if (!d.startsWith("62")) d = "62" + d;
        return d;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    private static class SendResult {
        boolean ok;
        int httpCode;
        String responseBody;
        String error;

        SendResult(boolean ok, int httpCode, String responseBody, String error) {
            this.ok = ok;
            this.httpCode = httpCode;
            this.responseBody = responseBody;
            this.error = error;
        }
    }
    private boolean isSessionReady() {

    try {

        String baseUrl = koneksiDBWa.WAHA_BASE_URL();
        String apiKey = koneksiDBWa.WAHA_API_KEY();
        String session = koneksiDBWa.SESSION();

        URL url = new URL(baseUrl + "/api/sessions/" + session);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-API-Key", apiKey);

        int code = conn.getResponseCode();

        if (code != 200) return false;

        String response = new String(
                conn.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return response.contains("\"status\":\"WORKING\"");

    } catch (Exception e) {
        return false;
    }
}

}
