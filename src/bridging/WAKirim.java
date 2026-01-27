package bridging;

import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * WAKirim.java - WA Gateway Fawwaz (http://172.22.10.165:3000)
 * Kompatibel dengan endpoint:
 * - /api/sendtext
 * - /api/sendmedia (via URL PDF)
 *
 * Penyesuaian dari versi WAHA agar semua fungsi tetap bekerja di WA Gateway Fawwaz
 * Author: Chandra Irawan M.T.I
 */
public class WAKirim {

    private static String readBody(InputStream is) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = in.readLine()) != null) {
        sb.append(line);
    }
    in.close();
    return sb.toString();
}

    private static String extractJsonString(String response, String key) {
    try {
        JSONObject obj = new JSONObject(response);
        return obj.optString(key, null);
    } catch (Exception e) {
        return null;
    }
}

    public static class SendResult {
        public boolean ok;
        public int httpCode;
        public String error;
        public String responseBody;
        private static final String WAHA_BASE_URL   = "https://waha.rspelitakasih.id";
        private static final String AUTH_TOKEN      = "AmpunBangJago"; // samakan dengan upload_lab.php
        private static final String UPLOAD_URL      = WAHA_BASE_URL + "/HasilLab/upload_lab.php";
        
        private static final int    CONNECT_TIMEOUT = 7000;   // ms
        private static final int    READ_TIMEOUT    = 15000;  // ms

        public SendResult(boolean ok, String responseBody) {
            this.ok = ok;
            this.responseBody = responseBody;
        }

        public SendResult() {}
    }
    

    public static SendResult kirimTeksWithStatus(String nomorWA, String pesan) {
        SendResult result = new SendResult();
        HttpURLConnection conn = null;

        try {
            String phone = toPhoneOnly(nomorWA);
            if (phone.isEmpty()) {
                result.ok = false;
                result.error = "Nomor WA tidak valid";
                return result;
            }

            String apiUrl = "http://172.22.10.165:3000/api/sendtext";

            JSONObject payload = new JSONObject();
            payload.put("sessions", "session_1");
            payload.put("target", phone);
            payload.put("message", pesan);

            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int httpCode = conn.getResponseCode();
            result.httpCode = httpCode;

            InputStream is = (httpCode >= 200 && httpCode < 400) ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            result.responseBody = responseBuilder.toString();

            // ✅ Logika toleran terhadap status:500 selama terbukti terkirim
            if ((httpCode == 200 || httpCode == 201) && (
                 result.responseBody.contains("Success") || 
                 result.responseBody.contains("\"status\":true") || 
                 result.responseBody.contains("\"status\":500")
            )) {
                result.ok = true;
            } else {
                result.ok = false;
                result.error = "HTTP " + httpCode + ": " + result.responseBody;
            }

        } catch (Exception e) {
            result.ok = false;
            result.error = e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return result;
    }

    public static void kirimTeks(String nomorWA, String pesan) {
        SendResult r = kirimTeksWithStatus(nomorWA, pesan);
        if (r.ok) {
            System.out.println("✅ Pesan berhasil dikirim ke " + nomorWA);
        } else {
            System.err.println("❌ Gagal kirim WA ke " + nomorWA + ": " + r.error);
            if (r.responseBody != null && !r.responseBody.isEmpty()) {
                System.err.println("Response: " + r.responseBody);
            }
        }
    }

    public static String toPhoneOnly(String noHP) {
        if (noHP == null) return "";
        String cleaned = noHP.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("0")) cleaned = "62" + cleaned.substring(1);
        else if (cleaned.startsWith("8")) cleaned = "62" + cleaned;
        if (!cleaned.matches("^62[0-9]{8,}$")) return "";
        return cleaned;
    }

    public static String uploadPDFToServer(File file) {
    String uploadedUrl = null;
    HttpURLConnection conn = null;

    final String WAHA_BASE_URL   = "https://waha.rspelitakasih.id";
    final String AUTH_TOKEN      = "AmpunBangJago";
    final String UPLOAD_URL      = WAHA_BASE_URL + "/HasilLab/upload_lab.php";
    final int CONNECT_TIMEOUT    = 7000;   // ms
    final int READ_TIMEOUT       = 15000;  // ms

    try {
        if (file == null || !file.exists() || file.length() == 0) {
            System.err.println("❌ File tidak ditemukan atau kosong: " + (file != null ? file.getAbsolutePath() : "null"));
            return null;
        }

        System.out.println("⏫ Mengupload file: " + file.getName());
        System.out.println("📦 Lokasi file: " + file.getAbsolutePath());
        System.out.println("📐 Ukuran file: " + file.length() + " bytes");

        String boundary = "----WAHA-" + Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";

        URL url = new URL(UPLOAD_URL);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (
            OutputStream output = conn.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)
        ) {
            // Header field file
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                  .append(file.getName()).append("\"").append(CRLF);
            writer.append("Content-Type: application/pdf").append(CRLF);
            writer.append(CRLF).flush();

            // File binary content
            Files.copy(file.toPath(), output);
            output.flush();
            writer.append(CRLF).flush();

            // End boundary
            writer.append("--").append(boundary).append("--").append(CRLF).flush();
        }

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();
        String response = readBody(is);

        System.out.println("🔁 Respon HTTP: " + responseCode);
        System.out.println("📨 Respon body: " + response);

        if (responseCode == 200) {
            // Ambil field "url" dari JSON
            String urlFromJson = extractJsonString(response, "url");
            uploadedUrl = (urlFromJson != null && !urlFromJson.isEmpty())
                ? urlFromJson
                : WAHA_BASE_URL + "/HasilLab/" + file.getName();

            System.out.println("✅ Upload sukses! URL: " + uploadedUrl);
        } else {
            System.err.println("❌ Upload gagal: HTTP " + responseCode);
        }

    } catch (Exception e) {
        System.err.println("❌ Exception saat upload: " + e.getMessage());
        e.printStackTrace();
    } finally {
        if (conn != null) conn.disconnect();
    }

    return uploadedUrl;
}
    
    public static SendResult kirimMediaPDF(String nomorWA, String pesan, String fileUrl) {
    SendResult result = new SendResult();
    HttpURLConnection conn = null;

    try {
        String phone = toPhoneOnly(nomorWA);
        if (phone.isEmpty()) {
            result.ok = false;
            result.error = "Nomor WA tidak valid";
            return result;
        }

        String apiUrl = "http://172.22.10.165:3000/api/sendmedia";

        JSONObject payload = new JSONObject();
        payload.put("sessions", "session_1");
        payload.put("target", phone);
        payload.put("message", pesan);
        payload.put("url", fileUrl);

        URL url = new URL(apiUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int httpCode = conn.getResponseCode();
        result.httpCode = httpCode;

        InputStream is = (httpCode >= 200 && httpCode < 400) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        result.responseBody = responseBuilder.toString();
        result.ok = result.responseBody.contains("Success") || result.responseBody.contains("\"status\":true");

        if (!result.ok) {
            result.error = "HTTP " + httpCode + ": " + result.responseBody;
        }

    } catch (Exception e) {
        result.ok = false;
        result.error = e.getMessage();
    } finally {
        if (conn != null) conn.disconnect();
    }

    return result;
}

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }
}