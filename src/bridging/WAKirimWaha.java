package bridging;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Util sederhana untuk upload PDF & kirim pesan WhatsApp via WAHA.
 * Versi ini kompatibel dengan fungsi lama dan menambah varian yang mengembalikan status.
 * Pembuat Chandra Irawan M.T.I
    Bagi yang ingin menggunakan dan melakukan perubahan atau penambahan
    sangat di perbolehkan, namun aplikasi ini tidak untuk diperjual/belikan
    bagi yang ingin berdonasi secangkir kopi bisa melalui
    BCA 8110400102 A/N Chandra Irawan 
    ingat untuk tidak DIPERJUAL BELIKAN ini bersifat open source
 */
public class WAKirimWaha {

    // ====== KONFIGURASI ======
    private static final String WAHA_BASE_URL   = "http://172.22.10.134:3000";
    private static final String UPLOAD_URL      = WAHA_BASE_URL + "/HasilLab/upload_lab.php";
    private static final String AUTH_TOKEN      = "SayangAnisaNairaNafasya"; // samakan dengan upload_lab.php

    private static final String API_SEND_PATH   = "/api/sendText"; // relative path
    private static final int    CONNECT_TIMEOUT = 7000;   // ms
    private static final int    READ_TIMEOUT    = 15000;  // ms

    // ====== HASIL KIRIM (baru) ======
    public static class SendResult {
        public final boolean ok;
        public final int httpCode;
        public final String responseBody;
        public final String error; // null jika tidak ada error

        public SendResult(boolean ok, int httpCode, String responseBody, String error) {
            this.ok = ok;
            this.httpCode = httpCode;
            this.responseBody = responseBody;
            this.error = error;
        }
    }
    
    public static String toChatId(String raw) {
        if (raw == null) return "";
        String d = raw.replaceAll("[^0-9]", "");
        if (d.isEmpty()) return "";
        if (d.startsWith("0")) d = "62" + d.substring(1);
        else if (!d.startsWith("62")) d = "62" + d;
        return d + "@c.us";
    }

