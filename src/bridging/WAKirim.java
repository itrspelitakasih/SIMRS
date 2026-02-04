package bridging;

import fungsi.koneksiDBWa;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * WAKirim - WAHA SAFE SENDER - Config diambil dari database.xml via koneksiDBWa
 * - Anti JSON bug - Aman untuk WAHA WEBJS
 */
public class WAKirim {

    // ===== TIMEOUT =====
    private static final int CONNECT_TIMEOUT = 7000;
    private static final int READ_TIMEOUT = 15000;

    // ===== PUBLIC API =====
    public static boolean kirimText(String nomorWa, String pesan) {
        SendResult r = kirimTextWithStatus(nomorWa, pesan);
        if (r.ok) {
            System.out.println("[WAHA] HTTP Response : " + r.httpCode);
            return true;
        } else {
            System.err.println("[WAHA] Gagal kirim pesan");
            if (r.error != null) {
                System.err.println("[WAHA] " + r.error);
            }
            if (r.responseBody != null && !r.responseBody.isEmpty()) {
                System.err.println("[WAHA] " + r.responseBody);
            }
            return false;
        }
    }

    // ===== CORE LOGIC =====
    private static SendResult kirimTextWithStatus(String nomorWa, String pesan) {

        String baseUrl = koneksiDBWa.WAHA_BASE_URL();
        String apiKey = koneksiDBWa.WAHA_API_KEY();
        String session = koneksiDBWa.SESSION();

        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            return new SendResult(false, 0, null, "Config WAHA belum lengkap");
        }

        String phone = normalizePhone(nomorWa);
        String pesanAman = escapeJson(pesan);

        // === Payload A (PALING STABIL) ===
        String payloadA
                = "{\"receiver\":\"" + phone + "\","
                + "\"message\":\"" + pesanAman + "\"}";

        SendResult r1 = postJson(baseUrl + "/api/sendText", payloadA, apiKey);
        if (r1.ok) {
            return r1;
        }

        // === Payload B (Fallback) ===
        String payloadB
                = "{\"chatId\":\"" + phone + "@c.us\","
                + "\"text\":\"" + pesanAman + "\","
                + "\"session\":\"" + session + "\"}";

        SendResult r2 = postJson(baseUrl + "/api/sendText", payloadB, apiKey);

        if (!r2.ok) {
            String joinErr = "[A:" + r1.error + "] [B:" + r2.error + "]";
            return new SendResult(false, r2.httpCode, r2.responseBody, joinErr);
        }

        return r2;
    }

    // ===== HTTP =====
    private static SendResult postJson(String urlStr, String payload, String apiKey) {
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
            String err = ok ? null : ("HTTP " + code + (body != null ? " | " + body : ""));
            return new SendResult(ok, code, body, err);

        } catch (SocketTimeoutException e) {
            return new SendResult(false, 0, null, "Timeout: " + e.getMessage());
        } catch (Exception e) {
            return new SendResult(false, 0, null, e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readBody(HttpURLConnection conn) {
        try (InputStream is
                = (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                ? conn.getInputStream()
                : conn.getErrorStream()) {

            if (is == null) {
                return "";
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // ===== UTIL =====
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

    public static String toPhoneOnly(String raw) {
        if (raw == null) {
            return "";
        }
        String d = raw.replaceAll("[^0-9]", "");
        if (d.startsWith("0")) {
            d = "62" + d.substring(1);
        } else if (!d.startsWith("62")) {
            d = "62" + d;
        }
        return d;
    }

    public static String getBaseFileUrl() {
        return koneksiDBWa.FILE_BASE_URL();
    }

    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    // ===== RESULT =====
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

    public static String uploadPDFToServer(File file) {
        HttpURLConnection conn = null;
        try {
            String baseUrl = koneksiDBWa.FILE_BASE_URL();
            String uploadUrl = baseUrl + "/HasilLab/upload_lab.php";
            String token = koneksiDBWa.TOKEN();

            String boundary = "----SIMRS-" + System.currentTimeMillis();
            String CRLF = "\r\n";

            URL url = new URL(uploadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

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
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
