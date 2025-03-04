/**
 *
 */
package com.zs.ina.admin.reports.view;

import com.zs.ina.admin.master.retrieve.RetrieveStaticMasterDAO;
import com.zs.ina.admin.reports.JRViewerFx;
import com.zs.ina.admin.reports.JRViewerFxMode;
import com.zs.ina.admin.reports.TransactionResult;
import com.zs.ina.admin.reports.WarningToast;
import com.zs.ina.common.UiiDGenerator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.imageio.ImageIO;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import zs.com.ina.db.mysql.pool.DBPool;

/**
 * @author Michael Grecol
 * @project JasperViewerFx
 * @filename JRViewerFxController.java
 * @date Mar 23, 2015
 */
public class FXMLReportViewerController implements Initializable {

    private JRViewerFxMode printMode;
    private String reportFilename;
    private JRDataSource reportDataset;
    @SuppressWarnings("rawtypes")
    private Map reportParameters;
    private ChangeListener<Number> zoomListener;
    private JasperPrint jasperPrint;
    @FXML
    private ImageView imageView;
    @FXML
    ComboBox<Integer> pageList;
    @FXML
    Slider zoomLevel;
    @FXML
    private TitledPane resultPane;
    @FXML
    private Accordion resultAccordion;
    @FXML
    private Label resultDescription;
    @FXML
    protected Node view;
    @FXML
    private ImageView imgViewLogo;
    @FXML
    private Button btnFirstPage;
    @FXML
    private Button btnPreviousPage;
    @FXML
    private Button btnLastPage;
    @FXML
    private Button btnNextPage;
    @FXML
    private ComboBox<String> cmbBranch;
    ObservableList branch = FXCollections.observableArrayList();
    private Stage parentStage;
    private Double zoomFactor;
    private double imageHeight;
    private double imageWidth;
    private List<Integer> pages;
    private Popup popup;
    private Label errorLabel;
    boolean showingToast;
    /**
     *
     */
    public void show() {
        if (reportParameters == null) {
            reportParameters = new HashMap();
        }
        if (printMode == null || printMode == JRViewerFxMode.REPORT_VIEW) {
            //	parentStage = new Stage();
            //Scene scene = new Scene((Parent) view);
//			parentStage.setTitle("Report Viewer");
//			parentStage.setIconified(true);
//			parentStage.initStyle(StageStyle.UNIFIED);
//			parentStage.setScene(scene);
//			parentStage.show();
            popup = new Popup();
            errorLabel = new Label("Error");
            errorLabel.setWrapText(true);
            errorLabel.setMaxHeight(200);
            errorLabel.setMinSize(100, 100);
            errorLabel.setMaxWidth(100);
            errorLabel.setAlignment(Pos.TOP_LEFT);
            errorLabel.getStyleClass().add("errorToastLabel");
            //errorLabel
            //		.setStyle("-fx-border-color: orange; -fx-border-width: 4; -fx-background-color: navajowhite; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black; -fx-effect: dropshadow( gaussian , rgba(255,255,255,0.5) , 0,0,0,1 );");
            popup.getContent().add(errorLabel);
            errorLabel.opacityProperty().bind(popup.opacityProperty());
            zoomFactor = 1d;
            zoomLevel.setValue(100d);
            imageView.setX(0);
            imageView.setY(0);
            imageHeight = jasperPrint.getPageHeight();
            imageWidth = jasperPrint.getPageWidth();
            if (zoomListener != null) {
                zoomLevel.valueProperty().removeListener(zoomListener);
            }
            zoomListener = new ChangeListener<Number>() {

                public void changed(ObservableValue<? extends Number> observable,
                        Number oldValue, Number newValue) {
                    zoomFactor = newValue.doubleValue() / 100;
                    imageView.setFitHeight(imageHeight * zoomFactor);
                    imageView.setFitWidth(imageWidth * zoomFactor);
                }
            };

            zoomLevel.valueProperty().addListener(zoomListener);
            if (jasperPrint.getPages().size() > 0) {
                viewPage(0);
                pages = new ArrayList<Integer>();
                for (int i = 0; i < jasperPrint.getPages().size(); i++) {
                    pages.add(i + 1);
                }
            }
            pageList.setItems(FXCollections.observableArrayList(pages));
            pageList.getSelectionModel().select(0);
        } else if (printMode == JRViewerFxMode.REPORT_PRINT) {
            print();
        }

    }

