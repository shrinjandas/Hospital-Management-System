import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.*;

// --- BILLABLE INTERFACE ---
interface Billable { double calculateTotalBill(); }

// --- BASE CLASS: PERSON ---
abstract class Person {
    private String id, name;
    private int age;

    public Person(String id, String name, int age) { this.id = id; this.name = name; setAge(age); }
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = (age >= 0 && age <= 125) ? age : 0; }
    public abstract String getRoleDescription();
    public abstract String getSummaryDetails();
}

// --- HOSPITAL MEMBER: DOCTOR ---
class Doctor extends Person {
    private String department, specialization;
    private double baseFee;

    public Doctor(String id, String name, int age, String dept, String spec, double fee) {
        super(id, name, age); this.department = dept; this.specialization = spec; this.baseFee = Math.max(0.0, fee);
    }
    public double getBaseFee() { return baseFee; }
    public String getSpecialization() { return specialization; }
    public double computeEffectiveFee(boolean emergency) { return emergency ? (baseFee * 1.50) : baseFee; }

    @Override public String getRoleDescription() { return "Specialist Medical Practitioner"; }
    @Override public String getSummaryDetails() { return String.format("Dr. %s (%s) | Dept: %s", getName(), specialization, department); }
}

// --- HOSPITAL MEMBER: PATIENT ---
class Patient extends Person {
    private String diagnosis, contact, category;

    public Patient(String id, String name, int age, String diag, String contact, String category) {
        super(id, name, age); this.diagnosis = diag; this.contact = contact; this.category = category;
    }
    public String getDiagnosis() { return diagnosis; }
    public String getContact() { return contact; }
    public String getCategory() { return category; }

    @Override public String getRoleDescription() { return "Admitted Patient [" + category + "]"; }
    @Override public String getSummaryDetails() { return String.format("%s (Age: %d) | Ph: %s", getName(), getAge(), contact); }
}

// --- APPOINTMENT & BILLING ---
class Appointment implements Billable {
    private String refId;
    private Patient patient;
    private Doctor doctor;
    private boolean isEmergency;
    private LocalDateTime timestamp = LocalDateTime.now();

    public Appointment(String refId, Patient p, Doctor d, boolean isEmergency) {
        this.refId = refId; this.patient = p; this.doctor = d; this.isEmergency = isEmergency;
    }

    @Override public double calculateTotalBill() { return doctor.computeEffectiveFee(isEmergency) * 1.05; }

    public String generateInvoiceSlip() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        double base = doctor.getBaseFee(), eff = doctor.computeEffectiveFee(isEmergency);
        return String.format(
            "========================================================\n" +
            "           APOLLO HEALTHCARE CLINICAL INVOICE           \n" +
            "========================================================\n" +
            "Reference ID : %-22s\nDate & Time  : %s\n" +
            "--------------------------------------------------------\n" +
            "PATIENT SUMMARY:\n  ID / Name  : %s / %s\n  Age / Phone: %d yrs | %s\n  Diagnosis  : %s\n  Category   : %s\n" +
            "--------------------------------------------------------\n" +
            "ATTENDING CONSULTANT:\n  Doctor     : %s\n  Role       : %s\n" +
            "--------------------------------------------------------\n" +
            "FEE BREAKDOWN (INR):\n  Base Fee             : ₹%,10.2f\n  Emergency Priority   : %s\n" +
            (isEmergency ? String.format("  Surge Subtotal       : ₹%,10.2f\n", eff) : "") +
            "  Hospital Tax (5%%)    : ₹%,10.2f\n--------------------------------------------------------\n" +
            "  TOTAL NET PAYABLE    : ₹%,10.2f\n" +
            "========================================================\n" +
            "       Status: Payment Confirmed & Slot Booked          \n" +
            "========================================================\n",
            refId, timestamp.format(dtf), patient.getId(), patient.getName(), patient.getAge(),
            patient.getContact(), patient.getDiagnosis(), patient.getCategory(), doctor.getSummaryDetails(),
            doctor.getRoleDescription(), base, (isEmergency ? "YES (+50% Surge)" : "NO (Standard)"),
            eff * 0.05, calculateTotalBill()
        );
    }
}

