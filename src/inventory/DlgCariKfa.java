/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile 
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */
package inventory;

import bridging.ApiSatuSehat;
import bridging.SatuSehatMapingObatAlkes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class DlgCariKfa extends javax.swing.JDialog {

    private DefaultTableModel tabMode;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private int i = 0;
    private ResultSet rs;
    private String link = "", json = "";
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode nameNode;
    private JsonNode response;
    private ApiSatuSehat api = new ApiSatuSehat();

    public DlgCariKfa(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode = new DefaultTableModel(null, new Object[]{
            "KFA Code", "KFA Display", "Form Code", "Form Display", "Numerator Code", "Denomina Code", "Route Code", "Route Display", "Registrar", "Nama Dagang"
        }) {
            Class[] types = new Class[]{
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbObat.setModel(tabMode);

        tbObat.setPreferredScrollableViewportSize(new Dimension(800, 800));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 10; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(80);
            } else if (i == 1) {
                column.setPreferredWidth(400);
            } else if (i == 2) {
                column.setPreferredWidth(80);
            } else if (i == 3) {
                column.setPreferredWidth(80);
            } else if (i == 4) {
                column.setPreferredWidth(80);
            } else if (i == 5) {
                column.setPreferredWidth(80);
            } else if (i == 6) {
                column.setPreferredWidth(80);
            } else if (i == 7) {
                column.setPreferredWidth(80);
            } else if (i == 8) {
                column.setPreferredWidth(200);
            } else if (i == 9) {
                column.setPreferredWidth(200);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        isForm();

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        jPanel2 = new javax.swing.JPanel();
        panelisi2 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        label13 = new widget.Label();
        TCari2 = new widget.TextBox();
        BtnCari = new widget.Button();
        label10 = new widget.Label();
        LCount = new widget.Label();
        jLabel6 = new widget.Label();
        cmbHlm = new widget.ComboBox();
        BtnKeluar = new widget.Button();
        BtnUpdateKFA = new widget.Button();
        BtnCariOnline = new widget.Button();
        scrollPane1 = new widget.ScrollPane();
        tbObat = new widget.Table();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Cari KFA ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setMinimumSize(new java.awt.Dimension(552, 149));
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(670, 770));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(816, 100));
        jPanel2.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi2.setBackground(new java.awt.Color(255, 150, 255));
        panelisi2.setName("panelisi2"); // NOI18N
        panelisi2.setPreferredSize(new java.awt.Dimension(100, 44));
        panelisi2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label9.setText("Key Word :");
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(label9);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(200, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi2.add(TCari);

        label13.setText("Registarr:");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(label13);

        TCari2.setName("TCari2"); // NOI18N
        TCari2.setPreferredSize(new java.awt.Dimension(150, 23));
        TCari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCari2KeyPressed(evt);
            }
        });
        panelisi2.add(TCari2);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelisi2.add(BtnCari);

        label10.setText("Record :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(label10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi2.add(LCount);

        jLabel6.setText("Limit Data :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(jLabel6);

        cmbHlm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "100", "200", "300", "400", "500", "1000", "Semua" }));
        cmbHlm.setName("cmbHlm"); // NOI18N
        cmbHlm.setPreferredSize(new java.awt.Dimension(90, 23));
        panelisi2.add(cmbHlm);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelisi2.add(BtnKeluar);

        BtnUpdateKFA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/search_page.png"))); // NOI18N
        BtnUpdateKFA.setMnemonic('K');
        BtnUpdateKFA.setText("Update KFA");
        BtnUpdateKFA.setToolTipText("Alt+K");
        BtnUpdateKFA.setName("BtnUpdateKFA"); // NOI18N
        BtnUpdateKFA.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnUpdateKFA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateKFAActionPerformed(evt);
            }
        });
        BtnUpdateKFA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateKFAKeyPressed(evt);
            }
        });
        panelisi2.add(BtnUpdateKFA);

        BtnCariOnline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariOnline.setMnemonic('3');
        BtnCariOnline.setText("Cari Online");
        BtnCariOnline.setToolTipText("Alt+3");
        BtnCariOnline.setName("BtnCariOnline"); // NOI18N
        BtnCariOnline.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnCariOnline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariOnlineActionPerformed(evt);
            }
        });
        BtnCariOnline.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariOnlineKeyPressed(evt);
            }
        });
        panelisi2.add(BtnCariOnline);

        jPanel2.add(panelisi2, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel2, java.awt.BorderLayout.PAGE_END);

        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbObatKeyReleased(evt);
            }
        });
        scrollPane1.setViewportView(tbObat);

        internalFrame1.add(scrollPane1, java.awt.BorderLayout.CENTER);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(660, 245));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        tampil();
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();

}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        tampil();
}//GEN-LAST:event_BtnCariKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked

}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
                TCari.setText("");
                TCari.requestFocus();
            }
            if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                java.awt.Window[] windows = java.awt.Window.getWindows();
                for (java.awt.Window window : windows) {
                    if (window instanceof SatuSehatMapingObatAlkes) {
                        SatuSehatMapingObatAlkes parentForm = (SatuSehatMapingObatAlkes) window;
                        parentForm.KFASystem.setText("http://sys-ids.kemkes.go.id/kfa");
                        parentForm.FormSystem.setText("http://terminology.kemkes.go.id/CodeSystem/medication-form");
                        parentForm.NemeratorSystem.setText("http://unitsofmeasure.org");
                        parentForm.DenominatorSystem.setText("http://terminology.hl7.org/CodeSystem/v3-orderableDrugForm");
                        parentForm.RouteSystem.setText("http://www.whocc.no/atc");
                        //parentForm.KFADisplay.getText("name");

                        break;
                    }
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased

    }//GEN-LAST:event_tbObatKeyReleased

    private void TCari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCari2KeyPressed
        tampil();
    }//GEN-LAST:event_TCari2KeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        dispose();
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnUpdateKFAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateKFAActionPerformed
        link = "https://api-satusehat.kemkes.go.id/kfa-v2/";
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            requestEntity = new HttpEntity(headers);
            int limit = 100;
            for (int i = 1; i <= 180; i++) {
                System.out.println("Iterasi ke : " + i);
                json = api.getRest().exchange(link + "products/all?page=" + i + "&size=" + limit + "&product_type=farmasi", HttpMethod.GET, requestEntity, String.class).getBody();
                root = mapper.readTree(json);
                JsonNode dataArray = root.path("items").path("data");
                for (JsonNode data : dataArray) {
                    // Extract all fields and create a DataObject instance
                    String name = data.path("name").asText();
                    String kfaCode = data.path("kfa_code").asText();
                    String active = data.path("active").asText();
                    String state = data.path("state").asText();
                    String image = data.path("image").asText();
                    String updatedAt = data.path("updated_at").asText();
                    String produksiBuatan = data.path("produksi_buatan").asText();
                    String nie = data.path("nie").asText();
                    String namaDagang = data.path("nama_dagang").asText();
                    String manufacturer = data.path("manufacturer").asText();
                    String registrar = data.path("registrar").asText();
                    String generik = data.path("generik").asText();
                    String rxterm = data.path("rxterm").asText();
                    String dosePerUnit = data.path("dose_per_unit").asText();
                    String fixPrice = data.path("fix_price").asText();
                    String hetPrice = data.path("het_price").asText();
                    String farmalkesHscode = data.path("farmalkes_hscode").asText();
                    String tayangLkpp = data.path("tayang_lkpp").asText();
                    String kodeLkpp = data.path("kode_lkpp").asText();
                    String netWeight = data.path("net_weight").asText();
                    String netWeightUomName = data.path("net_weight_uom_name").asText();
                    String volume = data.path("volume").asText();
                    String volumeUomName = data.path("volume_uom_name").asText();
                    String dosageFormCode = data.path("dosage_form").path("code").asText();
                    String dosageFormName = data.path("dosage_form").path("name").asText();
                    String productTemplateKfaCode = data.path("product_template").path("kfa_code").asText();
                    String productTemplateName = data.path("product_template").path("name").asText();
                    String productTemplateState = data.path("product_template").path("state").asText();
                    String productTemplateActive = data.path("product_template").path("active").asText();
                    String productTemplateDisplayName = data.path("product_template").path("display_name").asText();
                    String productTemplateUpdatedAt = data.path("product_template").path("updated_at").asText();

                    // Store data into MySQL table using Sequel.menyimpan method
                    if (Sequel.menyimpantf2("satu_sehat_kfa_master", "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
                            "Kfa Master",
                            31, // 31 columns
                            new String[]{name, kfaCode, active, state, image, updatedAt, produksiBuatan, nie, namaDagang, manufacturer, registrar, generik, rxterm, dosePerUnit, fixPrice, hetPrice, farmalkesHscode, tayangLkpp, kodeLkpp, netWeight, netWeightUomName, volume, volumeUomName, dosageFormCode, dosageFormName, productTemplateKfaCode, productTemplateName, productTemplateState, productTemplateActive, productTemplateDisplayName, productTemplateUpdatedAt}
                    ) == true) {
                        System.out.println("Sukses menyimpan : " + name);
                    } else {
                        System.out.println("gagal simpan, duplicate");
                    }
                }

                Thread.sleep(5000);

            }
        } catch (Exception ea) {
            System.out.println("Notifikasi Bridging : " + ea);
            // miningKFA();
        }
    }//GEN-LAST:event_BtnUpdateKFAActionPerformed

    private void BtnUpdateKFAKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateKFAKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateKFAKeyPressed

    private void BtnCariOnlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariOnlineActionPerformed
        // Pencarian KFA dari API Satu Sehat
        Valid.tabelKosong(tabMode);

        if (TCari.getText().trim().equals("")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Masukkan keyword pencarian terlebih dahulu!");
            TCari.requestFocus();
            return;
        }

        link = "https://api-satusehat.kemkes.go.id/kfa-v2/";
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            requestEntity = new HttpEntity(headers);

            // Encode keyword untuk URL
            String keyword = java.net.URLEncoder.encode(TCari.getText().trim(), "UTF-8");
            String registrar = "";

            // Jika ada filter registrar
            if (!TCari2.getText().trim().equals("")) {
                registrar = "&registrar=" + java.net.URLEncoder.encode(TCari2.getText().trim(), "UTF-8");
            }

            // Set limit dari combobox
            String limit = cmbHlm.getSelectedItem().toString();
            if (limit.equals("Semua")) {
                limit = "1000"; // Maksimal 1000 untuk API
            }

            // Panggil API dengan parameter pencarian
            String url = link + "products/all?page=1&size=" + limit + "&product_type=farmasi&name=" + keyword + registrar;
            System.out.println("URL API: " + url);

            json = api.getRest().exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
            root = mapper.readTree(json);

            // Parse response
            JsonNode dataArray = root.path("items").path("data");

            if (dataArray.isArray() && dataArray.size() > 0) {
                for (JsonNode data : dataArray) {
                    // Extract data dari response
                    String kfaCode = data.path("kfa_code").asText();
                    String name = data.path("name").asText();
                    String registrarData = data.path("registrar").asText();
                    String namaDagang = data.path("nama_dagang").asText();

                    // Ambil data dosage form
                    String dosageFormCode = data.path("dosage_form").path("code").asText();
                    String dosageFormName = data.path("dosage_form").path("name").asText();

                    // Untuk data detail seperti route dan ucum, perlu dicek dari product template atau data lain
                    // Karena API KFA v2 memiliki struktur yang berbeda
                    String ucumCode = "";
                    String routeCode = "";
                    String routeName = "";

                    // Tambahkan ke tabel
                    tabMode.addRow(new Object[]{
                        kfaCode,
                        name,
                        dosageFormCode,
                        dosageFormName,
                        ucumCode,
                        "", // Denomina code (kosong karena tidak ada di response)
                        routeCode,
                        routeName,
                        registrarData,
                        namaDagang
                    });
                }

                LCount.setText("" + tabMode.getRowCount());
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Berhasil mengambil " + tabMode.getRowCount() + " data dari API KFA");
            } else {
                LCount.setText("0");
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Data tidak ditemukan untuk keyword: " + TCari.getText().trim());
            }

        } catch (Exception ex) {
            System.out.println("Notifikasi Error Pencarian KFA Online: " + ex);
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Gagal melakukan pencarian online: " + ex.getMessage());
        }
    }//GEN-LAST:event_BtnCariOnlineActionPerformed

    private void BtnCariOnlineKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariOnlineKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE || evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariOnlineActionPerformed(null);
        }
    }//GEN-LAST:event_BtnCariOnlineKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgCariKfa dialog = new DlgCariKfa(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnCari;
    private widget.Button BtnCariOnline;
    private widget.Button BtnKeluar;
    private widget.Button BtnUpdateKFA;
    private widget.CekBox ChkInput;
    private widget.Label LCount;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox TCari;
    private widget.TextBox TCari2;
    private widget.ComboBox cmbHlm;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel6;
    private javax.swing.JPanel jPanel2;
    private widget.Label label10;
    private widget.Label label13;
    private widget.Label label9;
    private widget.panelisi panelisi2;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().trim().equals("")) {
                ps = koneksi.prepareStatement(
                        "select satu_sehat_kfa_master.kfa_code,satu_sehat_kfa_master.name,satu_sehat_kfa_master_detail.dosage_form_code,satu_sehat_kfa_master_detail.dosage_form_name,"
                        + "satu_sehat_kfa_master_detail.ucum_cs_code,satu_sehat_kfa_master_detail.rute_pemberian_code,satu_sehat_kfa_master_detail.rute_pemberian_name, satu_sehat_kfa_master.registrar,"
                        + "satu_sehat_kfa_master.nama_dagang from satu_sehat_kfa_master inner join satu_sehat_kfa_master_detail on satu_sehat_kfa_master.kfa_code=satu_sehat_kfa_master_detail.kfa_code "
                        + "order by kfa_code,satu_sehat_kfa_master.name asc limit " + cmbHlm.getSelectedItem().toString() + " ");

            } else {
                ps = koneksi.prepareStatement(
                        "select satu_sehat_kfa_master.kfa_code,satu_sehat_kfa_master.name,satu_sehat_kfa_master_detail.dosage_form_code,satu_sehat_kfa_master_detail.dosage_form_name,"
                        + "satu_sehat_kfa_master_detail.ucum_cs_code,satu_sehat_kfa_master_detail.rute_pemberian_code,satu_sehat_kfa_master_detail.rute_pemberian_name,satu_sehat_kfa_master.registrar,"
                        + "satu_sehat_kfa_master.nama_dagang from satu_sehat_kfa_master inner join satu_sehat_kfa_master_detail on satu_sehat_kfa_master.kfa_code=satu_sehat_kfa_master_detail.kfa_code "
                        + "where satu_sehat_kfa_master.registrar like ? and satu_sehat_kfa_master.name like ? order by kfa_code,satu_sehat_kfa_master.name asc limit " + cmbHlm.getSelectedItem().toString() + " ");
            }

            try {
                if (!TCari.getText().trim().equals("")) {
                    ps.setString(1, "%" + TCari2.getText().trim() + "%");
                    ps.setString(2, "%" + TCari.getText().trim() + "%");
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("kfa_code"), rs.getString("name"), rs.getString("dosage_form_code"), rs.getString("dosage_form_name"),
                        rs.getString("ucum_cs_code"), "", rs.getString("rute_pemberian_code"), rs.getString("rute_pemberian_name"),
                        rs.getString("registrar"), rs.getString("nama_dagang")
                    });
                }
                LCount.setText("" + tabMode.getRowCount());
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }

                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }

    }

    public JTable getTable() {
        return tbObat;
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 245));
            //FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            //FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

    public void isCek() {
        TCari.requestFocus();

    }

}
