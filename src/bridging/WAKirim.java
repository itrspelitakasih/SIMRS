package bridging;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WAKirim {

    // =========================
    // KONFIGURASI
    // =========================
    private static final String WAHA_BASE_URL = "http://172.22.10.134:3031";
    private static final String SESSION_NAME = "default";
    private static final String AUTH_TOKEN = "616e62d798524494b9a6a7655cc0d0ac";
    private static final String UPLOAD_BASE_URL = "https://apps.rspelitakasih.id/HasilLab/";

    // =========================
    // RESULT CLASS
    // =========================
    public static class SendResult {
        public boolean ok;
        public int httpCode;
        public String error;
        public String responseBody;
    }

    // =========================
    // UTIL: BACA STREAM
    // =========================
    private static String readBody(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    // =========================
    // UTIL: NORMALISASI NOMOR
    // =========================
    public static String toPhoneOnly(String noHP) {
        if (noHP == null) return "";

        String cleaned = noHP.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("0")) {
            cleaned = "62" + cleaned.substring(1);
        } else if (cleaned.startsWith("8")) {
            cleaned = "62" + cleaned;
        }

        if (!cleaned.matches("^62[0-9]{8,}$")) return "";
        return cleaned;
    }

    // =========================
    // KIRIM TEKS WA
    // =========================
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

            URL url = new URL(WAHA_BASE_URL + "/api/sendText");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-Key", AUTH_TOKEN);

            JSONObject json = new JSONObject();
            json.put("session", SESSION_NAME);
            json.put("chatId", phone + "@c.us");
            json.put("text", pesan);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }

            result.httpCode = conn.getResponseCode();
            InputStream is = (result.httpCode >= 200 && result.httpCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            result.responseBody = readBody(is);
            result.ok = result.httpCode >= 200 && result.httpCode < 300;

            if (!result.ok) result.error = "HTTP " + result.httpCode;

        } catch (Exception e) {
            result.ok = false;
            result.error = e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return result;
    }

    // =========================
    // UPLOAD PDF KE SERVER
    // =========================
    public static String uploadPDFToServer(File file) {
        HttpURLConnection conn = null;
        String boundary = "----Boundary" + System.currentTimeMillis();
        String CRLF = "\r\n";

        try {
            if (file == null || !file.exists()) return null;

            URL url = new URL(UPLOAD_BASE_URL + "upload_lab.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("X-API-Key", AUTH_TOKEN);

            try (
                    OutputStream output = conn.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)
            ) {
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

            int code = conn.getResponseCode();
            if (code == 200) {
                return UPLOAD_BASE_URL + file.getName();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }
}