// Custom button renderer to keep colors visible on Windows
class ModernButton extends JButton {
    public ModernButton(String text, Color bg, Color hover, Color fg) {
        super(text); setForeground(fg); setBackground(bg);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { setBackground(hover); repaint(); }
            public void mouseExited(MouseEvent e) { setBackground(bg); repaint(); }
        });
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        super.paintComponent(g2);
        g2.dispose();
    }
}

// --- FRONTEND GUI & MAIN DRIVER ---
public class HospitalManagementSystem extends JFrame {
    private JTextField txtId, txtName, txtAge, txtDiag, txtContact;
    private JComboBox<String> cmbCat, cmbDoc;
    private JCheckBox chkEmerg;
    private JTextArea txtInvoice;

    private Doctor[] doctors = {
        new Doctor("DOC-01", "Arun Mukherjee", 48, "Cardiology", "Cardiology", 1200.0),
        new Doctor("DOC-02", "Priyanka Sen", 41, "Neurology", "Neurology", 1500.0),
        new Doctor("DOC-03", "Rajesh Sharma", 53, "Orthopedics", "Orthopedics", 900.0)
    };

    public HospitalManagementSystem() {
        setTitle("Hospital Management Information System (HMIS) - Prototype Console");
        setSize(1150, 680); setMinimumSize(new Dimension(1050, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(243, 246, 250)); setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(15, 34, 64)); header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("HOSPITAL MANAGEMENT INFORMATION SYSTEM"), sub = new JLabel("PCC-CS-503 Prototype Submission | Front Desk & Inpatient Billing");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18)); title.setForeground(Color.WHITE);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12)); sub.setForeground(new Color(190, 215, 245));
        header.add(title); header.add(sub);
        add(header, BorderLayout.NORTH);

        // GridBag Layout Split to prevent text truncation on long labels & dropdowns
        JPanel mainSplit = new JPanel(new GridBagLayout());
        mainSplit.setOpaque(false);
        mainSplit.setBorder(new EmptyBorder(14, 18, 14, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Form Card (Using larger horizontal weighting to allocate drop-down space)
        JPanel leftCard = createCard("Patient Registration & Consultant Assignment");
        JPanel grid = new JPanel(new GridLayout(8, 2, 10, 8)); grid.setOpaque(false);

        grid.add(lbl("Patient ID:")); grid.add(txtId = field("P-" + (1000 + (int)(Math.random() * 9000))));
        grid.add(lbl("Full Name:")); grid.add(txtName = field(""));
        grid.add(lbl("Age (Years):")); grid.add(txtAge = field(""));
        grid.add(lbl("Diagnosis / Symptoms:")); grid.add(txtDiag = field(""));
        grid.add(lbl("Phone Number:")); grid.add(txtContact = field(""));

        grid.add(lbl("Admission Type:"));
        grid.add(cmbCat = new JComboBox<>(new String[]{"Out-Patient (OPD)", "In-Patient (General Ward)", "Day Care Surgery"}));
        
        grid.add(lbl("Consultant:"));
        String[] opts = new String[doctors.length];
        for (int i = 0; i < doctors.length; i++) {
            opts[i] = String.format("%s - %s (%s) [₹%d]", 
                doctors[i].getId(), doctors[i].getName(), doctors[i].getSpecialization(), (int)doctors[i].getBaseFee());
        }
        cmbDoc = new JComboBox<>(opts);
        cmbDoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        grid.add(cmbDoc);

        grid.add(lbl("Priority Level:"));
        chkEmerg = new JCheckBox("Emergency Case (+50% Surge)");
        chkEmerg.setFont(new Font("Segoe UI", Font.BOLD, 12)); chkEmerg.setForeground(new Color(190, 30, 30)); chkEmerg.setOpaque(false);
        grid.add(chkEmerg);
        leftCard.add(grid, BorderLayout.CENTER);

        // Left Card Action buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8)); btnBar.setOpaque(false);
        ModernButton btnReset = new ModernButton("Reset", new Color(225, 230, 236), new Color(205, 212, 220), new Color(60, 70, 80));
        ModernButton btnGen = new ModernButton("Generate Consultation Receipt (₹)", new Color(13, 148, 136), new Color(15, 118, 110), Color.WHITE);
        btnReset.setPreferredSize(new Dimension(85, 34)); btnGen.setPreferredSize(new Dimension(250, 34));
        btnBar.add(btnReset); btnBar.add(btnGen);
        leftCard.add(btnBar, BorderLayout.SOUTH);

        // Add Card with wider grid allotment
        gbc.gridx = 0; gbc.weightx = 0.54; gbc.insets = new Insets(0, 0, 0, 10);
        mainSplit.add(leftCard, gbc);

        // Right Invoice Card
        JPanel rightCard = createCard("Live Electronic Slip & Fee Breakdown");
        txtInvoice = new JTextArea("\n  Click \"Generate Consultation Receipt (₹)\" \n  to calculate fees and display the complete bill slip.");
        txtInvoice.setEditable(false); txtInvoice.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtInvoice.setBackground(new Color(248, 250, 252)); txtInvoice.setBorder(new EmptyBorder(10, 12, 10, 12));
        rightCard.add(new JScrollPane(txtInvoice), BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.46; gbc.insets = new Insets(0, 0, 0, 0);
        mainSplit.add(rightCard, gbc);

        add(mainSplit, BorderLayout.CENTER);

        // Event triggers
        btnGen.addActionListener(e -> processOrder());
        btnReset.addActionListener(e -> {
            txtId.setText("P-" + (1000 + (int)(Math.random() * 9000)));
            txtName.setText(""); txtAge.setText(""); txtDiag.setText(""); txtContact.setText("");
            chkEmerg.setSelected(false);
            txtInvoice.setText("\n  Form reset. Fill details and generate a receipt.");
        });
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10)); card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(210, 220, 230), 1, true), new EmptyBorder(14, 18, 14, 18)));
        JLabel h = new JLabel(title); h.setFont(new Font("Segoe UI", Font.BOLD, 13)); h.setForeground(new Color(15, 34, 64));
        card.add(h, BorderLayout.NORTH);
        return card;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(new Color(45, 55, 72)); return l;
    }

    private JTextField field(String t) {
        JTextField f = new JTextField(t); f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(210, 218, 228), 1, true), new EmptyBorder(4, 8, 4, 8))); return f;
    }

    private void processOrder() {
        if (txtName.getText().trim().isEmpty() || txtAge.getText().trim().isEmpty() || txtDiag.getText().trim().isEmpty() || txtContact.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are mandatory.", "Validation Error", JOptionPane.ERROR_MESSAGE); return;
        }
        try {
            int age = Integer.parseInt(txtAge.getText().trim());
            Patient p = new Patient(txtId.getText().trim(), txtName.getText().trim(), age, txtDiag.getText().trim(), txtContact.getText().trim(), (String)cmbCat.getSelectedItem());
            Appointment apt = new Appointment("APT-" + (10000 + (System.currentTimeMillis() % 90000)), p, doctors[cmbDoc.getSelectedIndex()], chkEmerg.isSelected());
            String slip = apt.generateInvoiceSlip();
            txtInvoice.setText(slip); txtInvoice.setCaretPosition(0);
            System.out.println("\n[SYSTEM EVENT - APPOINTMENT REGISTERED]\n" + slip);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric age.", "Invalid Age", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HospitalManagementSystem().setVisible(true));
    }
}