    /**
     *
     * @return
     */
    @FXML
    public boolean save() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName("report_" + LocalDate.now() + "_" + UiiDGenerator.getUIID8());
        fileChooser.getExtensionFilters().add(new ExtensionFilter("PDF Document", Arrays.asList("*.pdf", "*.PDF")));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG image", Arrays.asList("*.png", "*.PNG")));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("DOCX Document", Arrays.asList("*.docx", "*.DOCX")));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("XLSX Document", Arrays.asList("*.xlsx", "*.XLSX")));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("HTML Document", Arrays.asList("*.html", "*.HTML")));
        File file = fileChooser.showSaveDialog(parentStage);
        if (fileChooser.getSelectedExtensionFilter() != null && fileChooser.getSelectedExtensionFilter().getExtensions() != null) {
            List<String> selectedExtension = fileChooser.getSelectedExtensionFilter().getExtensions();
            if (selectedExtension.contains("*.pdf")) {
                try {
                    JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());
                } catch (JRException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (selectedExtension.contains("*.png")) {
                for (int i = 0; i < jasperPrint.getPages().size(); i++) {
                    String fileNumber = "0000" + Integer.toString(i + 1);
                    fileNumber = fileNumber.substring(fileNumber.length() - 4, fileNumber.length());
                    WritableImage image = getImage(i);
                    String[] fileTokens = file.getAbsolutePath().split("\\.");
                    String filename = "";

                    //add number to filename
                    if (fileTokens.length > 0) {
                        for (int i2 = 0; i2 < fileTokens.length - 1; i2++) {
                            filename = filename + fileTokens[i2] + ((i2 < fileTokens.length - 2) ? "." : "");
                        }
                        filename = filename + fileNumber + "." + fileTokens[fileTokens.length - 1];
                    } else {
                        filename = file.getAbsolutePath() + fileNumber;
                    }
                    System.out.println(filename);
                    File imageFile = new File(filename);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", imageFile);
                        System.out.println(imageFile.getAbsolutePath());
                    } catch (IOException e) {
                        TransactionResult t = new TransactionResult();
                        t.setResultNumber(-1);
                        t.setResult("Error Saving Report");
                        t.setResultDescription(e.getMessage());
                        setTransactionResult(t);
                        e.printStackTrace();
                    }

                }

            } else if (selectedExtension.contains("*.html")) {
                try {
                    JasperExportManager.exportReportToHtmlFile(jasperPrint, file.getAbsolutePath());
                } catch (JRException e) {
                    TransactionResult t = new TransactionResult();
                    t.setResultNumber(-1);
                    t.setResult("Error Saving Report");
                    t.setResultDescription(e.getMessage());
                    setTransactionResult(t);
                    e.printStackTrace();
                }
            } else if (selectedExtension.contains("*.docx")) {
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, file.getAbsolutePath());
                try {
                    exporter.exportReport();
                } catch (JRException e) {
                    TransactionResult t = new TransactionResult();
                    t.setResultNumber(-1);
                    t.setResult("Error Saving Report");
                    t.setResultDescription(e.getMessage());
                    setTransactionResult(t);
                    e.printStackTrace();
                }
                System.out.println("docx");
            } else if (selectedExtension.contains("*.xlsx")) {
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, file.getAbsolutePath());
                try {
                    exporter.exportReport();
                } catch (JRException e) {
                    TransactionResult t = new TransactionResult();
                    t.setResultNumber(-1);
                    t.setResult("Error Saving Report");
                    t.setResultDescription(e.getMessage());
                    setTransactionResult(t);
                    e.printStackTrace();
                }
                System.out.println("xlsx");
            }
        }
        return false;
    }

    private WritableImage getImage(int pageNumber) {
        BufferedImage image = null;
        try {
            image = (BufferedImage) JasperPrintManager.printPageToImage(jasperPrint, pageNumber, 2);
        } catch (JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        WritableImage fxImage = new WritableImage(jasperPrint.getPageWidth(), jasperPrint.getPageHeight());
        return SwingFXUtils.toFXImage(image, fxImage);

    }

    private void viewPage(int pageNumber) {
        imageView.setFitHeight(imageHeight * zoomFactor);
        imageView.setFitWidth(imageWidth * zoomFactor);
        imageView.setImage(getImage(pageNumber));
    }

    /**
     *
     */
    public void clear() {
        // TODO Auto-generated method stub

    }

    @FXML
    private void print() {
        try {
            jasperPrint.setName("Ina reports");
            JasperPrintManager.printReport(jasperPrint, true);
        } catch (JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @FXML
    private void pageListSelected(final ActionEvent event) {
        System.out.println(pageList.getSelectionModel().getSelectedItem() - 1);
        viewPage(pageList.getSelectionModel().getSelectedItem() - 1);
    }

    /**
     *
     * @param result
     * @param description
     * @param resultNum
     */
    public void setTransactionResult(String result, String description,
            int resultNum) {
        TransactionResult t = new TransactionResult();
        t.setResult(result);
        t.setResultDescription(description);
        t.setResultNumber(resultNum);
        t.setTransactionTime(new Date());
        setTransactionResult(t);
    }

    /**
     *
     * @param t
     */
    public void setTransactionResult(TransactionResult t) {
        if (t != null) {
            if (t.getTransactionTime() == null) {
                resultPane.setText(t.getResult() + "  Time: " + new Date());
            } else {
                resultPane.setText(t.getResult() + "  Time: "
                        + t.getTransactionTime());
            }

            resultDescription.setText(t.getResultDescription());
            resultPane.setVisible(true);
            resultAccordion.setVisible(true);
        } else {
            resultPane.setText("General Error Occurred" + "  Time: "
                    + new Date());
            resultDescription.setText("No data was returned.");
            resultPane.setVisible(true);
            resultAccordion.setVisible(true);
        }
        if (t.getResultNumber() != 0 && !showingToast) {
            showingToast = true;
            errorLabel.setText(t.getResult());
            popup.show(parentStage);
            popup.setOpacity(1.0d);
            WarningToast task = new WarningToast();
            task.progressProperty().addListener(new ChangeListener<Number>() {

                public void changed(
                        ObservableValue<? extends Number> observable,
                        Number oldValue, Number newValue) {
                    popup.setOpacity(newValue.doubleValue());
                    if (newValue.doubleValue() <= 0.01d) {
                        popup.hide();
                        showingToast = false;
                    }

                }

            });
            popup.setX(view.getScene().getWindow().getX()
                    + view.getScene().getWindow().getWidth() - 100);
            popup.setY(view.getScene().getWindow().getY());
            new Thread(task).start();

        }
    }

    /**
     *
     */
    public void clearTransactionResult() {

        resultPane.setText("");
        resultDescription.setText("");
        resultPane.setVisible(false);
        resultAccordion.setVisible(false);
    }

    /**
     *
     * @return
     */
    public JRViewerFxMode getPrintMode() {
        return printMode;
    }

    /**
     *
     * @param printMode
     */
    public void setPrintMode(JRViewerFxMode printMode) {
        this.printMode = printMode;
    }

    /**
     *
     * @return
     */
    public String getReportFilename() {
        return reportFilename;
    }

    /**
     *
     * @param reportFilename
     */
    public void setReportFilename(String reportFilename) {
        this.reportFilename = reportFilename;
    }

    /**
     *
     * @return
     */
    public JRDataSource getReportDataset() {
        return reportDataset;
    }

    /**
     *
     * @param reportDataset
     */
    public void setReportDataset(JRDataSource reportDataset) {
        this.reportDataset = reportDataset;
    }

    /**
     *
     * @return
     */
    public Map getReportParameters() {
        return reportParameters;
    }

    /**
     *
     * @param reportParameters
     */
    public void setReportParameters(Map reportParameters) {
        this.reportParameters = reportParameters;
    }

    /**
     *
     * @return
     */
    public Node getView() {
        return view;
    }

    /**
     *
     * @param view
     */
    public void setView(Node view) {
        this.view = view;
    }

    /**
     *
     */
    public void close() {
        parentStage.close();
    }

    /* (non-Javadoc)
     * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
     */
    public void initialize(URL location, ResourceBundle resources) {
        /* ====================== Load All branches ====================== */
        List<String> branches = RetrieveStaticMasterDAO.getAllBranches();
        for (String s : branches) {
            branch.add(s);
        }
        cmbBranch.setItems(branch);
        /* ====================== Load Reports For Dyanamic Branch ====================== */
        cmbBranch.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    String fxmlPath = "/com/zs/ina/admin/reports/view/FXMLReportViewer.fxml";
                    try {
                        Connection con = DBPool.getInstance().getConn();
                        Map<String, Object> parameters = new HashMap<>();
                        List<String> listBranch = new ArrayList<String>();
                        listBranch.add(newValue);
                        parameters.put("branchSelected", listBranch);
                        JasperReport jasperReport = JasperCompileManager.compileReport(FXMLReportViewerController.class.getResourceAsStream("Enquiry_Data_Branch.jrxml"));
                        jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);
                    } catch (JRException ex) {
                        Logger.getLogger(FXMLReportViewerController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    /* ====================== Check fxml to pass ====================== */
                    JRViewerFx viewer = new JRViewerFx(jasperPrint, JRViewerFxMode.REPORT_VIEW, (Stage) btnFirstPage.getScene().getWindow(), fxmlPath);
                    try {
                        // viewer.start((Stage) btnLoadCheck.getScene().getWindow());
                    } catch (Exception ex) {
                        // logger.error(ex.toString());
                        ex.printStackTrace();

                    }
                }
            }
        });
        /* ====================== Add Events to navigation buttons ====================== */
        btnFirstPage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                viewPage(0);
                pageList.getSelectionModel().selectFirst();

            }
        });
        btnLastPage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                viewPage(jasperPrint.getPages().size() - 1);
                pageList.getSelectionModel().selectLast();
            }
        });
        btnNextPage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                pageList.getSelectionModel().selectNext();
            }
        });
        btnPreviousPage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                pageList.getSelectionModel().selectPrevious();

            }
        });

    }

    /**
     *
     * @return
     */
    public JasperPrint getJasperPrint() {
        return jasperPrint;
    }

    /**
     *
     * @param jasperPrint
     */
    public void setJasperPrint(JasperPrint jasperPrint) {
        this.jasperPrint = jasperPrint;
    }

}