    /**
     * Upload file PDF hasil lab ke server WAHA (HasilLab/upload_lab.php).
     * @param file PDF yang akan diupload
     * @return URL publik hasil upload, atau null jika gagal
     */
    public static String uploadPDFToServer(File file) {
        String uploadedUrl = null;
        HttpURLConnection conn = null;
        try {
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

            try (OutputStream output = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

                // field file
                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(file.getName()).append("\"").append(CRLF);
                writer.append("Content-Type: application/pdf").append(CRLF);
                writer.append(CRLF).flush();

                Files.copy(file.toPath(), output);
                output.flush();
                writer.append(CRLF).flush();

                // end boundary
                writer.append("--").append(boundary).append("--").append(CRLF).flush();
            }

            int responseCode = conn.getResponseCode();
            String response  = readBody(conn);

            if (responseCode == 200) {
                // Ambil "url" dari JSON { "ok": true, "url": "..." }
                String urlFromJson = extractJsonString(response, "url");
                if (urlFromJson != null && !urlFromJson.isEmpty()) {
                    uploadedUrl = urlFromJson;
                } else {
                    // fallback kalau server tidak mengembalikan url
                    uploadedUrl = WAHA_BASE_URL + "/HasilLab/" + file.getName();
                }
                System.out.println("✅ Upload berhasil ke: " + uploadedUrl);
            } else {
                System.out.println("❌ Upload gagal: HTTP " + responseCode + " | " + response);
            }

        } catch (Exception e) {
            System.out.println("❌ Gagal upload PDF: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return uploadedUrl;
    }

    /**
     * Fungsi lama: kirim teks ke WAHA (tetap dipertahankan).
     * - Tidak melempar exception ke pemanggil (aman dipanggil dimana saja).
     * - Akan log status & error bila gagal.
     */
    public static void kirimTeks(String chatId, String pesan) {
        SendResult r = kirimTeksWithStatus(chatId, pesan);
        if (r.ok) {
            System.out.println("WA kirim teks, status: " + r.httpCode);
        } else {
            System.err.println("Gagal kirim teks ke WA: " + (r.error != null ? r.error : "HTTP " + r.httpCode));
            if (r.responseBody != null && !r.responseBody.isEmpty()) {
                System.err.println("Response: " + r.responseBody);
            }
        }
    }

    /**
     * Fungsi baru: kirim teks & kembalikan status detail.
     * - Default payload: {"receiver":"628xxx","message":"..."}
     * - Fallback payload (jika gagal): {"chatId":"628xxx@c.us","text":"...","session":"default"}
     */
    public static SendResult kirimTeksWithStatus(String chatId, String pesan) {
        // 1) Coba payload tipe A (receiver/message)
        String receiver = chatId;
        // Kalau chatId kamu berformat "62xxx@c.us", kita hapus suffix untuk mode receiver/message
        if (receiver.endsWith("@c.us")) {
            receiver = receiver.substring(0, receiver.length() - 5);
        }
        String payloadA = "{\"receiver\":\"" + escapeJson(receiver) + "\",\"message\":\"" + escapeJson(pesan) + "\"}";

        SendResult r1 = postJson(WAHA_BASE_URL + API_SEND_PATH, payloadA);
        if (r1.ok) return r1;

        // 2) Fallback payload tipe B (chatId/text/session)
        String chatIdB = chatId.endsWith("@c.us") ? chatId : (chatId + "@c.us");
        String payloadB = "{\"chatId\":\"" + escapeJson(chatIdB) + "\",\"text\":\"" + escapeJson(pesan) + "\",\"session\":\"default\"}";

        SendResult r2 = postJson(WAHA_BASE_URL + API_SEND_PATH, payloadB);

        // Jika dua-duanya gagal, kembalikan r2 (fallback) tapi sertakan info r1 untuk diagnosa
        if (!r2.ok && r1.error != null) {
            String joinErr = "[prim:" + r1.error + "] [fb:" + (r2.error != null ? r2.error : ("HTTP " + r2.httpCode)) + "]";
            return new SendResult(false, r2.httpCode, r2.responseBody, joinErr);
        }
        return r2;
    }

    // ====== UTIL HTTP ======

    private static SendResult postJson(String urlStr, String payload) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            String body = readBody(conn);

            boolean ok = (code >= 200 && code < 300);
            String err = ok ? null : ("HTTP " + code + (body != null && !body.isEmpty() ? (" | " + body) : ""));
            return new SendResult(ok, code, body, err);

        } catch (SocketTimeoutException e) {
            return new SendResult(false, 0, null, "Timeout: " + e.getMessage());
        } catch (IOException e) {
            return new SendResult(false, 0, null, "IO error: " + e.getMessage());
        } catch (Exception e) {
            return new SendResult(false, 0, null, "Error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String readBody(HttpURLConnection conn) {
        try (InputStream is = (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                ? conn.getInputStream()
                : conn.getErrorStream()) {
            if (is == null) return "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } catch (Exception e) {
            return "";
        }
    }

    // ====== UTIL JSON (simple) ======

    /** Mengambil nilai string untuk key pertama yang ditemukan di JSON sederhana. */
    private static String extractJsonString(String json, String key) {
        if (json == null) return null;
        // cari "key"
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        // cari titik dua setelah key
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0) return null;

        // lompat spasi
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

        if (i >= json.length()) return null;

        if (json.charAt(i) == '\"') {
            // String
            int start = i + 1;
            int end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == '\\') { end += 2; continue; } // skip escape
                if (c == '\"') break;
                end++;
            }
            if (end >= json.length()) return null;
            return json.substring(start, end);
        } else {
            // Bukan string (number/boolean/null) — tidak kita tangani di sini
            return null;
        }
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}